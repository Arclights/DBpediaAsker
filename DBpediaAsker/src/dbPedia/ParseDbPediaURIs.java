package dbPedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ParseDbPediaURIs {

	/**
	 * Recreates the hashmap created by the DBPediaURIExtracter
	 * @return
	 */
	public static HashMap<String, String> parse() {
		System.out.println("Loading dbPedia URI's...");
		HashMap<String, String> out = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					"dbPediaURIs.txt")));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\t");
				out.put(parts[0], parts[1]);
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
