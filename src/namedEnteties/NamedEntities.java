package namedEnteties;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class NamedEntities extends HashMap<String, NamedEntity> {

	/**
	 * Container for named entities
	 */
	private static final long serialVersionUID = -1354921555138905214L;

	public NamedEntities() {

	}

	public void put(NamedEntity ne) {
		put(ne.getName(), ne);
	}

	/**
	 * Clears out named entities that hasn't got a URI from DBpedia
	 */
	public void clearUp() {
		ArrayList<String> keysToRemove = new ArrayList<>();
		for (String key : this.keySet()) {
			if (this.get(key).getDbPediaURI() == null) {
				keysToRemove.add(key);
			}
		}
		for (String key : keysToRemove) {
			this.remove(key);
		}
	}

	/**
	 * Prints the triples for each named entity covered by DBpedia to a file
	 * that can be loaded into a RDF database
	 */
	public void printToTurtleFile() {
		try {
			PrintWriter writer = new PrintWriter("triples.ttl");
			writer.println("@PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.");
			writer.println("@PREFIX dbp: <http://dbpedia.org/resource/>.");
			writer.println("@PREFIX tags: <http://aakerber.net/tags/>.");
			writer.println("@PREFIX dbpporp: <http://dbpedia.org/property/>.");
			for (NamedEntity ne : values()) {
				writer.println(ne.toTurtleString());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
