package com.arclights.dbpediaasker.dbPedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBPediaURIExtracter {

	/**
	 * Extracts the URI's to an output file such that it is an direct
	 * translation from the swedish word to the DBpedia identifier without
	 * non-ASCII characters.
	 * 
	 * It uses two translations: Swedish word -> DBpedia identifier(with non-ASCII characters)
	 * DBpedia identifier(with non-ASCII characters) -> DBpedia identifier(without non-ASCII characters)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		HashMap<String, String> uris = new HashMap<>();
		try {
			System.out.println("Reading URIs...");
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					"configs/labels_en_uris_sv.ttl")));
			Pattern p = Pattern
					.compile(
							"<(.+)> <http://www\\.w3\\.org/2000/01/rdf-schema#label> \"(.+)\"",
							Pattern.UNICODE_CHARACTER_CLASS);
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					uris.put(m.group(2).toLowerCase(), m.group(1));
				}
			}
			reader.close();

			translateURIs(uris);

			PrintWriter writer = new PrintWriter("dbPediaURIs.txt");
			for (String key : uris.keySet()) {
				writer.println(key + "\t" + uris.get(key));
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Translates DBpedia identifiers with non-ASCII characters to identifiers without
	 * @param URIs - The swedish word -> DBpedia identifier(with non-ASCII characters) map
	 * @throws java.io.IOException
	 */
	private static void translateURIs(HashMap<String, String> URIs)
			throws IOException {
		System.out.println("Translating URIs...");
		HashMap<String, String> uriTanslations = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				"configs/iri_same_as_uri_en.ttl")));

		Pattern p = Pattern.compile(
				"<(.+)> <http://www.w3.org/2002/07/owl#sameAs> <(.+)>",
				Pattern.UNICODE_CHARACTER_CLASS);
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				uriTanslations.put(m.group(1), m.group(2));
			}
		}
		reader.close();

		for (String key : URIs.keySet()) {
			if (uriTanslations.containsKey(URIs.get(key))) {
				URIs.put(key, uriTanslations.get(URIs.get(key)));
			}
		}
	}

}
