package com.wynnzh;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import edu.cmu.lti.weizh.docmodel.Sentence;

public class Utils {
	public static List<Sentence> getSentences(DataSet d) {
		List<Sentence> sent = new ArrayList<Sentence>();
		for (Document doc : d.getDocuments())
			try {
				for (Paragraph p : doc.getParagraphs()){
					sent.addAll(p.getSentences());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return sent;
	}
}
