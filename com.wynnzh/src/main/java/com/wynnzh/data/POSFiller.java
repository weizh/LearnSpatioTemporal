package com.wynnzh.data;

import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.nlp.pos.PercTaggerPosOnf;

public class POSFiller {

	public static void main(String arvg[]) throws Exception {
		WCDataSet te = WCDataSet.getTaggedTestDataset();
		WCDataSet tr = WCDataSet.getUntaggedTrainingDataset();
		PercTaggerPosOnf postagger = new PercTaggerPosOnf(
				"src/main/resources/trainedModels/ONF_CNNTrain_POS_PERC_100_NegInf_basic.trainer");

		int i = 0 ; 
		for (Document d : te.getDocuments())
			for (Sentence s : d.getParagraphs().get(0).getSentences())
			{
				postagger.tag(s);
//				if (i%100==0)
				System.out.println(i++);
			}

		for (Document d : tr.getDocuments())
			for (Sentence s : d.getParagraphs().get(0).getSentences())
			{
				postagger.tag(s);
//				if (i%100==0)
				System.out.println(i++);
			}
		
		te.store("src/main/resources/dataObjects/WCtest_POSEnhanced.data.bin");
		tr.store("src/main/resources/dataObjects/WCtrain_POSEnhanced.data.bin");
		
	}
}
