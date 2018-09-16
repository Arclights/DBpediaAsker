package com.arclights.dbpediaasker.interpreter

enum class IOB {
    I,
    O,
    B
}

data class TaggedString(
        val wordIndex: Int,
        val wordForm: String,
        val lemma: String,
        val posTagCoarse: String?,
        val posTagFine: String?,
        val morphologicalFeatures: Set<String>?,
        val head: String?,
        val dependencyType: String?,
        val chunkTag: IOB?,
        val chunkType: String?,
        val neTag: IOB?,
        val neType: String?,
        val tokenId: String
)

class TaggedStringAppender : Appendable {

    private var taggedStrings = emptyList<TaggedString>()

    override fun append(csq: CharSequence): Appendable {
        if (csq.isNotBlank()) {
            taggedStrings = taggedStrings.plus(
                    csq
                            .trim()
                            .split('\t')
                            .let {
                                TaggedString(
                                        wordIndex = it[0].toInt(),
                                        wordForm = it[1],
                                        lemma = it[2],
                                        posTagCoarse = getStringOrNull(it[3]),
                                        posTagFine = getStringOrNull(it[4]),
                                        morphologicalFeatures = getStringSetOrNull(it[5]),
                                        head = getStringOrNull(it[6]),
                                        dependencyType = getStringOrNull(it[7]),
                                        chunkTag = getIOBOrNull(it[8]),
                                        chunkType = getStringOrNull(it[9]),
                                        neTag = getIOBOrNull(it[10]),
                                        neType = getStringOrNull(it[11]),
                                        tokenId = it[12]
                                )
                            }
            )
        }
        return this
    }

    private fun getStringOrNull(string: String) =
            when (string) {
                "_" -> null
                else -> string
            }

    private fun getStringSetOrNull(string: String) =
            when (string) {
                "_" -> null
                else -> string.split('|').toSet()
            }

    private fun getIOBOrNull(string: String) =
            when (string.toUpperCase()) {
                "_" -> null
                "I" -> IOB.I
                "O" -> IOB.O
                "B" -> IOB.B
                else -> throw IllegalArgumentException("Unknown IOB representation: $string")
            }

    fun asList() = taggedStrings

    override fun toString() = taggedStrings.toString()

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun append(c: Char): Appendable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}