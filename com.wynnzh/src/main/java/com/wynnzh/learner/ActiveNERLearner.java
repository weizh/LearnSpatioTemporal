package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.docmodel.Word;
import edu.cmu.lti.weizh.feature.FCONST;
import edu.cmu.lti.weizh.mlmodel.PerceptronFDMM;
import edu.cmu.lti.weizh.train.AbstractPercTrain;

public class ActiveNERLearner extends AbstractPercTrain<String, ActiveNERLearner, DataSet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<Sentence> trainedSentences;

	static String thd;
	static String tvd;
	static String fhd;
	static String fvd;

	static String[] thetaHeaders;
	static String[] featureHeaders;

	static {
		thd = "-thd-";
		tvd = "-tvd-";
		fhd = "-fhd-";
		fvd = "-fvd-";

		thetaHeaders = new String[] {
				// basic
				FCONST.T_WORD, FCONST.T_LEMMA, FCONST.T_WORDFORM + thd + FCONST.T_SUFFIX,
				FCONST.T_PREFIX + thd + FCONST.T_SUFFIX, FCONST.T_POS + thd + FCONST.T_SUFFIX, FCONST.T_POS, FCONST.T_WORDFORM,
				FCONST.T_SUFFIX };

		featureHeaders = new String[] {
				// basic

				FCONST.p(FCONST.F_POS, 2) + fhd + FCONST.p(FCONST.F_POS, 1),
				FCONST.p(FCONST.F_POS, 1) + fhd + FCONST.n(FCONST.F_POS, 1),
				FCONST.n(FCONST.F_POS, 1) + fhd + FCONST.n(FCONST.F_POS, 2),

				FCONST.p(FCONST.F_WORDFORM, 2) + fhd + FCONST.p(FCONST.F_WORDFORM, 1),
				FCONST.p(FCONST.F_WORDFORM, 1) + fhd + FCONST.n(FCONST.F_WORDFORM, 1),
				FCONST.n(FCONST.F_WORDFORM, 1) + fhd + FCONST.n(FCONST.F_WORDFORM, 2),

				FCONST.p(FCONST.F_LEMMA, 1), FCONST.p(FCONST.F_LEMMA, 2), FCONST.n(FCONST.F_LEMMA, 1),
				FCONST.n(FCONST.F_LEMMA, 2), };

	}

	ActiveNERLearner() {
		super(thetaHeaders, thd, tvd, featureHeaders, fhd, fvd);
		trainedSentences = new ArrayList<Sentence>();
	}

	public void resetModel() {
		this.pfdmm = new PerceptronFDMM();
	}
	public List<Sentence> getSentences() {
		return trainedSentences;
	}

	@Override
	protected String getGoldLabel(Word w) {
		String i = w.getEntityType();
		 if (i==null) return "[O]";
		 if (i.equalsIgnoreCase("o"))return "ORG";
		 return i;
	}

	@Override
	protected ActiveNERLearner self() {

		return this;
	}

}
