package com.wynnzh.learner;

import java.util.List;

import model.SentenceSample;

import com.wynnzh.data.RandomSampler;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.feature.FCONST.LEARNERTYPE;

public class RandomPercLearningWCDataSet {

	public static void main(String argv[]) throws Exception{
		RandomSampler samples =  RandomSampler.load("src/main/resources/dataObjects/SuffledEnhancedTrainTestData.randomsampler");
//		List<SentenceSample> train = samples.getTrainsents();
		List<SentenceSample> test = samples.getTestsents();
		System.out.println("Test sentence size is "+ test.size());
		DataSet testData = samples.getTestSentsAsDataSet();
		
		DataSet runningData = new DataSet(1, "Running DataSet in each iteration.");
		runningData.getDocuments().add(new Document("Running doc in dataset"));
		
		ActiveLearner aner = new ActiveLearner(LEARNERTYPE.OntoNotesNewsNER);
		
		int T= 1000;int t = 0 ;
		for (SentenceSample s : test) if ((t++)<T){
			System.out.print("Iteration "+ t +"\n");
			aner.resetModel();
			aner.addTrainingSentence(s.getValue());
			aner.train(100, 1E-10);
			
			ActiveEvaluator eval = new ActiveEvaluator(aner);
			eval.evaluate(testData);
		}
	}
}
