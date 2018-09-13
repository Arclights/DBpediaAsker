package com.arclights.dbpediaasker.serverInterpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;

import com.arclights.dbpediaasker.tools.DependencyStructureTool;

public class GetDependencyStructure {

	/**
	 * Processes the question to answer and returns the dependency structure
	 * 
	 * @return
	 * @throws MaltChainedException
	 * @throws java.io.IOException
	 */
	public static DependencyStructure getStructure()
			throws MaltChainedException, IOException {
		System.out.println("Extracting question...");
		MaltParserService service = new MaltParserService();
		service.initializeParserModel("-c swemalt-1.7.2 -m parse -w . -lfi parser.log");

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream("questionToTag.txt.conll"), "UTF-8"));
		DependencyStructure out = null;
		String line = null;
		String lastLine = " ";
		ArrayList<String> words = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0) {
				if (lastLine.length() > 0
						&& lastLine.split("\t")[1].equals(".")) {
					// För att kunna läsa in frågor med punkter i
				} else {
					out = service
							.parse(words.toArray(new String[words.size()]));
					break;
				}
			} else {
				words.add(line);
			}
			lastLine = line;
		}
		reader.close();
		System.out.println(DependencyStructureTool.graphToString(out));
		return out;
	}
}
