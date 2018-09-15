package com.arclights.dbpediaasker.questionProcessor;

import java.io.IOException;

public class TextTagger {

    /**
     * Runs Stagger on the information from the cards database
     * <p>
     * OBS: The output will end up in RDF-output.txt.conll, not in out.txt. But
     * Stagger doesn't seem to work without it.
     *
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public static void tag() throws Exception {
        System.out.println("Tagging...");
		Process proc = Runtime
				.getRuntime()
				.exec("java -Xms2000M -jar src/main/resources/libs/stagger.jar -modelfile configs/swedish.bin -tag RDF_output.txt > out.txt");
		proc.waitFor();
    }


    public static void main(String[] args) throws Exception{
        try {
            tag();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
