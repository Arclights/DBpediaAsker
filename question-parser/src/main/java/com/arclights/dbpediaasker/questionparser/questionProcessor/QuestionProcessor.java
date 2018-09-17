package com.arclights.dbpediaasker.questionparser.questionProcessor;

import com.arclights.dbpediaasker.commons.NamedEntities;
import com.arclights.dbpediaasker.commons.NamedEntity;
import com.arclights.dbpediaasker.commons.ParseDbPediaURIs;
import com.arclights.dbpediaasker.commons.question.Question;
import com.arclights.dbpediaasker.commons.triple.CreateTriples;
import com.arclights.dbpediaasker.questionparser.dbPedia.TranslateTags;
import com.arclights.dbpediaasker.questionparser.namedEnteties.ExtractNamedEntetiesOld;
import com.arclights.dbpediaasker.questionparser.question.ExtractQuestions;
import com.arclights.dbpediaasker.questionparser.question.QuestionHandler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import org.maltparser.core.exception.MaltChainedException;

public class QuestionProcessor {

	/**
	 * Analyzes the questions See paper
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			HashMap<String, String> dbpediaURIs = ParseDbPediaURIs.parse();
			NamedEntities NEs = ExtractNamedEntetiesOld
					.extract("RDF_output.txt.conll");
			for (NamedEntity ne : NEs.values()) {
				ne.setIdentifiers(dbpediaURIs);
			}
			ArrayList<Question> questions = ExtractQuestions.extract();

			CreateTriples.create(questions, NEs, dbpediaURIs);

			TranslateTags.translate(questions, NEs);

			QuestionHandler.printQuestionsToSQL(questions);

			NEs.clearUp();
			NEs.printToTurtleFile();

		} catch (MaltChainedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
