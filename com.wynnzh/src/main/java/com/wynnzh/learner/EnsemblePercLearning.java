package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	public static List<ActiveLearner> createNewEnsemblesWithReplacement(FCONST.LEARNERTYPE type, List<Sentence> totalSents,
			List<Double> sentsWeights, int modelCount, double proportion) throws Exception {
		List<ActiveLearner> learners = new ArrayList<ActiveLearner>(modelCount);

		double length = totalSents.size();
		int toSampleLength = (int) (length * proportion);

		for (int m = 0; m < modelCount; m++) {

			DataSet aDataSet = getEmptyDataSetWithSingleParagraph();

			for (int i = 0; i < toSampleLength; i++) {
				int nextSentenceIndex = (int) (length * r.nextDouble());
				setSentencesToDataSet(aDataSet, totalSents.get(nextSentenceIndex));
			}
			ActiveLearner.setTypeBeforeCreate(type);
			ActiveLearner aLearner = new ActiveLearner();
			aLearner.train(aDataSet);
			learners.add(aLearner);
		}
		return learners;
	}

	public static void main(String[] arg) throws Exception {

		DataSet d = new DataSet(0, null);

		List<Sentence> totalSents = new ArrayList<Sentence>();
		for (Document doc : d.getDocuments())
			for (Paragraph para : doc.getParagraphs())
				totalSents.addAll(para.getSentences());

		double[] sentenceWeights = new double[totalSents.size()];
		for (int i = 0; i < sentenceWeights.length; i++) {
			sentenceWeights[i] = 1;
		}
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
}
