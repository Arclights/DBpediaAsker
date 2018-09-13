package com.arclights.dbpediaasker.dbPedia;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import namedEnteties.NamedEntities;
import namedEnteties.NamedEntity;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

import question.Question;

import triple.Triple;

public class TranslateTags {
	private static String server = "http://dbpedia.org/sparql/";

	/**
	 * Tries to find the translations from the tags from our extractions with
	 * labels in DBpedia. It also calculates the coverage for the translation.
	 * 
	 * @param questions
	 *            - The questions with corresponding answers and extracted
	 *            triples with tags
	 * @param NEs
	 *            - The named entities found in all the questions
	 * @return - The tag to label translations
	 */
	public static HashMap<String, String> translate(
			ArrayList<Question> questions, NamedEntities NEs) {
		HashMap<String, String> translations = new HashMap<>();
		double nbrOfTriples = 0;
		double triplesTagCovered = 0;
		double triplesDBpediaCovered = 0;
		double questionsDBpediaCovered = 0;
		boolean questionDBpediaCovered = false;
		double questionsTagCovered = 0;
		boolean questionTagCovered = false;

		SPARQLRepository repo = new SPARQLRepository(server);
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("repo initialized");

		try {
			RepositoryConnection con = repo.getConnection();
			try {
				for (Question question : questions) {
					if (question.nbrOfTriples() > 0) {
						for (Triple t : question.getTriples()) {
							if (t.isValid()) {
								System.out.println(t);
								String res = performQuery(con,
										(NamedEntity) t.getS(),
										(NamedEntity) t.getO());
								if (res != null) {
									translations.put(t.getLabel(), res);
									triplesDBpediaCovered++;
									if (!questionDBpediaCovered) {
										questionDBpediaCovered = true;
										questionsDBpediaCovered++;
									}
								}
								triplesTagCovered++;
								if (!questionTagCovered) {
									questionsTagCovered++;
									questionTagCovered = true;
								}
							}
							nbrOfTriples++;
						}
					}
					questionDBpediaCovered = false;
					questionTagCovered = false;
				}
			} finally {
				con.close();
			}
		} catch (OpenRDFException e) {
		} finally {
			try {
				repo.shutDown();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		printCoverage(questions.size(), questionsTagCovered,
				questionsDBpediaCovered, nbrOfTriples, triplesTagCovered,
				triplesDBpediaCovered, NEs, questions);
		printTranslationsToFile(translations);
		return translations;
	}

	/**
	 * Performs the query to DBpedia
	 * 
	 * @param con
	 *            - The connection
	 * @param s
	 *            - The subject in the triple to match
	 * @param o
	 *            - The object in the triple to match
	 * @return - The predicate, if triple found
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private static String performQuery(RepositoryConnection con, NamedEntity s,
			NamedEntity o) throws RepositoryException, MalformedQueryException,
			QueryEvaluationException {
		String out = null;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				getQuery(s, o));
		TupleQueryResult result = tupleQuery.evaluate();
		System.out.println("Query: " + getQuery(s, o));
		try {
			BindingSet bindingSet;
			if (result.hasNext()) {
				bindingSet = result.next();
				Value p = bindingSet.getValue("p");
				out = p.stringValue();
			}
		} finally {
			result.close();
		}
		return out;
	}

	/**
	 * Generates the query for DBpedia
	 * 
	 * @param s
	 *            - The subject of the sought after triple
	 * @param o
	 *            - The object of the sought after triple
	 * @return - The query
	 */
	private static String getQuery(NamedEntity s, NamedEntity o) {
		return "SELECT ?p { " + s.getForQuery() + " ?p " + o.getForQuery()
				+ ".} LIMIT 1";
	}

	/**
	 * Prints the generated translations to a file
	 * 
	 * @param translations
	 *            - Translation hashmap
	 */
	private static void printTranslationsToFile(
			HashMap<String, String> translations) {
		try {
			PrintWriter writer = new PrintWriter("TagTranslations.txt");
			for (String key : translations.keySet()) {
				writer.println(key + ";" + translations.get(key));
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Prints the collected coverage to file
	 * 
	 * @param nbrOfQuestions
	 *            - The number of questions found in total
	 * @param questionsTagCovered
	 *            - The number of questions that has at least one triple that
	 *            has a tag
	 * @param questionsDBpediaCovered
	 *            - The number of questions that has at least one triple who's
	 *            tag has a corresponding label in DBpedia
	 * @param nbrOfTriples
	 *            - The total number of triples generated
	 * @param triplesTagCovered
	 *            - The number of triples that has a tag
	 * @param triplesDBpediaCovered
	 *            - The number of triples who's tag has a corresponding label in
	 *            DBpedia
	 * @param NEs
	 *            - The named entities found in all the questions
	 * @param questions
	 *            - The questions with corresponding answers and triples
	 */
	private static void printCoverage(int nbrOfQuestions,
			double questionsTagCovered, double questionsDBpediaCovered,
			double nbrOfTriples, double triplesTagCovered,
			double triplesDBpediaCovered, NamedEntities NEs,
			ArrayList<Question> questions) {
		try {
			PrintWriter writer = new PrintWriter("Coverage.txt");
			writer.println("Total number of questions: " + nbrOfQuestions);
			writer.println("Number of questions covered by tags: "
					+ questionsTagCovered);
			writer.println("Tag question coverage: "
					+ Math.round(questionsTagCovered / nbrOfQuestions * 100)
					+ "%");
			writer.println("Number of questions covered by DBpedia: "
					+ questionsDBpediaCovered);
			writer.println("DBpedia question coverage: "
					+ Math.round(questionsDBpediaCovered / nbrOfQuestions * 100)
					+ "%");
			writer.println();
			writer.println("Total number of triples: " + nbrOfTriples);
			writer.println("Number of triples covered by Tags: "
					+ triplesTagCovered);
			writer.println("Tag triple coverage: "
					+ Math.round(triplesTagCovered / nbrOfTriples * 100) + "%");
			writer.println("Number of triples covered by DBpedia: "
					+ triplesDBpediaCovered);
			writer.println("DBpedia triple coverage: "
					+ Math.round(triplesDBpediaCovered / nbrOfTriples * 100)
					+ "%");
			writer.println();
			writer.println("Percentage of named entities extracted as part of a tagged triple: "
					+ getNECoverage(NEs, questions) + "%");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the coverage of how many named entities belongs to at least
	 * one triple
	 * 
	 * @param NEs - The named entities
	 * @param questions - The questions with corresponding answers and triples
	 * @return
	 */
	private static long getNECoverage(NamedEntities NEs,
			ArrayList<Question> questions) {
		double nesCovered = 0;
		boolean foundCover = false;
		for (NamedEntity ne : NEs.values()) {
			for (Question q : questions) {
				for (Triple t : q.getTriples()) {
					if (t.isValid()
							&& (t.getS().equals(ne) || t.getO().equals(ne))) {
						nesCovered++;
						foundCover = true;
						break;
					}
				}
				if (foundCover) {
					break;
				}
			}
			foundCover = false;
		}

		return Math.round(nesCovered / NEs.size() * 100);
	}
}
