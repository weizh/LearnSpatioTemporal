package com.wynnzh.learner;

import java.util.List;

import model.SentenceSample;

import com.wynnzh.data.RandomSampler;
import com.wynnzh.data.WCDataSet;

import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;

public class RandomLearning extends ActiveLearner {

	RandomLearning(){
		super();
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2400041746089127990L;

	public static void main(String argv[]) throws Exception{
		RandomLearning r = new RandomLearning();
		RandomSampler samples =  RandomSampler.load("src/main/resources/dataObjects/SuffledEnhancedTrainTestData.randomsampler");
//		List<SentenceSample> train = samples.getTrainsents();
		List<SentenceSample> test = samples.getTestsents();
		System.out.println("Test sentence size is "+ test.size());
		WCDataSet testData = samples.getTestSentsAsDataSet();
		
		WCDataSet runningData = new WCDataSet(1, "Running DataSet in each iteration.");
		runningData.getDocuments().add(new Document("Running doc in dataset"));
		Document runningDoc = runningData.getDocuments().get(0);
		runningDoc.addParagraph(new Paragraph());
		Paragraph runningPara = runningDoc.getParagraphs().get(0);
		
		int T= 1000;int t = 0 ;
		for (SentenceSample s : test) if ((t++)<T){
			System.out.print("Iteration "+ t +"\n");
			r.getSentences().add(s.getValue());
			runningPara.addSentence(s.getValue());
			r.resetModel();
			r.train(runningData, 100, 1E-10);
			
			ActiveEvaluator eval = new ActiveEvaluator((ActiveLearner)r);
			eval.evaluate(testData);
		}
	}

}
