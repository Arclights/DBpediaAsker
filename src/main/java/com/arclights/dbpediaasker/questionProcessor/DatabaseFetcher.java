package com.arclights.dbpediaasker.questionProcessor;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DatabaseFetcher {

	/**
	 * Fetches the data from the database and put it in the file RDF_output.txt in the following format:
	 * 
	 * .
	 * .
	 * .
	 * 
	 * <name of the question>
	 * <question>
	 * <answer>
	 * <alternative answer>
	 * 
	 * .
	 * .
	 * .
	 */
	public static void fetchFromDatabase() {
		String sesameServer = "http://server:8080/openrdf-sesame";
		String repositoryID = "DBpAsker";

		Repository repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			RepositoryConnection con = repo.getConnection();
			try {
				String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX kvitt:<http://cs.lth.se/ontologies/kvitt.owl#> SELECT ?name ?quest ?answer ?alt_answer WHERE { ?question kvitt:text ?quest . ?question kvitt:answer ?answer . ?question kvitt:alt_answer ?alt_answer . ?card kvitt:name ?name . ?card kvitt:questions ?question .}";
				TupleQuery tupleQuery = con.prepareTupleQuery(
						QueryLanguage.SPARQL, queryString);

				TupleQueryResult result = tupleQuery.evaluate();

				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
						"RDF_output.txt")));

				try {
					BindingSet bindingSet;
					while (result.hasNext()) {
						bindingSet = result.next();
						Value name = bindingSet.getValue("name");
						Value question = bindingSet.getValue("quest");
						Value answer = bindingSet.getValue("answer");
						Value altAnswer = bindingSet.getValue("alt_answer");

						bw.write(formatStringForWrite(name.toString()));
						bw.write(formatStringForWrite(question.toString()));
						bw.write(formatStringForWrite(answer.toString()));
						bw.write(formatStringForWrite(altAnswer.toString()));
						bw.write("\n");
					}

					// do something interesting with the values here...
				} finally {
					result.close();
					bw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (OpenRDFException e) {
			// handle exception
		} finally {
			try {
				repo.shutDown();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Formats incoming string from the database into proper format
	 * 
	 * @param in
	 *            - String from database
	 * @return - Formatted string
	 */
	private static String formatStringForWrite(String in) {
		String out=in.replace("(", "").replace(")", "").replace("\"", "");
		out = removeDotsInBeginning(out);
		if (out.length() == 0
				|| !Character.toString(out.charAt(out.length() - 1)).matches(
						"[.!?]")) {
			out += ".";
		}
		return out+"#\n";
	}

	/**
	 * If the string from the databse contains dots in the beginning, e.g.
	 * ...hello, they will be removed
	 * 
	 * @param in
	 *            - String from databse
	 * @return
	 */
	private static String removeDotsInBeginning(String in) {
		if (in.length() > 1 && in.charAt(0) == '.') {
			return removeDotsInBeginning(in.substring(1, in.length()));
		}
		return in;
	}

	public static void main(String[] args) {
		fetchFromDatabase();
	}
}
