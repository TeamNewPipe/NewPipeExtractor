package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser

object BSChannelLinkHandlerFactory : ListLinkHandlerFactory() {
    override fun getUrl(id: String, contentFilter: MutableList<String>?, sortFilter: String?): String {
        if (NETWORK_LHF.onAcceptId(id)) {
            return NETWORK_LHF.getUrl(id, contentFilter, sortFilter)
        }
        return CHANNEL_API_URL.format(id.split(":").last())
    }

    override fun getId(url: String): String {
        if (NETWORK_LHF.onAcceptUrl(url)) {
            return NETWORK_LHF.getId(url)
        }
        return kotlin.runCatching { Parser.matchGroup(ID_PATTERN, url, 2) }.getOrNull()
                ?: Parser.matchGroup1(ID_PATTERN_API, url)
    }

    override fun onAcceptUrl(url: String?): Boolean {
        return Parser.isMatch(ID_PATTERN, url) || Parser.isMatch(ID_PATTERN_API, url) || NETWORK_LHF.onAcceptUrl(url)
    }


    private const val ID_PATTERN = "/sounds/(brand|series)/([^/?&#]*)"
    private const val ID_PATTERN_API = "/programmes/playable\\?container=([^/?&#]*)"
    private const val CHANNEL_API_URL = "https://rms.api.bbc.co.uk/v2/programmes/playable?container=%s&offset=0"
    const val NETWORK_ID_PREFIX = "network:"
    private val NETWORK_LHF = BSNetworkChannelLinkHandlerFactory

    fun onAcceptNetworkUrl(url: String): Boolean {
        return NETWORK_LHF.onAcceptUrl(url)
    }

    @Throws(ParsingException::class)
    fun fromNetworkId(id: String): ListLinkHandler {
        if (NETWORK_LHF.onAcceptId(id)) {
            return NETWORK_LHF.fromId(id)
        } else {
            throw ParsingException("id is not of type network")
        }
    }

    private object BSNetworkChannelLinkHandlerFactory : ListLinkHandlerFactory() {
        override fun getUrl(id: String, contentFilter: MutableList<String>?, sortFilter: String?): String {
            return NETWORK_API_URL.format(id.split(":").last())
        }

        override fun onAcceptUrl(url: String?): Boolean {
            return Parser.isMatch(ID_PATTERN_NETWORK_API, url)
        }

        override fun getId(url: String?): String {
            val id = Parser.matchGroup1(ID_PATTERN_NETWORK_API, url)
            // prefix to distinguish from normal channel
            return "$NETWORK_ID_PREFIX$id"
        }

        fun onAcceptId(id: String): Boolean {
            return id.startsWith(NETWORK_ID_PREFIX)
        }

        private const val ID_PATTERN_NETWORK_API = "/programmes/playable\\?network=([^/?&#]*)"
        private const val NETWORK_API_URL = "https://rms.api.bbc.co.uk/v2/programmes/playable?network=%s&offset=0"
    }
}