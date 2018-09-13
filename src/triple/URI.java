package triple;

import java.util.HashMap;

public class URI {
	/**
	 * Represents an URI
	 */

	static HashMap<String, String> prefixCovert;

	static {
		prefixCovert = new HashMap<>();
		prefixCovert.put("http://www.w3.org/1999/02/22-rdf-syntax-ns", "rdf");
		prefixCovert.put("http://dbpedia.org/resource/", "dbp");
		prefixCovert.put("http://dbpedia.org/ontology/", "dbpont");
		prefixCovert.put("http://dbpedia.org/property/", "dbpporp");
		prefixCovert.put("http://aakerber.net/tags/", "tags");
	}

	String baseURI;
	String extensionURI;
	String fullURI;

	public URI(String uri) {
		String[] parts = splitURI(removeQuoteMarks(uri));
		baseURI = parts[0];
		extensionURI = parts[1];
		// fullURI = removeQuoteMarks(uri);
	}

	/**
	 * Returns full URI
	 * 
	 * @return
	 */
	public String getFullURI() {
		// return fullURI;
		return toString();
	}

	/**
	 * Return the first part of the URi, e.g. http://dbpedia.org/resource/
	 * 
	 * @return
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * Returns the URI for use in a Turtle file
	 * 
	 * @return
	 */
	public String getTurtleURI() {
		if (prefixCovert.get(baseURI) == null) {
			System.out.println(baseURI);
		}
		return prefixCovert.get(baseURI)
				+ ":"
				+ extensionURI.replace("(", "\\(").replace(")", "\\)")
						.replace("&", "\\&").replace(".", "\\.")
						.replace("'", "\\'").replace(",", "\\,");
	}

	/**
	 * Removes the quotation marks from a string
	 * 
	 * @param in
	 *            - string
	 * @return
	 */
	private String removeQuoteMarks(String in) {
		if (in != null && in.length() > 0) {
			if (in.charAt(0) == '\'') {
				return in.substring(1, in.length() - 1);
			}
		}
		return in;
	}

	/**
	 * Splits URI to get the base part and extension part
	 * 
	 * @param uri
	 * @return
	 */
	private String[] splitURI(String uri) {
		if (uri.charAt(0) == '<') {
			uri = uri.substring(1, uri.length() - 1);
		}
		String[] out = uri.split("#");
		if (out.length > 1) {
			return out;
		}
		out = new String[2];
		String[] split = uri.split("/");
		out[1] = split[split.length - 1];
		out[0] = uri.substring(0, uri.length() - out[1].length());
		return out;
	}

	@Override
	public String toString() {
		// return fullURI;
		return baseURI + extensionURI;
	}

	public String getQueryVersion() {
		return "<" + toString() + ">";
	}

}
