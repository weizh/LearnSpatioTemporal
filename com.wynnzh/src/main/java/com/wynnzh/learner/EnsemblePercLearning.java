package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wynnzh.Utils;

import edu.cmu.lti.weizh.data.DataFactory;
import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.feature.FCONST;
import edu.cmu.lti.weizh.io.Storable;

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
		int n = (int) (N * proportion);

		for (int m = 0; m < modelCount; m++) {

			DataSet aDataSet = getEmptyDataSetWithSingleParagraph();

			for (int i = 0; i < N; i++) {
				boolean toSample = r.nextDouble() < sentsWeights.get(i) ? true : false;
				if (toSample)
					setSentencesToDataSet(aDataSet, totalSents.get(i));
			}
			ActiveLearner.setTypeBeforeCreate(type);
			ActiveLearner aLearner = new ActiveLearner();
			aLearner.train(aDataSet);
			learners.add(aLearner);
		}
		return learners;
	}

	private static void setSentencesToDataSet(DataSet aDataSet, Sentence sampledSentence) {
		try {
			aDataSet.getDocuments().get(0).getParagraphs().get(0).addSentence(sampledSentence);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static DataSet getEmptyDataSetWithSingleParagraph() {

		Paragraph p = new Paragraph();
		Document doc = new Document(null);
		doc.addParagraph(p);
		DataSet d = new DataSet(1, null);
		d.getDocuments().add(doc);
		return d;
	}

	@Override
	protected EnsemblePercLearning self() {
		return this;
	}

	public static void main(String[] arg) throws Exception {

		DataSet train = DataFactory.getCONLL2kTrain();
		DataSet test = DataFactory.getCONLL2kTest();

		List<Sentence> TestSentences = Utils.getSentences(train);

		List<Sentence> U = Utils.getSentences(train);
		List<Double> Uw = new ArrayList<Double>(U.size());

		List<Sentence> T = new ArrayList<Sentence>();
		List<Double> Tw = new ArrayList<Double>(T.size());

		List<ActiveLearner> ensemble = null;
		while (true) {
			List<Sentence> UStar = bestUtility(U, Uw, ensemble);
			T.addAll(UStar);
			modifyWeights(Tw);

			ensemble = createNewEnsembles(FCONST.LEARNERTYPE.CONLL2KChunking, T, Tw, 5, 0.5);

			enesembleTest(ensemble, TestSentences);

		}

	}

	private static void enesembleTest(List<ActiveLearner> ensemble, List<Sentence> testSentences) {

	}
	
	private static void modifyWeights(List<Double> tw) {
		// TODO Auto-generated method stub
		
	}

	private static List<Sentence> bestUtility(List<Sentence> u, List<Double> uw, List<ActiveLearner> ensemble) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
