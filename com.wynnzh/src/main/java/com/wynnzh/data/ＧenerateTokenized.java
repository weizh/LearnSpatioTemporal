package com.wynnzh.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import com.wynnzh.OPENNLP;

public class ＧenerateTokenized {

	public static void main(String argv[]) throws IOException {

		Random r = new Random();
		File folder = new File("files");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(".csv")) {
				List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());
				String name = file.getName();
				PrintWriter writertrain = new PrintWriter("files/annot/train/" + name.substring(0, name.length()-4) + ".train", "UTF-8");
				PrintWriter writertest = new PrintWriter("files/annot/test/" +  name.substring(0, name.length()-4) + ".test", "UTF-8");
				for (String line : lines) {

					String[] items = line.split("\t");
					List<String> tokens = OPENNLP.getTokensAsStringList(items[3]);
					if (r.nextDouble() < 0.95) {
						writertrain.println(line);
						for (String token : tokens)
							writertrain.println(token + "\t");
						writertrain.println();
					} else {
						writertest.println(line);
						for (String token : tokens)
							writertest.println(token + "\t");
						writertest.println();
					}
				}
				writertrain.close();
				writertest.close();
			}
		}
	}
}