package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

object BSKioskLinkHandlerFactory : ListLinkHandlerFactory() {

    override fun getUrl(id: String, contentFilter: MutableList<String>?, sortFilter: String?): String {
        return kiosks.getOrElse(id) { throw ParsingException("Unsupported id") }
    }

    override fun getId(url: String): String {
        return reversedKiosks.getOrElse(url) { throw ParsingException("Unsupported url") }
    }

    override fun onAcceptUrl(url: String): Boolean {
        return reversedKiosks.containsKey(url)
    }

    const val DEFAULT_KIOSK_ID = "Radio (Live)"
    val kiosks = mapOf(
            "Radio (Live)" to "https://rms.api.bbc.co.uk/v2/networks/playable",
            "Mixes" to "https://rms.api.bbc.co.uk/v2/programmes/playable?category=mixes&offset=0",
            "Podcasts" to "https://rms.api.bbc.co.uk/v2/programmes/playable?category=podcasts&offset=0",
            "News" to "https://rms.api.bbc.co.uk/v2/programmes/playable?category=news&offset=0",
            "Sports" to "https://rms.api.bbc.co.uk/v2/programmes/playable?category=sport&offset=0",
            "History" to "https://rms.api.bbc.co.uk/v2/programmes/playable?category=factual-history&offset=0"
    )
    private val reversedKiosks = kiosks.entries.associate { (k, v) -> v to k }
}