package com.wynnzh.learner;

import java.util.List;

import com.wynnzh.Utils;

import edu.cmu.lti.weizh.data.DATA_PATHS;
import edu.cmu.lti.weizh.data.DataFactory;
import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.feature.FCONST.LEARNERTYPE;

public class RandomPercLearningDataSet {

	public static void main(String argv[]) throws Exception{
		
		System.out.print("ONF_ABC_RANDOM SAMPLING");
		
//		DataSet  train = DataFactory.getCONLL2kTrain();
//		DataSet  test = DataFactory.getCONLL2kTest();
		DataSet train = DataFactory.getONFDataSet(DATA_PATHS.ONF_NBC_TRAIN, true);
		DataSet test = DataFactory.getONFDataSet(DATA_PATHS.ONF_NBC_TEST, true);
		
		List<Sentence> sents = Utils.getSentences(train);
		
		ActiveLearner aner = new ActiveLearner(LEARNERTYPE.OntoNotesNewsNER);
		
		int T= 1000;int t = 0 ;
		for (Sentence s : sents) if ((t++)<T){
			System.out.print("Iteration "+ t +"\n");
			aner.resetModel();
			aner.addTrainingSentence(s);
			aner.train(100, 1E-10);
			
			ActiveEvaluator eval = new ActiveEvaluator(aner);
			eval.evaluate(test);
		}
	}

	
}
