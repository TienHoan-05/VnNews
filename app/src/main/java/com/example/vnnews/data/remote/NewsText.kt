package com.example.vnnews.data.remote

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.text.Normalizer

/** Decode feed HTML once and keep Vietnamese combining marks in canonical form. */
object NewsText {
    fun title(value: String): String = normalize(Parser.unescapeEntities(value, false))

    fun description(value: String?): String = if (value.isNullOrBlank()) "" else
        normalize(Parser.unescapeEntities(Jsoup.parseBodyFragment(value).text(), false))

    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFC)
        .replace(Regex("[\\p{Cc}&&[^\\n\\r\\t]]|[\\u200b\\ufeff]"), "")
        .replace(Regex("[\\s\\u00a0\\u202f]+"), " ").trim()
}
