package namedEnteties;

import java.util.ArrayList;
import java.util.HashMap;

import triple.Triple;
import triple.URI;

public class NamedEntity implements Comparable<NamedEntity> {
	/**
	 * Represents a named entity
	 */
	ArrayList<String> words;
	String type;
	URI dbPediaURI;
	String wikiDataResource;
	ArrayList<Triple> triples;
	NamedEntities NEs;

	public NamedEntity(String type) {
		words = new ArrayList<>();
		this.type = type;
		triples = new ArrayList<>();
	}

	/**
	 * Adds the words that make out the named entity
	 * 
	 * @param word
	 */
	public void put(String word) {
		words.add(word);
	}

	/**
	 * Adds triple that is related to the named entity
	 * 
	 * @param t
	 *            - The triple
	 */
	public void addTriple(Triple t) {
		if (!triples.contains(t)) {
			triples.add(t);
		}
	}

	/**
	 * Removes a certain triple from the named entity
	 * 
	 * @param t
	 *            - The triple
	 */
	public void removeTriple(Triple t) {
		triples.remove(t);
	}

	/**
	 * Creates a triple that links the named entity with the name of the named
	 * entity
	 * 
	 * @param labels
	 *            - The labels from DBpedia
	 */
	public void setIdentifiers(HashMap<String, String> labels) {
		extractDbPediaURI(labels);
		Triple t = new Triple(this, new URI(
				"<http://dbpedia.org/property/name>"), getName());
		triples.add(t);
	}

	/**
	 * Tries to find the DBpedia URI for the named entity
	 * 
	 * @param labels
	 *            - The labels from DBpedia
	 */
	private void extractDbPediaURI(HashMap<String, String> labels) {
		if (labels.containsKey(getName())) {
			dbPediaURI = new URI("<" + labels.get(getName()) + ">");
		}
	}

	/**
	 * Returns the DBpedia URI
	 * 
	 * @return
	 */
	public URI getDbPediaURI() {
		return dbPediaURI;
	}

	/**
	 * Returns the Turtle-file representation of the named entity
	 * 
	 * @return
	 */
	public String toTurtleString() {
		String out = "";
		if (dbPediaURI != null) {
			out = dbPediaURI.getTurtleURI() + "\n";
			for (int i = 0; i < triples.size(); i++) {
				if (triples.get(i).isValid()) {
					out += "\t" + triples.get(i).secondHalfToTurtleString();
					if (i < triples.size() - 1) {
						out += ";\n";
					}
				}
			}
			out += " .\n\n";
		}
		return out;
	}

	/**
	 * Returns the name of the named entity.
	 * 
	 * @return
	 */
	public String getName() {
		String out = "";
		for (int i = 0; i < words.size(); i++) {
			out += words.get(i).toLowerCase();
			if (i < words.size() - 1) {
				out += " ";
			}
		}
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		for (String word : words) {
			out += word + " ";
		}
		return out + "| " + type + " | " + dbPediaURI;
	}

	/**
	 * Returns the query representation
	 * 
	 * @return
	 */
	public String getForQuery() {
		if (dbPediaURI == null) {
			return "<null>";
		}
		return dbPediaURI.getQueryVersion();
	}

	@Override
	public int compareTo(NamedEntity ne) {
		return this.getName().compareTo(ne.getName());
	}
}
