package serverInterpreter;

import java.util.ArrayList;

import namedEnteties.NamedEntities;
import namedEnteties.NamedEntity;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

import tools.DependencyNodeTools;
import triple.CreateTriples;
import triple.Triple;
import triple.URI;

public class GetTriples {

	/**
	 * Generates triples from the dependency structure of the question
	 * 
	 * @param ds
	 *            - The dependency structure of the question
	 * @param NEs
	 *            - The named entities
	 * @return
	 * @throws MaltChainedException
	 */
	public static ArrayList<Triple> get(DependencyStructure ds,
			NamedEntities NEs) throws MaltChainedException {
		ArrayList<Triple> triples = new ArrayList<>();
		System.out.println("Creating triples...");
		ArrayList<Integer> visitedNodes = new ArrayList<>();
		for (int i = 1; i < ds.getHighestDependencyNodeIndex(); i++) {
			if (DependencyNodeTools.getPosTag(ds.getDependencyNode(i)).equals(
					"PM")
					&& !visitedNodes.contains(i)) {
				ArrayList<DependencyNode> prevProperNouns = new ArrayList<>();
				visitedNodes.add(i);
				prevProperNouns.add(ds.getDependencyNode(i));
				for (int j = i + 1; j <= ds.getHighestDependencyNodeIndex(); j++) {
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
					NamedEntity questNe = NEs.get(entity.toLowerCase());
					String tag = CreateTriples.findTag(prevProperNouns.get(0)
							.getHead(), ds);
					Triple t = new Triple(questNe, new URI(
							"http://aakerber.net/tags/" + tag),
							(NamedEntity) null);
					triples.add(t);
				}

			}
		}
		return triples;
	}

}
