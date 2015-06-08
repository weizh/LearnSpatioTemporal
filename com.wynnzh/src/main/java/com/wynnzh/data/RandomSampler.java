package com.wynnzh.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import model.SentenceSample;

public class RandomSampler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<SentenceSample> testsents;

	public List<SentenceSample> getTestsents() {
		return testsents;
	}

	public void setTestsents(List<SentenceSample> testsents) {
		this.testsents = testsents;
	}

	public List<SentenceSample> getTrainsents() {
		return trainsents;
	}

	public void setTrainsents(List<SentenceSample> trainsents) {
		this.trainsents = trainsents;
	}

	List<SentenceSample> trainsents;

	RandomSampler(List<SentenceSample> trainSents, List<SentenceSample> testSents) {
		this.trainsents = trainSents;
		this.testsents = testSents;
	}

	public static void main(String argb[]) throws Exception {

		List<SentenceSample> test = WCDataSet.convertToSentenceMapEntryList(WCDataSet
				.load("src/main/resources/dataObjects/WCtest_POSEnhanced.data.bin"));

		List<SentenceSample> train = WCDataSet.convertToSentenceMapEntryList(WCDataSet
				.load("src/main/resources/dataObjects/WCtrain_POSEnhanced.data.bin"));

		long seed = System.nanoTime();
		// randomize list 5 times.
		for (int i = 0; i < 5; i++) {
			Collections.shuffle(train, new Random(seed));
			Collections.shuffle(test, new Random(seed));
		}
		RandomSampler sampler = new RandomSampler(train, test);
		sampler.store("src/main/resources/dataObjects/SuffledEnhancedTrainTestData.randomsampler");
	}

	private void store(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static RandomSampler load(String string) {
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(string));
		} catch (IOException e) {
			e.printStackTrace();
		}
		RandomSampler t = null;
		try {
			t = (RandomSampler) is.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

	public WCDataSet getTestSentsAsDataSet() {
		WCDataSet d = new WCDataSet(0, null);
		d.getDocuments().add(new Document(null));
		d.getDocuments().get(0).addParagraph(new Paragraph());
		Paragraph p = null;
		try {
			p = d.getDocuments().get(0).getParagraphs().get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for ( SentenceSample s : testsents){
			p.addSentence(s.getValue());
		}
		return d;
	}
}
