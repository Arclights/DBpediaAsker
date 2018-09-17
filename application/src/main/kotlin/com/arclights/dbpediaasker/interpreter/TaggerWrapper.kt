package com.arclights.dbpediaasker.interpreter

import com.google.common.io.CharSource
import com.google.inject.Inject
import com.google.inject.Singleton
import io.dropwizard.lifecycle.Managed
import org.slf4j.LoggerFactory
import se.su.ling.stagger.EnglishTokenizer
import se.su.ling.stagger.Evaluation
import se.su.ling.stagger.FormatException
import se.su.ling.stagger.LatinTokenizer
import se.su.ling.stagger.SwedishTokenizer
import se.su.ling.stagger.TagNameException
import se.su.ling.stagger.TaggedToken
import se.su.ling.stagger.Tagger
import se.su.ling.stagger.Token
import se.su.ling.stagger.Tokenizer
import java.io.BufferedReader
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Reader
import java.io.StringReader

/**
 * Wrapper of Stagger that pre-loads Stagger so it doesn't have to be
 * loaded for every question
 */
@Singleton
class TaggerWrapper @Inject constructor(private val taggerModel: CharSource) : Managed {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var lang: String
    private lateinit var tagger: Tagger
    private var inputSents: Array<Array<TaggedToken>>? = null
    private var hasNE = true
    private var extendLexicon = true
    private var preserve = false
    private var plainOutput = false

    override fun start() {
        ObjectInputStream(taggerModel.asByteSource(Charsets.UTF_8).openStream()).use {
            logger.info("Loading Stagger model ...")
            tagger = it.readObject() as Tagger
            logger.info("Stagger model loaded")
            lang = tagger.taggedData.language
        }
    }

    override fun stop() {
    }

    /**
     * Performs the tagging on a question. Because of Staggers structure, the
     * question has to be in a file that has to be in a list. I then prints the
     * result to a file.
     *
     * @param question
     * - The question in a file in an ArrayList
     * @throws FormatException
     * @throws TagNameException
     * @throws java.io.IOException
     */
    @Throws(FormatException::class, TagNameException::class, IOException::class)
    fun tag(question: String): List<TaggedString> {
        var fileID = question.split("\\s".toRegex()).first()
        val reader = StringReader(question)
        val tokenizer = getTokenizer(reader, lang)
        var sentIdx = 0
        val tagAppender = TaggedStringAppender()
        tokenizer
                .getSentenceIterator()
                .forEach { sentence ->
                    if (tokenizer.sentID != null && fileID != tokenizer.sentID) {
                        fileID = tokenizer.sentID
                        sentIdx = 0
                    }
                    val sent = sentence.map { TaggedToken(it, "$fileID:$sentIdx:$it.offset") }.toTypedArray()
                    val taggedSent = tagger.tagSentence(sent, true, false)

                    tagger.taggedData.writeConllSentence(tagAppender, taggedSent, plainOutput)
                    sentIdx++
                }
        tokenizer.yyclose()
        return tagAppender.asList()
    }

    /**
     * Performs the tagging on a question. Because of Staggers structure, the
     * question has to be in a file that has to be in a list. I then prints the
     * result to a file.
     *
     * @param question
     * - The question in a file in an ArrayList
     * @throws FormatException
     * @throws TagNameException
     * @throws java.io.IOException
     */
    @Throws(FormatException::class, TagNameException::class, IOException::class)
    fun tagConllSentences(question: String) {

        tagger.setExtendLexicon(extendLexicon)
        if (!hasNE)
            tagger.setHasNE(false)

        inputSents = tagger.taggedData.readConll(
                BufferedReader(StringReader(question)),
                null,
                true,
                false
        )
        val eval = Evaluation()
        var count = 0
        for (sent in inputSents!!) {
            if (count % 100 == 0)
                System.err.print("Tagging sentence nr: $count\r")
            count++
            val taggedSent = tagger.tagSentence(sent, true, preserve)

            eval.evaluate(taggedSent, sent)
            tagger.taggedData.writeConllGold(System.out, taggedSent, sent, plainOutput)
        }
        logger.debug("Tagging sentence nr: $count")
        logger.debug("POS accuracy: ${eval.posAccuracy()} (${eval.posCorrect} / ${eval.posTotal})")
        logger.debug("NE precision: ${eval.nePrecision()}")
        logger.debug("NE recall:    ${eval.neRecall()}")
        logger.debug("NE F-score:   ${eval.neFscore()}")
    }

    /**
     * Creates and returns a tokenizer for the given language.
     *
     * @param reader
     * @param lang
     * @return
     */
    private fun getTokenizer(reader: Reader, lang: String): Tokenizer =
            when (lang) {
                "sv" -> SwedishTokenizer(reader)
                "en" -> EnglishTokenizer(reader)
                "any" -> LatinTokenizer(reader)
                else -> throw IllegalArgumentException()
            }

}

fun Tokenizer.getSentenceIterator(): Iterator<List<Token>> {
    val tokenizer = this
    return object : Iterator<List<Token>> {
        var next: List<Token>? = null

        override fun hasNext(): Boolean {
            next = tokenizer.readSentence()
            return next != null
        }

        override fun next(): List<Token> = next ?: throw NoSuchElementException()
    }
}