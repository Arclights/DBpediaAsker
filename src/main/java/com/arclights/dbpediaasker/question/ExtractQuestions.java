package com.arclights.dbpediaasker.question;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;

public class ExtractQuestions {
	/**
	 * Extracts the questions and corresponding answers from the processed
	 * database result
	 * 
	 * @return
	 * @throws MaltChainedException
	 * @throws java.io.IOException
	 */
	public static ArrayList<Question> extract() throws MaltChainedException,
			IOException {
		System.out.println("Extracting questions...");
		MaltParserService service = new MaltParserService();

		// For some unknown reason, the mco-file can't be in a additional folder
		service.initializeParserModel("-c swemalt-1.7.2 -m parse -w . -lfi parser.log");

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream("RDF_output.txt.conll"), "UTF-8"));

		ArrayList<Question> questions = new ArrayList<Question>();

		String line = null;
		ArrayList<String> lines = new ArrayList<String>();

		int part = 0;
		// The name is not used in this implementation
		DependencyStructure name = null;
		DependencyStructure question = null;
		DependencyStructure answer = null;
		DependencyStructure altAnswer = null;
		String lastLine = " ";
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0) {
				if (part == 1 && lastLine.length() > 0
						&& lastLine.split("\t")[1].equals(".")) {
					// För att kunna läsa in frågor med punkter i
				} else {
					if (part == 0) {
						name = service.parse(lines.toArray(new String[lines
								.size()]));
					} else if (part == 1) {
						question = service.parse(lines.toArray(new String[lines
								.size()]));
					} else if (part == 2) {
						answer = service.parse(lines.toArray(new String[lines
								.size()]));
					} else if (part == 3) {
						altAnswer = service.parse(lines
								.toArray(new String[lines.size()]));
						Question quest = new Question(question, answer,
								altAnswer);
						questions.add(quest);
						part = -1;
					}
					part++;
					lines.clear();
				}
			} else {
				lines.add(line);
			}
			lastLine = line;
		}
		reader.close();
		return questions;
	}
}
