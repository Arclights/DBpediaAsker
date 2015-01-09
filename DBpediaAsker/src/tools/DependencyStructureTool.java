package tools;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class DependencyStructureTool {
	
	/**
	 * Returns a print-friendly version of a dependency structure
	 * 
	 * @param graph
	 *            - Dependency structure
	 * @return
	 */
	public static String graphToString(DependencyStructure graph) {
		StringBuilder out = new StringBuilder();
		try {
			out.append(graph);
			for (int i = 1; i <= graph.getHighestDependencyNodeIndex(); i++) {
				DependencyNode node;
				node = graph.getDependencyNode(i);
				if (node != null) {
					for (SymbolTable table : node.getLabelTypes()) {
						out.append(node.getLabelSymbol(table) + "\t");
					}
					if (node.hasHead()) {
						Edge e = node.getHeadEdge();
						out.append(e.getSource().getIndex() + "\t");
						if (e.isLabeled()) {
							for (SymbolTable table : e.getLabelTypes()) {
								out.append(e.getLabelSymbol(table) + "\t");
							}
						} else {
							for (SymbolTable table : graph
									.getDefaultRootEdgeLabels().keySet()) {
								out.append(graph
										.getDefaultRootEdgeLabelSymbol(table)
										+ "\t");
							}
						}
					}
					out.append('\n');
				}
			}
		} catch (MaltChainedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return out.toString();
	}

}
