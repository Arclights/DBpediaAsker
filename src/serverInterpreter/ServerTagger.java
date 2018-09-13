package serverInterpreter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import se.su.ling.stagger.EnglishTokenizer;
import se.su.ling.stagger.Evaluation;
import se.su.ling.stagger.FormatException;
import se.su.ling.stagger.LatinTokenizer;
import se.su.ling.stagger.SwedishTokenizer;
import se.su.ling.stagger.TagNameException;
import se.su.ling.stagger.TaggedToken;
import se.su.ling.stagger.Tagger;
import se.su.ling.stagger.Token;
import se.su.ling.stagger.Tokenizer;

public class ServerTagger {
	/**
	 * Hacked version of Stagger that preloads Stagger so it doesn't have to be
	 * loaded for every question
	 */
	private String lang;
	Tagger tagger;
	TaggedToken[][] inputSents = null;
	boolean hasNE = true;
	boolean extendLexicon = true;
	boolean preserve = false;
	boolean plainOutput = false;

	public ServerTagger(String modelFile) {
		ObjectInputStream modelReader;
		try {
			modelReader = new ObjectInputStream(new FileInputStream(modelFile));
			System.err.println("Loading Stagger model ...");
			tagger = (Tagger) modelReader.readObject();
			lang = tagger.getTaggedData().getLanguage();
			modelReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Performs the tagging on a question. Because of Staggers structure, the
	 * question has to be in a file that has to be in a list. I then prints the
	 * result to a file.
	 * 
	 * @param inputFiles
	 *            - The question in a file in an ArrayList
	 * @throws FormatException
	 * @throws TagNameException
	 * @throws java.io.IOException
	 */
	public void tag(ArrayList<String> inputFiles) throws FormatException,
			TagNameException, IOException {

		tagger.setExtendLexicon(extendLexicon);
		if (!hasNE)
			tagger.setHasNE(false);

		for (String inputFile : inputFiles) {
			if (!(inputFile.endsWith(".txt") || inputFile.endsWith(".txt.gz"))) {
				inputSents = tagger.getTaggedData().readConll(inputFile, null,
						true, !inputFile.endsWith(".conll"));
				Evaluation eval = new Evaluation();
				int count = 0;
				for (TaggedToken[] sent : inputSents) {
					if (count % 100 == 0)
						System.err
								.print("Tagging sentence nr: " + count + "\r");
					count++;
					TaggedToken[] taggedSent = tagger.tagSentence(sent, true,
							preserve);

					eval.evaluate(taggedSent, sent);
					tagger.getTaggedData().writeConllGold(System.out,
							taggedSent, sent, plainOutput);
				}
				System.err.println("Tagging sentence nr: " + count);
				System.err.println("POS accuracy: " + eval.posAccuracy() + " ("
						+ eval.posCorrect + " / " + eval.posTotal + ")");
				System.err.println("NE precision: " + eval.nePrecision());
				System.err.println("NE recall:    " + eval.neRecall());
				System.err.println("NE F-score:   " + eval.neFscore());
			} else {
				String fileID = (new File(inputFile)).getName().split("\\.")[0];
				BufferedReader reader = openUTF8File(inputFile);
				BufferedWriter writer = null;
				if (inputFiles.size() > 0) {
					String outputFile = inputFile
							+ (plainOutput ? ".plain" : ".conll");
					writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outputFile), "UTF-8"));
				}
				Tokenizer tokenizer = getTokenizer(reader, lang);
				ArrayList<Token> sentence;
				int sentIdx = 0;
				while ((sentence = tokenizer.readSentence()) != null) {
					TaggedToken[] sent = new TaggedToken[sentence.size()];
					if (tokenizer.sentID != null) {
						if (!fileID.equals(tokenizer.sentID)) {
							fileID = tokenizer.sentID;
							sentIdx = 0;
						}
					}
					for (int j = 0; j < sentence.size(); j++) {
						Token tok = sentence.get(j);
						String id;
						id = fileID + ":" + sentIdx + ":" + tok.offset;
						sent[j] = new TaggedToken(tok, id);
					}
					TaggedToken[] taggedSent = tagger.tagSentence(sent, true,
							false);
					tagger.getTaggedData().writeConllSentence(
							(writer == null) ? System.out : writer, taggedSent,
							plainOutput);
					sentIdx++;
				}
				tokenizer.yyclose();
				if (writer != null)
					writer.close();
			}
		}
	}

	/**
	 * A function from Stagger
	 * 
	 * @param name
	 * @return
	 * @throws java.io.IOException
	 */
	private static BufferedReader openUTF8File(String name) throws IOException {
		if (name.equals("-"))
			return new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		else if (name.endsWith(".gz"))
			return new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(name)), "UTF-8"));
		return new BufferedReader(new InputStreamReader(new FileInputStream(
				name), "UTF-8"));
	}

	/**
	 * Creates and returns a tokenizer for the given language.
	 * 
	 * @param reader
	 * @param lang
	 * @return
	 */
	private static Tokenizer getTokenizer(Reader reader, String lang) {
		Tokenizer tokenizer;
		if (lang.equals("sv")) {
			tokenizer = new SwedishTokenizer(reader);
		} else if (lang.equals("en")) {
			tokenizer = new EnglishTokenizer(reader);
		} else if (lang.equals("any")) {
			tokenizer = new LatinTokenizer(reader);
		} else {
			throw new IllegalArgumentException();
		}
		return tokenizer;
	}

}
