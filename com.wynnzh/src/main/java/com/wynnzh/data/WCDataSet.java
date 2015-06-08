package com.wynnzh.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import model.SentenceSample;
import edu.cmu.lti.weizh.docmodel.AbstractDataSet;
import edu.cmu.lti.weizh.docmodel.Document;
import edu.cmu.lti.weizh.docmodel.Paragraph;
import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.docmodel.Word;

public class WCDataSet extends AbstractDataSet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8141071233116291972L;

	public WCDataSet(int capacity, String evalType) {
		super(capacity, evalType);
	}

	static final String trainPath = "src/main/resources/annot/train/";
	static final String testPath = "src/main/resources/annot/test/";

	public static WCDataSet getUntaggedTrainingDataset() throws Exception {
		WCDataSet d = new WCDataSet(1000, "WebCrawlTrain data without labels.");
		fill(d, new File(trainPath));
		return d;
	}

	public static WCDataSet getTaggedTestDataset() throws Exception {
		WCDataSet d = new WCDataSet(1000, "WebCrawl test data labeled.");
		fill(d, new File(testPath));
		return d;
	}

	private static void fill(WCDataSet d, File node) throws Exception {

		// System.out.println(node.getAbsoluteFile());

		if (node.isDirectory()) {
			File[] subNote = node.listFiles();
			for (File f : subNote) {
				fill(d, f);
			}
		} else {
			Document doc = readAFileIntoDocument(node);
			if (doc != null)
				d.getDocuments().add(doc);
		}
	}

	private static Document readAFileIntoDocument(File node) throws Exception {
		Document d = new Document(node.getName().split("[.]")[0]);
//		System.out.println(d.getDocId());
		Paragraph p  = new Paragraph();
		d.addParagraph(p);
		if (node.exists() == false)
			return null;
		BufferedReader br = new BufferedReader(new FileReader(node));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] toks = line.split("\t");
			
			if (toks.length == 4) {
				Sentence s = new Sentence(toks[3]);
				List<Word> words = new ArrayList<Word>();
				while ((line = br.readLine())!=null && line.trim().length()!=0){
					String[] tks = line.trim().split("\t");
					if (tks.length==1){
						Word w = new Word(line);
						words.add(w);
					}else if (tks.length==2){
						Word w = new Word(tks[0]);
						w.setEntityType(tks[1]);
						words.add(w);
					}
				}
				s.setWords(words);
				d.getParagraphs().get(0).addSentence(s);
			}else{
//				System.out.println(toks.length);
			}
		}
		return d;
	}

	public static void main(String argb[]) throws Exception {
		WCDataSet d = WCDataSet.getTaggedTestDataset();
		WCDataSet dtrain = WCDataSet.getUntaggedTrainingDataset();
		for (Document doc : d.getDocuments()){
			for (Sentence sent : doc.getParagraphs().get(0).getSentences()){
				System.out.println(doc.getDocId()+"\t"+sent.getPlainSentence());
			}
		}
		d.store("src/main/resources/dataObjects/testStore");
	}
	
	public void store(String string) {
		try {
			FileOutputStream fileOut = new FileOutputStream(string);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.println("Model serialized to " + string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static WCDataSet  load(String string) {
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(string));
		} catch (IOException e) {
			e.printStackTrace();
		}
		WCDataSet t = null;
		try {
			t = (WCDataSet) is.readObject();
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

	public static List<SentenceSample> convertToSentenceMapEntryList(WCDataSet data) throws Exception {
		List<SentenceSample> idSentMap = new ArrayList<SentenceSample>(data.getDocuments().size()*10);
		for (Document d: data.getDocuments()	){
			for (Paragraph p : d.getParagraphs()){
				for (Sentence s : p.getSentences()){
				idSentMap.add(new SentenceSample(d.getDocId(),s));
				}
			}
		}
		return idSentMap;
	}


	
}
