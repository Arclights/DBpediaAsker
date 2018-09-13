package com.arclights.dbpediaasker.triple;

import com.arclights.dbpediaasker.namedEnteties.NamedEntities;
import com.arclights.dbpediaasker.namedEnteties.NamedEntity;
import com.arclights.dbpediaasker.question.Question;
import com.arclights.dbpediaasker.tools.DependencyNodeTools;
import com.arclights.dbpediaasker.tools.DependencyStructureTool;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateTriples {

	/**
	 * Generates triples from questions and answers. If first tries to find
	 * named entities in the answers. If there is none, it will try and find
	 * nouns instead
	 *
	 * @param questions
	 *            - The question structure
	 * @param NEs
	 *            - The named entities
	 * @param dbpediaURIs
	 *            - The DBpedia URI's
	 * @throws MaltChainedException
	 */
	public static void create(ArrayList<Question> questions, NamedEntities NEs,
                              HashMap<String, String> dbpediaURIs) throws MaltChainedException {

		System.out.println("Creating new triples...");
		for (Question question : questions) {
			ArrayList<NamedEntity> answerNamedEntities = getNamedEntities(
					question.getAnswer(), NEs);
			answerNamedEntities.addAll(getNamedEntities(
					question.getAltAnswer(), NEs));
			if (answerNamedEntities.isEmpty()) {
				NamedEntity noun = findNoun(question.getAnswer(), dbpediaURIs);
				if (noun != null) {
					answerNamedEntities.add(noun);
				}
				noun = findNoun(question.getAltAnswer(), dbpediaURIs);
				if (noun != null) {
					answerNamedEntities.add(noun);
				}
			}
			DependencyStructure questionStructure = question.getQuestion();
			ArrayList<Integer> visitedNodes = new ArrayList<>();
			for (int i = 1; i < questionStructure
					.getHighestDependencyNodeIndex(); i++) {
				if (DependencyNodeTools.getPosTag(
						questionStructure.getDependencyNode(i)).equals("PM")
						&& !visitedNodes.contains(i)) {
					ArrayList<DependencyNode> prevProperNouns = new ArrayList<>();
					visitedNodes.add(i);
					prevProperNouns.add(questionStructure.getDependencyNode(i));
					for (int j = i + 1; j < questionStructure
							.getHighestDependencyNodeIndex(); j++) {
						if (DependencyNodeTools.getPosTag(
								questionStructure.getDependencyNode(j)).equals(
								"PM")) {
							visitedNodes.add(j);
							prevProperNouns.add(questionStructure
									.getDependencyNode(j));
						} else {
							break;
						}
					}
					String entity = "";
					for (int j = 0; j < prevProperNouns.size(); j++) {
						entity += DependencyNodeTools.getLemma(prevProperNouns
								.get(j));
						if (j < prevProperNouns.size() - 1) {
							entity += " ";
						}
					}
					if (NEs.containsKey(entity.toLowerCase())) {
						NamedEntity questNe = NEs.get(entity.toLowerCase());
						String tag = findTag(prevProperNouns.get(0).getHead(),
								questionStructure);
						for (NamedEntity ne : answerNamedEntities) {
							Triple t = new Triple(questNe, new URI(
									"http://aakerber.net/tags/" + tag), ne);
							if (tag == null) {
								System.out.println(DependencyStructureTool
										.graphToString(questionStructure));
								System.out.println(t);
								System.out.println();
							}

							if (tag != null) {
								question.addTriple(t);
								questNe.addTriple(t);
							}
						}
					}

				}
			}
		}
	}

	/**
	 * Tries to find a noun in the dependency structure and packs it as a named
	 * entities
	 *
	 * @param ds
	 *            - Dependency structure
	 * @param dbpediaURIs
	 *            - The DBpedia URI's
	 * @return
	 * @throws MaltChainedException
	 */
	private static NamedEntity findNoun(DependencyStructure ds,
			HashMap<String, String> dbpediaURIs) throws MaltChainedException {
		for (int i = 0; i < ds.getHighestDependencyNodeIndex(); i++) {
			DependencyNode node = ds.getDependencyNode(i);
			if (node.isLabeled()
					&& DependencyNodeTools.getPosTag(node).equals("NN")) {
				NamedEntity newEntity = new NamedEntity("");
				newEntity.put(DependencyNodeTools.getLemma(node));
				newEntity.setIdentifiers(dbpediaURIs);
				if (newEntity.getDbPediaURI() == null) {
					newEntity = new NamedEntity("");
					newEntity.put(DependencyNodeTools.getForm(node));
					newEntity.setIdentifiers(dbpediaURIs);
				}
				return newEntity;
			}
		}
		return null;
	}

	/**
	 * Tries to find the named entities in the dependency structure
	 *
	 * @param ds
	 *            - Dependency structure
	 * @param NEs
	 *            - Named entities
	 * @return
	 * @throws MaltChainedException
	 */
	private static ArrayList<NamedEntity> getNamedEntities(
			DependencyStructure ds, NamedEntities NEs)
			throws MaltChainedException {
		ArrayList<NamedEntity> out = new ArrayList<>();
		ArrayList<Integer> visitedNodes = new ArrayList<>();
		for (int i = 1; i < ds.getHighestDependencyNodeIndex(); i++) {
			if (DependencyNodeTools.getPosTag(ds.getDependencyNode(i)).equals(
					"PM")
					&& !visitedNodes.contains(i)) {
				ArrayList<DependencyNode> prevProperNouns = new ArrayList<>();
				visitedNodes.add(i);
				prevProperNouns.add(ds.getDependencyNode(i));
				for (int j = i + 1; j < ds.getHighestDependencyNodeIndex(); j++) {
					if (DependencyNodeTools.getPosTag(ds.getDependencyNode(j))
							.equals("PM")) {
						visitedNodes.add(j);
						prevProperNouns.add(ds.getDependencyNode(j));
					} else {
						break;
					}
				}
				String entity = "";
				for (int j = 0; j < prevProperNouns.size(); j++) {
					entity += DependencyNodeTools.getLemma(prevProperNouns
							.get(j));
					if (j < prevProperNouns.size() - 1) {
						entity += " ";
					}
				}
				if (NEs.containsKey(entity.toLowerCase())) {
					out.add(NEs.get(entity.toLowerCase()));
				}

			}
		}
		return out;

	}

	/**
	 * Tries to find a noun by starting at a dependency node and working its way
	 * up the structure. If it finds none, it will try and go forward from the
	 * root instead.
	 *
	 * @param node
	 * @param ds
	 * @return
	 */
	public static String findTag(DependencyNode node, DependencyStructure ds) {
		try {
			if (node.isLabeled()) {
				if (DependencyNodeTools.getPosTag(node).equals("NN")) {
					return DependencyNodeTools.getLemma(node);
				}
				return findTag(node.getHead(), ds);
			} else {
				return findTagForward(ds.getDependencyRoot());
			}
		} catch (MaltChainedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Tries to find a noun beginning from the root
	 *
	 * @param node
	 *            - The dependency node
	 * @return
	 */
	private static String findTagForward(DependencyNode node) {
		try {
			if (node.isLabeled()
					&& DependencyNodeTools.getPosTag(node).equals("NN")) {
				return DependencyNodeTools.getLemma(node);

			}
		} catch (MaltChainedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (DependencyNode n : DependencyNodeTools.getDependents(node)) {
			String noun = findTagForward(n);
			if (noun != null) {
				return noun;
			}
		}

		return null;
	}

}
