package com.arclights.dbpediaasker;

import com.arclights.dbpediaasker.dbPedia.ParseDbPediaURIs;
import com.arclights.dbpediaasker.namedEnteties.ExtractNamedEnteties;
import com.arclights.dbpediaasker.namedEnteties.NamedEntities;
import com.arclights.dbpediaasker.namedEnteties.NamedEntity;
import com.arclights.dbpediaasker.serverInterpreter.GetDependencyStructure;
import com.arclights.dbpediaasker.serverInterpreter.GetTriples;
import com.arclights.dbpediaasker.serverInterpreter.ParseTagTranslations;
import com.arclights.dbpediaasker.serverInterpreter.ServerTagger;
import com.arclights.dbpediaasker.triple.Triple;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sparql.SPARQLRepository;
import se.su.ling.stagger.FormatException;
import se.su.ling.stagger.TagNameException;

public class Server {

	/**
	 * The server that waits for a question from the web interface Send back the
	 * answer if it finds one
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PrintWriter writer;
			System.out.println("Loading tagger...");
			ServerTagger tagger = new ServerTagger("configs/swedish.bin");
			System.out.println("Tagger loaded");

			HashMap<String, String> tagTrans = ParseTagTranslations.parse();

			HashMap<String, String> dbpediaURIs = ParseDbPediaURIs.parse();
			ServerSocket serverSocket = new ServerSocket(25003);
			System.out.println("Server up");
			while (true) {
				System.out.println("Waiting for connection...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connected");
				String answer = "Error: Could not compute the answer";

				try {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(clientSocket.getInputStream()));
					String question = br.readLine();
					System.out.println(question);
					writer = new PrintWriter("questionToTag.txt");
					writer.println(question);
					writer.close();

					System.out.println("Tagging question...");
					ArrayList<String> inputFiles = new ArrayList<>();
					inputFiles.add("questionToTag.txt");
					tagger.tag(inputFiles);
					System.out.println("Question tagged");

					NamedEntities NEs = ExtractNamedEnteties
							.extract("questionToTag.txt.conll");
					for (NamedEntity ne : NEs.values()) {
						ne.setIdentifiers(dbpediaURIs);
					}
					NEs.clearUp();
					System.out.println("Named entities extracted...");
					System.out.println("Named entities:");
					for (String key : NEs.keySet()) {
						System.out.println(key + "->" + NEs.get(key));
					}

					System.out.println("Retrieving dependency structure...");
					DependencyStructure ds = GetDependencyStructure
							.getStructure();
					System.out.println("Derpendency structure retreived");

					System.out.println("Creating triples...");
					ArrayList<Triple> triples = GetTriples.get(ds, NEs);

					for (Triple t : triples) {
						System.out.println(t);
					}

					answer = formatString(getAnswer(triples, tagTrans));

				} catch (MaltChainedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FormatException e) {
					e.printStackTrace();
				} catch (TagNameException e) {
					e.printStackTrace();
				}

				PrintWriter socketWriter = new PrintWriter(
						clientSocket.getOutputStream());
				socketWriter.println(answer);
				socketWriter.flush();

				clientSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Returns an answer for the question, if there is one, by first searching
	 * locally. If it doesn't find an answer there it proceeds to search DBpedia
	 *
	 * @param triples
	 *            - The incomplete triples that, when complete, can answer the
	 *            question
	 * @param tagTrans
	 *            - The hashmap with the tag -> label translations
	 * @return
	 */
	private static String getAnswer(ArrayList<Triple> triples,
			HashMap<String, String> tagTrans) {
		String answer = "Error: Doesn't contain any named entity";
		if (!triples.isEmpty()) {
			answer = searchlocalDb(triples.get(0));

			if (answer == null) {
				answer = searchDbPedia(triples.get(0), tagTrans);
			}

			if (answer == null) {
				answer = "I don't know the answer to that";
			}
		}

		return answer;
	}

	/**
	 * Searches the local database created by the question processor
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple to search for
	 * @return
	 */
	private static String searchlocalDb(Triple t) {
		Repository repo = new HTTPRepository(
				"http://aakerberg.net:8077/openrdf-sesame/", "solution");
		RepositoryConnection con = null;
		try {
			con = repo.getConnection();
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					getLocalDbQuery(t));
			System.out.println("Local Query: " + getLocalDbQuery(t));

			TupleQueryResult result = tupleQuery.evaluate();
			BindingSet bindingSet;
			if (result.hasNext()) {
				bindingSet = result.next();
				return bindingSet.getValue("name").toString();
			} else {
				tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
						getAltLocalDbQuery(t));
				System.out.println("Local Alternative Query: "
						+ getAltLocalDbQuery(t));
				result = tupleQuery.evaluate();
				if (result.hasNext()) {
					bindingSet = result.next();
					String[] res = bindingSet.getValue("o").toString()
							.split("/");
					return res[res.length - 1];
				}
			}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
				repo.shutDown();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Generates the query to search the local database and trying to get the
	 * potential name of the entity
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple
	 * @return
	 */
	private static String getLocalDbQuery(Triple t) {
		return "PREFIX tags:<http://aakerber.net/tags/>\nPREFIX dbp:<http://dbpedia.org/resource/>\nPREFIX dbpporp:<http://dbpedia.org/property/>\nPREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nselect ?name {"
				+ ((NamedEntity) t.getS()).getDbPediaURI().getQueryVersion()
				+ " <"
				+ t.getLabel()
				+ "> ?o .\n?o <http://dbpedia.org/property/name> ?name .}";
	}

	/**
	 * Generates the query to search the local database without getting the
	 * potential name of the entity
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple
	 * @return
	 */
	private static String getAltLocalDbQuery(Triple t) {
		return "PREFIX tags:<http://aakerber.net/tags/>\nPREFIX dbp:<http://dbpedia.org/resource/>\nPREFIX dbpporp:<http://dbpedia.org/property/>\nPREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nselect ?o {"
				+ ((NamedEntity) t.getS()).getDbPediaURI().getQueryVersion()
				+ " <" + t.getLabel() + "> ?o .}";
	}

	/**
	 * Searches DBpedia for an answer
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple
	 * @param tagTrans
	 *            - The tag -> label translations
	 * @return
	 */
	private static String searchDbPedia(Triple t,
			HashMap<String, String> tagTrans) {
		SPARQLRepository repo = new SPARQLRepository(
				"http://dbpedia.org/sparql/");
		TupleQueryResult result = null;
		try {
			repo.initialize();
			RepositoryConnection con = repo.getConnection();
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					getDbPediaQuery(t, tagTrans));
			System.out
					.println("Dbpedia Query: " + getDbPediaQuery(t, tagTrans));
			result = tupleQuery.evaluate();
			BindingSet bindingSet;
			if (result.hasNext()) {
				bindingSet = result.next();
				return bindingSet.getValue("name").toString();
			} else {
				tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
						getAltDbPediaQuery(t, tagTrans));
				System.out.println("Dbpedia Alternative Query: "
						+ getAltDbPediaQuery(t, tagTrans));
				result = tupleQuery.evaluate();
				if (result.hasNext()) {
					bindingSet = result.next();
					return bindingSet.getValue("name").toString();
				} else {
					tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
							getAlt2DbPediaQuery(t, tagTrans));
					System.out.println("Dbpedia Alternative Query: "
							+ getAlt2DbPediaQuery(t, tagTrans));
					result = tupleQuery.evaluate();
					if (result.hasNext()) {
						bindingSet = result.next();
						String[] res = bindingSet.getValue("o").toString()
								.split("/");
						return res[res.length - 1];
					}
				}
			}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (result != null)
					result.close();
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Generates the query to search DBpedia and trying to get the potential
	 * name of the entity
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple
	 * @param tagTrans
	 *            - The tag -> label translations
	 * @return
	 */
	private static String getDbPediaQuery(Triple t,
			HashMap<String, String> tagTrans) {
		return "select ?name {"
				+ ((NamedEntity) t.getS()).getDbPediaURI().getQueryVersion()
				+ " <"
				+ tagTrans.get(t.getLabel())
				+ "> ?o .\n?o <http://dbpedia.org/property/name> ?name .} LIMIT 1";
	}

	/**
	 * Generates the query to search DBpedia and trying to get the potential
	 * English name of the entity
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple
	 * @param tagTrans
	 *            - The tag -> label translations
	 * @return
	 */
	private static String getAltDbPediaQuery(Triple t,
			HashMap<String, String> tagTrans) {
		return "select ?name {"
				+ ((NamedEntity) t.getS()).getDbPediaURI().getQueryVersion()
				+ " <"
				+ tagTrans.get(t.getLabel())
				+ "> ?o .\n?o <http://dbpedia.org/property/enName> ?name .} LIMIT 1";
	}

	/**
	 * Generates the query to search DBpedia without getting the potential name
	 * of the entity
	 *
	 * @param t
	 *            - The com.arclights.dbpediaasker.triple
	 * @param tagTrans
	 *            - The tag -> label translations
	 * @return
	 */
	private static String getAlt2DbPediaQuery(Triple t,
			HashMap<String, String> tagTrans) {
		return "select ?o {"
				+ ((NamedEntity) t.getS()).getDbPediaURI().getQueryVersion()
				+ " <" + tagTrans.get(t.getLabel()) + "> ?o .} LIMIT 1";
	}

	/**
	 * Removes possible quotes from string. If there isn't any quotes the
	 * original string is returned. Else if the string is surrounded only by
	 * quotes, the quotes are removed, ie. "obi-wan kenobi". Else if the string
	 * is surrounded by quotes and a language marker, the marker and the quotes
	 * are remove, ie. "Moscow"@en.
	 *
	 * @param in
	 * @return
	 */
	private static String formatString(String in) {
		if (in != null && in.length() > 0) {
			if (in.charAt(0) == '\"') {
				if (in.charAt(in.length() - 1) == '\"') {
					return in.substring(1, in.length() - 1);
				}
				return in.substring(1, in.length() - 4);
			}
			in = in.replace('_', ' ');
		}
		return in;
	}

}
