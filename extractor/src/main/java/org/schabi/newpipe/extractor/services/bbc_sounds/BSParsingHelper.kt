package org.schabi.newpipe.extractor.services.bbc_sounds

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal object BSParsingHelper {

    @Throws(ParsingException::class)
    fun parseDate(textualDate: String): Calendar {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .apply { timeZone = TimeZone.getTimeZone("GMT") }
                    .parse(textualDate).let { Calendar.getInstance().apply { time = it } }
        } catch (e: ParseException) {
            throw ParsingException("Could not parse date: $textualDate", e)
        }
    }

    fun getNextPageUrl(prevPageUrl: String, limit: Int, total: Int): String {
        val prevOffset = try {
            Parser.matchGroup1("offset=(\\d*)", prevPageUrl)
        } catch (e: RegexException) {
            return ""
        }
        val nextOffset = prevOffset.toInt() + limit
        return if (nextOffset < total) {
            prevPageUrl.replace("offset=$prevOffset", "offset=$nextOffset")
        } else {
            ""
        }
    }
}
