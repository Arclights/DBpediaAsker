package tools;

import java.util.TreeSet;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class DependencyNodeTools {

	/**
	 * 
	 * Returns the form of a node in a dependency structure
	 * 
	 * @param node
	 *            - Dependency node
	 * @return
	 * @throws MaltChainedException
	 */
	public static String getForm(DependencyNode node)
			throws MaltChainedException {
		return node
				.getLabelSymbol((SymbolTable) node.getLabelTypes().toArray()[1]);
	}

	/**
	 * Return the lemma of a node in a dependency structure
	 * 
	 * @param node
	 *            - Dependency node
	 * @return
	 * @throws MaltChainedException
	 */
	public static String getLemma(DependencyNode node)
			throws MaltChainedException {
		return node
				.getLabelSymbol((SymbolTable) node.getLabelTypes().toArray()[2]);
	}

	/**
	 * Return the label of the head of a node in a dependency structure
	 * 
	 * @param node
	 *            - Dependency node
	 * @return
	 * @throws MaltChainedException
	 */
	public static String getHeadLabel(DependencyNode node)
			throws MaltChainedException {
		Edge e = node.getHeadEdge();
		if (node.hasHead()) {
			if (e.isLabeled()) {
				String out = "";
				for (SymbolTable table : e.getLabelTypes()) {
					out += e.getLabelSymbol(table) + " ";
				}
				return out;
			}
		}
		return "bla";
	}

	/**
	 * Return the POStag of a node in a dependency structure
	 * 
	 * @param node
	 *            - Dependency node
	 * @return
	 * @throws MaltChainedException
	 */
	public static String getPosTag(DependencyNode node)
			throws MaltChainedException {
		return node
				.getLabelSymbol((SymbolTable) node.getLabelTypes().toArray()[4]);
	}

	/**
	 * Returns the dependents of a dependency node in a dependency structure
	 * 
	 * @param in
	 *            - Depedendency node
	 * @return
	 */
	public static TreeSet<DependencyNode> getDependents(DependencyNode in) {
		TreeSet<DependencyNode> dependents = new TreeSet<>();
		dependents.addAll(in.getLeftDependents());
		dependents.addAll(in.getRightDependents());
		return dependents;
	}

}
