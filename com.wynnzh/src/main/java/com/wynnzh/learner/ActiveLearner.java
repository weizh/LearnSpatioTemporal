package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.docmodel.Word;
import edu.cmu.lti.weizh.feature.FCONST;
import edu.cmu.lti.weizh.feature.Feature;
import edu.cmu.lti.weizh.feature.Theta;
import edu.cmu.lti.weizh.mlmodel.PerceptronFDMM;
import edu.cmu.lti.weizh.train.AbstractPercTrain;

public class ActiveLearner extends AbstractPercTrain<String, ActiveLearner, DataSet> {

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

	static FCONST.LEARNERTYPE type = null;

	public static void setTypeBeforeCreate(FCONST.LEARNERTYPE learnerType) {
		type = learnerType;
		switch (type) {
		case CONLL2KChunking:
			thetaHeaders = Theta.getConll2kChunkingthetaHeaders();
			featureHeaders = Feature.getCONLL2KChunkingFeatureHeaders();
		case CONLL2kPOS:
			thetaHeaders = Theta.getPOSthetaHeaders();
			featureHeaders = Feature.getPOSfeatureHeaders();
		case OntoNotesNewsNER:
		case WebCrawl:
			thetaHeaders = Theta.getOnfNerThetaHeaders();
			featureHeaders = Feature.getOnfNerFeatureHeaders();
		default:
		}
		if (thetaHeaders == null || featureHeaders == null)
			throw new UnsupportedOperationException("The type of ActiveLearner is not defined.");
	}

	ActiveLearner() {
		super(thetaHeaders, Theta.getThd(), Theta.getTvd(), featureHeaders, Feature.getFhd(), Feature.getFhd());
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
		if (type.equals(FCONST.LEARNERTYPE.WebCrawl)) {
			if (i == null)
				return "[O]";
			if (i.equalsIgnoreCase("o"))
				return "ORG";
		}
		return i;
	}

	@Override
	protected ActiveLearner self() {
		return this;
	}
	
	public void train(int t, double th) throws Exception{
		this.setT(T);
		this.setThreshold(th);
		DataSet d = new DataSet(100, null);
		Document doc = new Document(null);
		Paragraph p = new Paragraph();
		d.getDocuments().add(doc); doc.addParagraph(p);
		p.getSentences().addAll(this.trainedSentences);
		super.train(d);
	}
	
	public void train(DataSet t){
		throw new UnsupportedOperationException("Should call train(int t, double th).");
	}
	
	public void train(DataSet d, int t, double th) throws Exception{
		throw new UnsupportedOperationException("Should call train(int t, double th).");
	}

	public void addTrainingSentence(Sentence value) {
		this.trainedSentences.add(value);
		
	}
}
