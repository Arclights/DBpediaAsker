package serverInterpreter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ParseTagTranslations {

	/**
	 * Recreates the hashmap containing the tag -> label translations generated
	 * by the question processor
	 * 
	 * @return
	 * @throws java.io.IOException
	 */
	public static HashMap<String, String> parse() throws IOException {
		HashMap<String, String> translations = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(
				"TagTranslations.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(";");
			translations.put(parts[0], parts[1]);
		}
		reader.close();
		return translations;
	}

}
