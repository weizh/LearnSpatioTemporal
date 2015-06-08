package com.wynnzh.learner;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.weizh.data.DataFactory;
import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.feature.FCONST.LEARNERTYPE;

public class RandomPercLearningDataSet {

	public static void main(String argv[]) throws Exception{
		
		DataSet  train = DataFactory.getCONLL2kTrain();
		DataSet  test = DataFactory.getCONLL2kTest();
		List<Sentence> sents = getSentences(train);
		
		ActiveLearner.setTypeBeforeCreate(LEARNERTYPE.CONLL2KChunking);
		ActiveLearner aner = new ActiveLearner();
		
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

	private static List<Sentence> getSentences(DataSet d) {
		List<Sentence> sent = new ArrayList<Sentence>();
		for (Document doc : d.getDocuments())
			try {
				for (Paragraph p : doc.getParagraphs()){
					sent.addAll(p.getSentences());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return sent;
	}
}