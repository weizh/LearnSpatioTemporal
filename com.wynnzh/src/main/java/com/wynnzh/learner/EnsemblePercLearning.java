package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.wynnzh.Utils;

import edu.cmu.lti.weizh.data.DATA_PATHS;
import edu.cmu.lti.weizh.data.DataFactory;
import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.docmodel.Word;
import edu.cmu.lti.weizh.eval.Prediction;
import edu.cmu.lti.weizh.feature.FCONST;
import edu.cmu.lti.weizh.io.Storable;
import edu.cmu.lti.weizh.mlmodel.PerceptronFDMM.iNode;

public class EnsemblePercLearning extends Storable<EnsemblePercLearning> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Random r = new Random();

	@Override
	protected EnsemblePercLearning self() {
		return this;
	}

	// TODO modify the weights based on the simulated annealing idea. Here we
	// just assign uniformly distributed weights.
	// THe weights are illustrated in the IBM Notebook. June 11th.
	// alpha = 2(1-r)/(t-1).

	private static void modifyWeights(List<Sentence> T, List<Double> tw, List<Sentence> UStar, double sampleProb, boolean rw) {

		if (tw.size() == 0) {
			for (int i = 0; i < T.size(); i++)
				tw.add(sampleProb);
			return;
		}

		if (rw) {
			// Below is for sentence reweighting.
			for (int i = 0; i < UStar.size(); i++) {
				tw.add(1.0);
			}

			double alpha = 2.0 * (1.0 - sampleProb) / (double) (T.size() - 1);
			for (int i = 0; i < tw.size() - 1; i++) {
				double neww = tw.get(i) - alpha;
				if (neww < sampleProb)
					neww = sampleProb;
				tw.set(i, neww);
			}
		} else {
			// below is for uniform sampling
			for (int i = 0; i < UStar.size(); i++) {
				tw.add(sampleProb);
			}
		}

	}

	private static String getGoldLabel(Word w) {
		return w.getEntityType();
	}

	public static void main(String[] arg) throws Exception {

		boolean reweighting = true;
		int coldstart = 20;
		System.out.println(" ONF_PRI_ cold start="+coldstart+" reweighting=" + reweighting);

		int n = 1; // how many sentences in a batch.
		// DataSet train = DataFactory.getCONLL2kTrain();
		// DataSet test = DataFactory.getCONLL2kTest();
		// DataSet train = DataFactory.getNLPBATrain();
		// DataSet test = DataFactory.getNLPBAEval();
		DataSet train = DataFactory.getONFDataSet(DATA_PATHS.ONF_NBC_TRAIN, true);
		DataSet test = DataFactory.getONFDataSet(DATA_PATHS.ONF_NBC_TEST, true);

		List<Sentence> TestSentences = Utils.getSentences(test);
		System.out.println("Total final test sentence # is :" + TestSentences.size());
		List<Sentence> U = Utils.getSentences(train);
		System.out.println("Initial U size is:" + U.size());

		List<Sentence> T = new ArrayList<Sentence>();
		System.out.println("Initial T size is:" + T.size());

		List<Double> Tw = new ArrayList<Double>();

		List<ActiveLearner> ensemble = null;

		int iter = 0;
		while (true) {
			System.out.println("iteration " + (iter++));
			List<Sentence> UStar = new ArrayList<Sentence>();

			System.out.println("Selecting next best sentence to annotate (simulation)");
			UStar.addAll(bestUtility(U, ensemble,coldstart));

			double sampleRate = 0.5;

			T.addAll(UStar);
			System.out.println("T size is :" + T.size());

			removeFromTraining(U, UStar);
			System.out.println("U size is :" + U.size());

			System.out.println(U.size());

			modifyWeights(T, Tw, UStar, sampleRate, reweighting);

			System.out.println("Creating new ensembles with new training set");
			ensemble = createNewEnsembles(FCONST.LEARNERTYPE.OntoNotesNewsNER, T, Tw, 5, sampleRate);
			System.out.println("Test ensemble on test data");
			enesembleTest(ensemble, TestSentences);
		}
	}

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
		// int n = (int) (N * proportion);

		for (int m = 0; m < modelCount; m++) {

			ArrayList<Sentence> selected = new ArrayList<Sentence>();

			for (int i = 0; i < N; i++) {
				if (i < 10)
					selected.add(totalSents.get(i));
				boolean toSample = r.nextDouble() < sentsWeights.get(i) ? true : false;
				if (toSample)
					selected.add(totalSents.get(i));
			}
			ActiveLearner aLearner = new ActiveLearner(type);
			aLearner.setSentences(selected);
			aLearner.train(100, 1E-10);
			learners.add(aLearner);
		}
		return learners;
	}

	private static void removeFromTraining(List<Sentence> u, List<Sentence> uStar) {

		Iterator<Sentence> iter = u.iterator();
		while (iter.hasNext()) {
			Sentence n = iter.next();
			if (uStar.contains(n))
				iter.remove();
		}
	}

	private static void enesembleTest(List<ActiveLearner> ensemble, List<Sentence> testSentences) throws Exception {
		 HashMap<String, Double> predictions = new HashMap<String, Double>();
		 HashMap<String, Double> correctPredictions = new HashMap<String,
		 Double>();
		 HashMap<String, Double> totalCorrect = new HashMap<String, Double>();

		HashMap<String, Double> indpredictions = new HashMap<String, Double>();
		HashMap<String, Double> indcorrectPredictions = new HashMap<String, Double>();
		HashMap<String, Double> indtotalCorrect = new HashMap<String, Double>();

		int iter = 0;
		for (Sentence c : testSentences) {
			iter++;
			// if ((iter++) % 100 == 0)
			// System.out.println("Tested " + (iter - 1) + " sentences");

			 Prediction[] finalPred = new Prediction[c.getWords().size()];
			Prediction[] indFinalPred = new Prediction[c.getWords().size()];
			for (int i = 0; i < c.getWords().size(); i++) {
				 finalPred[i] = new Prediction(1);
				indFinalPred[i] = new Prediction(1);
			}
			// Test individual sentence with ensemble.
			for (ActiveLearner learner : ensemble) {

				// create final prediction by aggregating sequences using
				// sentence prob.
				// use the max prob sequence pred[0].
				 iNode[] pred = learner.predictNBest(c, 2);
				 double prob = pred[0].prob;
				 String[] str = pred[0].sequence;
				 mergeToFinalPredWithSeqProb(str, prob, finalPred);
				// create final prediction by aggregating individual
				// forward-backward probabilities.
				Prediction[] fbpred = learner.predictBestWithProbabilities(c);
				mergeToFinalPredWithIndProb(fbpred, indFinalPred);

			}
			List<Word> words = c.getWords();
			 String[] bestfinalPred = bestCand(finalPred);
			String[] bestIndfinalPred = bestCand(indFinalPred);

			for (int i = 0; i < words.size(); i++) {
				String type = getGoldLabel(words.get(i));

				 String finalPrediction = bestfinalPred[i];
				String indfinalPrediction = bestIndfinalPred[i];

				 putToMap(predictions, finalPrediction);
				 if (type.equals(finalPrediction))
				 putToMap(correctPredictions, type);
				 putToMap(totalCorrect, type);

				putToMap(indpredictions, indfinalPrediction);
				if (type.equals(indfinalPrediction))
					putToMap(indcorrectPredictions, type);
				putToMap(indtotalCorrect, type);

			}
		}
		 System.out.println("Sequence probability for single token:");
		 printF1(totalCorrect, predictions, correctPredictions);

		System.out.println("Individual probability (fb algorithm) used:");
		printF1(indtotalCorrect, indpredictions, indcorrectPredictions);
	}

	private static String[] bestSeqCand(HashMap<List<String>, Double> seqs) {
		double max = Double.NEGATIVE_INFINITY;
		List<String> string = null;
		for (Entry<List<String>, Double> entry : seqs.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
				string = entry.getKey();
			}
		}
		return string.toArray(new String[] {});
	}

	private static void mergeToFinalPredWithIndProb(Prediction[] fbpred, Prediction[] indFinalPred) {
		for (int i = 0; i < fbpred.length; i++) {
			String bestName = fbpred[i].getBestCandidateName();
			double val = fbpred[i].getBestCandidateProb();
			HashMap<String, Double> m = indFinalPred[i].getMap();
			m.put(bestName, m.containsKey(bestName) ? m.get(bestName) + val : val);
		}
	}

	private static void mergeToFinalPredWithSeqProb(String[] str, double prob, Prediction[] finalPred) {
		for (int i = 0; i < finalPred.length; i++) {
			HashMap<String, Double> m = finalPred[i].getMap();
			m.put(str[i], m.containsKey(str[i]) ? m.get(str[i]) + prob : prob);
		}
	}

	private static String[] bestCand(Prediction[] p) {
		String f[] = new String[p.length];
		for (int i = 0; i < p.length; i++) {
			f[i] = p[i].getBestMapCand();
		}
		return f;
	}

	/**
	 * Use the first 20 sentences for cold start.
	 * 
	 * @param u
	 * @param ensemble
	 * @return
	 * @throws Exception
	 */
	private static List<Sentence> bestUtility(List<Sentence> u, List<ActiveLearner> ensemble, int coldstart) throws Exception {

		if (ensemble == null) {
			return u.subList(0, coldstart);
		}
		double maxUtil = Double.NEGATIVE_INFINITY;
		Sentence maxSentence = null;
		for (int i = 0; i < u.size(); i++) {
			if (i % 1000 == 0)
				System.out.println("Checking out sentence " + i);
			double util = utility(u.get(i), ensemble);
			if (util > maxUtil) {
				util = maxUtil;
				maxSentence = u.get(i);
			}
		}
		System.out.println("Utility found Sentence: " + maxSentence.getWords());

		ArrayList<Sentence> rsent = new ArrayList<Sentence>();
		rsent.add(maxSentence);
		return rsent;
	}

	private static double utility(Sentence s, List<ActiveLearner> ensemble) throws Exception {

		HashMap<String, Double> seqMap = new HashMap<String, Double>();

		for (int i = 0; i < ensemble.size(); i++) {
			iNode[] pred = ensemble.get(i).predictNBest(s, 5);
			StringBuffer dstr = new StringBuffer();
			for (int k = 0; k < pred.length; k++) {
				for (String label : pred[k].sequence) {
					dstr.append(label);
				}
				String ds = dstr.toString();
				if (seqMap.containsKey(ds) == false) {
					seqMap.put(ds, pred[k].prob);
				} else
					seqMap.put(ds, seqMap.get(ds) + pred[k].prob);
			}
		}

		/**
		 * Settles et al. (emnlp 08). Sequence vote entropy.
		 */
		double entropy = 0;

		for (Entry<String, Double> e : seqMap.entrySet()) {
			String seq = e.getKey();
			double v = e.getValue() / (double) ensemble.size();
			entropy -= v * Math.log(v);
		}

		return entropy;
	}

	// /////////////////////////////

	private static void putToMap(HashMap<String, Double> m, String p) {

		if (m.containsKey(p))
			m.put(p, m.get(p) + 1);
		else
			m.put(p, 1.0);

	}

	private static void printF1(HashMap<String, Double> totalCorrect, HashMap<String, Double> predictions,
			HashMap<String, Double> correctPredictions) {

		double totalPred = getTotalNATExcluded(predictions);
		double totalGold = getTotalNATExcluded(totalCorrect);
		double totalCPred = getTotalNATExcluded(correctPredictions);
		System.out.println(totalPred + " " + totalGold + " " + totalCPred);
		System.out.println("System total recall: " + totalCPred / totalGold);
		System.out.println("System total precision: " + totalCPred / totalPred);
		System.out.println("System total F1: " + 2 * totalCPred / (totalGold + totalPred));
	}

	private static double getTotalNATExcluded(HashMap<String, Double> predictions) {
		double t = 0;
		for (Entry<String, Double> e : predictions.entrySet()) {
			if (e.getKey().equalsIgnoreCase("o") || e.getKey().equalsIgnoreCase("[o]")) {
				continue;
			}
			t += e.getValue();
		}
		return t;
	}

}
