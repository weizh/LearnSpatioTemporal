package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.docmodel.Word;
import edu.cmu.lti.weizh.eval.Prediction;
import edu.cmu.lti.weizh.feature.FCONST;
import edu.cmu.lti.weizh.feature.Feature;
import edu.cmu.lti.weizh.feature.Theta;
import edu.cmu.lti.weizh.mlmodel.PerceptronFDMM;
import edu.cmu.lti.weizh.mlmodel.PerceptronFDMM.iNode;
import edu.cmu.lti.weizh.train.AbstractPercTrain;

public class ActiveLearner extends AbstractPercTrain<String, ActiveLearner, DataSet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<Sentence> trainedSentences;

	static FCONST.LEARNERTYPE type = null;

	ActiveLearner(FCONST.LEARNERTYPE learnerType) {

		setTypeBeforeCreate(learnerType);
		this.pfdmm = new PerceptronFDMM();

		Theta.setTHETA_HEADERS(thetaHeaders);

		trainedSentences = new ArrayList<Sentence>();
	}

	private void setTypeBeforeCreate(FCONST.LEARNERTYPE learnerType) {
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

	public void resetModel() {
		this.pfdmm = new PerceptronFDMM();
	}

	public List<Sentence> getSentences() {
		return trainedSentences;
	}
	public void setSentences( List<Sentence> sent ) {
		 trainedSentences = sent;
	}
	@Override
	protected String getGoldLabel(Word w) {

		if (type.equals(FCONST.LEARNERTYPE.CONLL2kPOS)) {
			return w.getPartOfSpeech();
		} else if (type.equals(FCONST.LEARNERTYPE.OntoNotesNewsNER)) {
			return w.getEntityType();
		} else if (type.equals(FCONST.LEARNERTYPE.CONLL2KChunking)) {
			return w.getChunkType();
		} else if (type.equals(FCONST.LEARNERTYPE.WebCrawl)) {
			String i = w.getEntityType();
			if (i == null)
				return "[O]";
			if (i.equalsIgnoreCase("o"))
				return "ORG";
		}
		return null;
	}

	@Override
	protected ActiveLearner self() {
		return this;
	}

	public void train(int t, double th) throws Exception {
		this.setT(t);
		this.setThreshold(th);
		DataSet d = new DataSet(100, null);
		Document doc = new Document(null);
		Paragraph p = new Paragraph();
		d.getDocuments().add(doc);
		doc.addParagraph(p);
		p.getSentences().addAll(this.trainedSentences);
		super.train(d);
	}

	public void train(DataSet t) {
		throw new UnsupportedOperationException("Should call train(int t, double th).");
	}

	public void train(DataSet d, int t, double th) throws Exception {
		throw new UnsupportedOperationException("Should call train(int t, double th).");
	}

	public void addTrainingSentence(Sentence value) {
		this.trainedSentences.add(value);
	}

	public Prediction[] predictBestWithProbabilities(Sentence s) throws Exception {

		List<Word> words = s.getWords();

		List<Theta<String>> thetas = new ArrayList<Theta<String>>(words.size());
		List<List<Feature<String>>> features = new ArrayList<List<Feature<String>>>(words.size());
		String[] goldLabels = new String[words.size()];

		for (int i = 0; i < words.size(); i++) {
			Word w = words.get(i);
			Theta<String> theta = new Theta<String>(w);
			thetas.add(theta);
			List<Feature<String>> feats = new ArrayList<Feature<String>>(getFeatureHeaders().length);
			for (String fheader : getFeatureHeaders())
				feats.add(new Feature<String>(fheader, s, i));
			features.add(feats);
			goldLabels[i] = (getGoldLabel(w));
		}
		int denom = getIterationsUsed() * getTotalSentProcessed();
		String[] predictions;
		predictions = getModel().viterbiDecodeAvgParam(thetas, features, denom);
		Prediction[] predProbs = getModel().maxProductAvgParam(thetas, features, denom);
		
		for (int i = 0 ; i < predProbs.length; i++){
			predProbs[i].setBestCandidateName(predictions[i]);
		}
		predictions = Prediction2String(predProbs);
		
		setPredictions(words, predictions);

		return predProbs;
	}

	public iNode[] predictNBest(Sentence s, int N) throws Exception {

		List<Word> words = s.getWords();

		List<Theta<String>> thetas = new ArrayList<Theta<String>>(words.size());
		List<List<Feature<String>>> features = new ArrayList<List<Feature<String>>>(words.size());
		String[] goldLabels = new String[words.size()];

		for (int i = 0; i < words.size(); i++) {
			Word w = words.get(i);
			Theta<String> theta = new Theta<String>(w);
			thetas.add(theta);
			List<Feature<String>> feats = new ArrayList<Feature<String>>(getFeatureHeaders().length);
			for (String fheader : getFeatureHeaders())
				feats.add(new Feature<String>(fheader, s, i));
			features.add(feats);
			goldLabels[i] = (getGoldLabel(w));
		}
		int denom = getIterationsUsed() * getTotalSentProcessed();
		iNode[] predictions;
		predictions = getModel().viterbiDecodeAvgParam(thetas, features, denom, N);
		String [] strPreds = predictions[0].getSequence();
		setPredictions(words, strPreds);

		return predictions;
	}

	private String[] Prediction2String(Prediction[] preds) {
		String p[] = new String[preds.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = preds[i].getBestCandidateName();
		}
		return p;
	}

	private void setPredictions(List<Word> words, String[] predictions) {
		int i = 0;
		for (Word w : words) {
			try {
				w.setPrediction(predictions[i++]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
