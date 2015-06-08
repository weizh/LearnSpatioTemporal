package com.wynnzh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

public class OPENNLP {

	private static SentenceDetectorME sdetector = null;
	private static Tokenizer tokenizer = null;
	private static POSTaggerME postagger = null;
	private static ChunkerME chunker = null;

	static Tokenizer getTokenizer() {
		if (tokenizer == null) {
			InputStream tokis = null;
			try {
				tokis = new FileInputStream("/home/wei/Documents/activeLearning/software/en-token.bin");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TokenizerModel model = null;
			try {
				model = new TokenizerModel(tokis);
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tokenizer = new TokenizerME(model);
		}
		return tokenizer;
	}

	static SentenceDetectorME getSentenceDetector() {
		if (sdetector == null) {
			InputStream sentis = null;
			try {
				sentis = new FileInputStream("/home/wei/Documents/activeLearning/software/en-sent.bin");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SentenceModel sentmodel = null;
			try {
				sentmodel = new SentenceModel(sentis);
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sdetector = new SentenceDetectorME(sentmodel);
		}
		return sdetector;
	}

	static POSTaggerME getPOSTagger() {
		if (postagger == null) {
			POSModel posmodel = new POSModelLoader().load(new File(
					"/home/wei/Documents/activeLearning/software/en-pos-maxent.bin"));
			postagger = new POSTaggerME(posmodel);
		}
		return postagger;
	}

	static ChunkerME getChunker() {
		if (chunker == null) {
			InputStream is = null;
			try {
				is = new FileInputStream("/home/wei/Documents/activeLearning/software/en-chunker.bin");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ChunkerModel cModel = null;
			try {
				cModel = new ChunkerModel(is);
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			chunker = new ChunkerME(cModel);
		}
		return chunker;
	}

	public static Span[] getSentences(String s) {
		return OPENNLP.getSentenceDetector().sentPosDetect(s);
	}

	public static Span[] getTokens(String s) {
		return OPENNLP.getTokenizer().tokenizePos(s);
	}

	public static List<String> getTokensAsStringList(String s){
		Span[] spantokens = getTokens(s);
		List<String> tokens = new ArrayList<String>();
		for (Span span : spantokens){
			tokens.add(s.substring(span.getStart(), span.getEnd()));
		}
		return tokens;
	}
	/**
	 * 
	 * Implicit tokenizer used. POSTagger has to use String[] tokens.
	 * 
	 * @param s
	 * @return
	 */
	public static String[] getPOS(String s) {
		return OPENNLP.getPOSTagger().tag(tokenizer.tokenize(s));
	}

	public static void main(String argv[]) throws IOException {
		// String input = argv[0];
		String input = "Will This-AM work?";

		System.out.println(getTokensAsStringList(input));
		
		SentenceDetectorME sdetector = OPENNLP.getSentenceDetector();
		Tokenizer tokenizer = OPENNLP.getTokenizer();
		POSTaggerME postagger = OPENNLP.getPOSTagger();

		// chunker
		InputStream is = new FileInputStream("/home/wei/Documents/activeLearning/software/en-chunker.bin");
		ChunkerModel cModel = new ChunkerModel(is);
		ChunkerME chunkerME = new ChunkerME(cModel);

		// // NE finders
		// InputStream locis = new
		// FileInputStream("/home/wei/Documents/activeLearning/software/en-ner-location.bin");
		// TokenNameFinderModel locner = new TokenNameFinderModel(locis);
		// NameFinderME locfinder = new NameFinderME(locner);
		//
		// InputStream dateis = new
		// FileInputStream("/home/wei/Documents/activeLearning/software/en-ner-date.bin");
		// TokenNameFinderModel datener = new TokenNameFinderModel(dateis);
		// NameFinderME datefinder = new NameFinderME(datener);
		//
		// InputStream timeis = new
		// FileInputStream("/home/wei/Documents/activeLearning/software/en-ner-time.bin");
		// TokenNameFinderModel timener = new TokenNameFinderModel(timeis);
		// NameFinderME timefinder = new NameFinderME(timener);
		/**
		 * Pipeline starts.
		 */

		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		perfMon.start();

		String sentences[] = sdetector.sentDetect(input);
		for (String sentence : sentences) {

			System.out.println("Sentence is :" + sentence);
			String tokens[] = tokenizer.tokenize(sentence);
			String[] tags = postagger.tag(tokens);
			// String result[] = chunkerME.chunk(tokens, tags);
			// for (String s : result)
			// System.out.println(s);
			String[] span = chunkerME.chunk(tokens, tags);
			for (String s : span)
				System.out.println(s);

			// Span locs[] = locfinder.find(tokens);
			// for(Span s: locs)
			// System.out.println(s.toString());
			//
			// Span dates[] = datefinder.find(tokens);
			// for(Span s: dates)
			// System.out.println(s.toString());
			//
			// Span times[] = timefinder.find(tokens);
			// for(Span s: times)
			// System.out.println(s.toString());

			POSSample sample = new POSSample(tokens, tags);
			System.out.println(sample.toString());

			perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();

	}

}
