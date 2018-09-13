package com.arclights.dbpediaasker.question;

import java.util.ArrayList;

import com.arclights.dbpediaasker.namedEnteties.NamedEntity;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;

import com.arclights.dbpediaasker.tools.DependencyNodeTools;
import com.arclights.dbpediaasker.triple.Triple;

public class Question {
	/**
	 * Container for questions, their answers and their triples
	 */

	private String questionString;
	private DependencyStructure question;
	private String answerString;
	private DependencyStructure answer;
	private String altAnswerString;
	private DependencyStructure altAnswer;
	private ArrayList<Triple> triples;

	public Question(DependencyStructure questionIn,
			DependencyStructure answerIn, DependencyStructure altAnswerIn) {
		triples = new ArrayList<>();
		question = questionIn;
		answer = answerIn;
		altAnswer = altAnswerIn;
		setQuestionString(questionIn);
		setAnswerString(answerIn);
		setAltAnswerString(altAnswerIn);
	}

	/**
	 * Extracts the question as a string from the dependency structure
	 * 
	 * @param questionIn
	 */
	private void setQuestionString(DependencyStructure questionIn) {
		questionString = "";
		try {
			for (int i = 1; i < questionIn.getHighestDependencyNodeIndex() - 1; i++) {
				questionString += DependencyNodeTools.getForm(questionIn
						.getDependencyNode(i)) + " ";
			}
			questionString += DependencyNodeTools.getForm(questionIn
					.getDependencyNode(questionIn
							.getHighestDependencyNodeIndex() - 1));
			questionString += DependencyNodeTools.getForm(questionIn
					.getDependencyNode(questionIn
							.getHighestDependencyNodeIndex()));
		} catch (MaltChainedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Extracts the answer as a string from the dependency structure
	 * 
	 * @param questionIn
	 */
	private void setAnswerString(DependencyStructure answerIn) {
		answerString = "";
		try {
			for (int i = 1; i < answerIn.getHighestDependencyNodeIndex() - 1; i++) {
				answerString += DependencyNodeTools.getForm(answerIn
						.getDependencyNode(i)) + " ";
			}
			answerString += DependencyNodeTools
					.getForm(answerIn.getDependencyNode(answerIn
							.getHighestDependencyNodeIndex() - 1));
			answerString += DependencyNodeTools
					.getForm(answerIn.getDependencyNode(answerIn
							.getHighestDependencyNodeIndex()));
		} catch (MaltChainedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Extracts the alternative as a string from the dependency structure
	 * 
	 * @param questionIn
	 */
	private void setAltAnswerString(DependencyStructure altAnswerIn) {
		altAnswerString = "";
		if (altAnswerIn.getHighestDependencyNodeIndex() > 1) {
			try {
				for (int i = 1; i < altAnswerIn.getHighestDependencyNodeIndex() - 1; i++) {
					altAnswerString += DependencyNodeTools.getForm(altAnswerIn
							.getDependencyNode(i)) + " ";
				}
				altAnswerString += DependencyNodeTools.getForm(altAnswerIn
						.getDependencyNode(altAnswerIn
								.getHighestDependencyNodeIndex() - 1));
				altAnswerString += DependencyNodeTools.getForm(altAnswerIn
						.getDependencyNode(altAnswerIn
								.getHighestDependencyNodeIndex()));
			} catch (MaltChainedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds a com.arclights.dbpediaasker.triple
	 * 
	 * @param t
	 *            - Triple
	 */
	public void addTriple(Triple t) {
		if (!triples.contains(t)) {
			triples.add(t);
		}
	}

	/**
	 * Removes a com.arclights.dbpediaasker.triple
	 * 
	 * @param t
	 *            - Triple
	 */
	public void removeTriple(Triple t) {
		triples.remove(t);
	}

	/**
	 * Returns a list of all the triples generated for the question
	 * 
	 * @return
	 */
	public ArrayList<Triple> getTriples() {
		return triples;
	}

	/**
	 * Returns the number of triples generated
	 * 
	 * @return
	 */
	public int nbrOfTriples() {
		return triples.size();
	}

	/**
	 * Checks if the question contains at least one valid com.arclights.dbpediaasker.triple
	 * 
	 * @return
	 */
	public boolean hasTriples() {
		for (Triple t : triples) {
			if (t.isValid()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the "SQL" version of the data. (It's not real SQl, but it's a
	 * format that can be parsed easily and added to an SQL database)
	 * 
	 * @return
	 */
	public String toSQL() {
		String out = "";
		out += questionString;
		out += ";";
		out += answerString;
		out += ";";
		Triple t = null;
		for (Triple triple : triples) {
			if (triple.isValid()) {
				t = triple;
				break;
			}
		}
		if (t.getS() instanceof NamedEntity
				&& ((NamedEntity) t.getS()).getDbPediaURI() != null) {
			out += ((NamedEntity) t.getS()).getDbPediaURI();
		} else {
			out += " ";
		}
		out += ";";
		if (t.getO() instanceof NamedEntity
				&& ((NamedEntity) t.getO()).getDbPediaURI() != null) {
			out += ((NamedEntity) t.getO()).getDbPediaURI();
		} else {
			out += " ";
		}
		out += ";";
		out += t.getLabel();
		return out;
	}

	/**
	 * Returns the question as a dependency structure
	 * 
	 * @return
	 */
	public DependencyStructure getQuestion() {
		return question;
	}

	/**
	 * Returns the answer as a dependency structure
	 * 
	 * @return
	 */
	public DependencyStructure getAnswer() {
		return answer;
	}

	/**
	 * Returns the alternative answer as a dependency structure
	 * 
	 * @return
	 */
	public DependencyStructure getAltAnswer() {
		return altAnswer;
	}

	/**
	 * Returns the question as a string
	 * 
	 * @return
	 */
	public String getQuestionAsString() {
		return questionString;
	}

	/**
	 * Returns the answer as a string
	 * 
	 * @return
	 */
	public String getAnswerAsString() {
		return answerString;
	}

	/**
	 * Returns the alternative answer as a string
	 * 
	 * @return
	 */
	public String getAltAnswerAsString() {
		return altAnswerString;
	}

}
