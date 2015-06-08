package com.wynnzh.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.wynnzh.OPENNLP;

public class DataPurifierTweets {

	public static void main(String argv[]) throws IOException {

		String filepath = "/home/wei/git/ActiveLearning4TempGeoDisambiguation/com.wynnzh/files/";
		String fileName = "Onto-Readable.csv";
		String phrase = "onto";
		BufferedReader br = new BufferedReader(new FileReader(new File(filepath + fileName)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filepath + "filtering/" + fileName)));

		String line = null;
		int lineNo = -1;
		StringBuffer outString = new StringBuffer();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			String sentence = line.split("\t")[3];
			String lowered = sentence.toLowerCase();
			List<String> tokens = OPENNLP.getTokensAsStringList(lowered);
			if (tokens.contains(phrase.toLowerCase())) {
				bw.write(line);
				bw.write("\n");
			}
		}
		bw.close();
	}

}
