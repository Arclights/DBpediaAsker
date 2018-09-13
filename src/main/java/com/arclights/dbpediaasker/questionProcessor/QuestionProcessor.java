package com.arclights.dbpediaasker.questionProcessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import namedEnteties.ExtractNamedEnteties;
import namedEnteties.NamedEntities;
import namedEnteties.NamedEntity;

import org.maltparser.core.exception.MaltChainedException;

import question.ExtractQuestions;
import question.Question;
import question.QuestionHandler;
import triple.CreateTriples;
import dbPedia.ParseDbPediaURIs;
import dbPedia.TranslateTags;

public class QuestionProcessor {

	/**
	 * Analyzes the questions See paper
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			HashMap<String, String> dbpediaURIs = ParseDbPediaURIs.parse();
			NamedEntities NEs = ExtractNamedEnteties
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
