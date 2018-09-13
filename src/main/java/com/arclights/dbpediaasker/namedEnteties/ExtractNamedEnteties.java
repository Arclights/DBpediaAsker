package com.arclights.dbpediaasker.namedEnteties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ExtractNamedEnteties {

	/**
	 * Extracts the named entities found by Stagger
	 * 
	 * @param fileName
	 *            - Stagger output
	 * @return - -The named entities
	 */
	public static NamedEntities extract(String fileName) {
		System.out.println("Extracting named entities...");
		NamedEntities out = new NamedEntities();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					fileName)));
			String line = reader.readLine();
			while (line != null) {
				String[] parts = line.split("\t");
				boolean readLine = false;
				if (parts.length > 1) {
					if (parts[10].equals("B")) {
						NamedEntity ne = new NamedEntity(parts[11]);
						ne.put(parts[2]);
						while ((line = reader.readLine()) != null) {
							readLine = true;
							parts = line.split("\t");
							if (parts.length > 1 && parts[10].equals("I")) {
								ne.put(parts[2]);
							} else {
								break;
							}
						}
						out.put(ne);
					}
				}
				if (!readLine) {
					line = reader.readLine();
				}
			}

			reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out;
	}

}
