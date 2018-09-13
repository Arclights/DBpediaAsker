package question;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class QuestionHandler {

	/**
	 * Prints the questions to file that can later be read to a SQL database
	 * 
	 * @param questions
	 *            - Question structure
	 */
	public static void printQuestionsToSQL(ArrayList<Question> questions) {
		try {
			PrintWriter writer = new PrintWriter("Questions.sql");
			for (Question q : questions) {
				if (q.hasTriples()) {
					writer.println(q.toSQL());
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
