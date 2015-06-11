package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.wynnzh.Utils;

import edu.cmu.lti.weizh.data.DataFactory;
import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.feature.FCONST;
import edu.cmu.lti.weizh.io.Storable;
import edu.cmu.lti.weizh.mlmodel.PerceptronFDMM.iNode;

public class EnsemblePercLearning extends Storable<EnsemblePercLearning> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Random r = new Random();

	/**
	 * 
	 * @param type
	 * @param totalSents
	 * @param sentsWeights
	 * @param modelCount
	 * @param proportion
	 *            To Sample in total Sentences.
	 * @return
	 * @throws Exception
	 */
	public static List<ActiveLearner> createNewEnsembles(FCONST.LEARNERTYPE type, List<Sentence> totalSents,
			List<Double> sentsWeights, int modelCount, double proportion) throws Exception {
		List<ActiveLearner> learners = new ArrayList<ActiveLearner>(modelCount);

		double N = totalSents.size();
//		int n = (int) (N * proportion);

		for (int m = 0; m < modelCount; m++) {

			ArrayList<Sentence> selected = new ArrayList<Sentence>();

			for (int i = 0; i < N; i++) {
				if (i<10) selected.add(totalSents.get(i));
				boolean toSample = r.nextDouble() < sentsWeights.get(i) ? true : false;
				if (toSample)
					selected.add( totalSents.get(i));
			}
			ActiveLearner aLearner = new ActiveLearner(type);
			aLearner.setSentences( selected );
			aLearner.train(100,1E-10);
			learners.add(aLearner);
		}
		return learners;
	}

	@Override
	protected EnsemblePercLearning self() {
		return this;
	}

	public static void main(String[] arg) throws Exception {

		int n = 1; //how many sentences in a batch.
		DataSet train = DataFactory.getCONLL2kTrain();
		DataSet test = DataFactory.getCONLL2kTest();

		List<Sentence> TestSentences = Utils.getSentences(train);
		
		List<Sentence> U = Utils.getSentences(train);
		System.out.println("Initial U size is:"+ U.size());

		List<Sentence> T = new ArrayList<Sentence>();
		System.out.println("Initial T size is:" + T.size());

		List<Double> Tw = new ArrayList<Double>();

		List<ActiveLearner> ensemble = null;
		
		while (true) {
//			System.out.println("iteration i");
			List<Sentence> UStar = new ArrayList<Sentence>();
			UStar.add(bestUtility(U, ensemble));

			T.addAll(UStar);
			System.out.println("T size is :" + T.size());
			
			removeFromTraining(U,UStar);
			System.out.println("U size is :" + U.size());
			
			System.out.println(U.size());
			double sampleProb = 0.5;
			modifyWeights(T,Tw,sampleProb);

			ensemble = createNewEnsembles(FCONST.LEARNERTYPE.CONLL2KChunking, T, Tw, 5, sampleProb);
			enesembleTest(ensemble, TestSentences);
		}
	}

	private static void removeFromTraining(List<Sentence> u, List<Sentence> uStar) {
		
		 Iterator<Sentence> iter = u.iterator();
		 while (iter.hasNext()) {
			 Sentence n = iter.next();
			 if (uStar.contains(n)) iter.remove();
		 }
	}

	private static void enesembleTest(List<ActiveLearner> ensemble, List<Sentence> testSentences) {
		
	}
	
	//TODO modify the weights based on the simulated annealing idea. Here we just assign uniformly distributed weights.
	private static void modifyWeights(List<Sentence> T, List<Double> tw, double sampleProb) {
		for (int i=0; i< T.size(); i++){
			tw.add(sampleProb);
		}
	}

	private static Sentence bestUtility(List<Sentence> u, List<ActiveLearner> ensemble) throws Exception {
		
		if (ensemble == null){
			return u.get(0);
		}
		double maxUtil = Double.NEGATIVE_INFINITY;
		Sentence maxSentence = null;
		for (int i = 0 ; i < u.size(); i++){
//			System.out.println("Checking out sentence " + i);
			double util = utility(u.get(i),ensemble);
			if (util > maxUtil){
				util = maxUtil;
				maxSentence = u.get(i);
			}
		}
		System.out.println("Utility found Sentence: " + maxSentence.getWords());

		return maxSentence;
	}
	
	private static double utility(Sentence s, List<ActiveLearner> ensemble) throws Exception{
		
		HashMap<String, Double> seqMap = new HashMap<String,Double>();
		
		for (int i = 0 ; i < ensemble.size() ; i ++){
			 iNode[] pred = ensemble.get(i).predictNBest(s, 5);
			 StringBuffer dstr = new StringBuffer();
			 for (int k = 0; k< pred.length; k++){
				 for(String label : pred[k].sequence){
					 dstr.append(label);
				 }
				 String ds = dstr.toString();
				 if (seqMap.containsKey(ds.toString())==false){
					 seqMap.put(ds,pred[k].prob);
				 }else
					 seqMap.put(ds, seqMap.get(ds)+pred[k].prob);
			 }
		}
		
		/**
		 * Settles et al. (emnlp 08). Sequence vote entropy.
		 */
		double entropy = 0;
		
		for ( Entry<String, Double> e : seqMap.entrySet()){
			String seq = e.getKey();
			double v = e.getValue()/(double)ensemble.size();
			entropy -= v *Math.log(v);
		}

		return entropy;
	}	
}
