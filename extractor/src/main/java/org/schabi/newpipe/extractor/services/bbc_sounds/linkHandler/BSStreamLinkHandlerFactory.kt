package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser

object BSStreamLinkHandlerFactory : LinkHandlerFactory() {
    override fun getId(url: String): String {
        if (NETWORK_STREAM_LHF.onAcceptUrl(url)) {
            return NETWORK_STREAM_LHF.getId(url)
        }
        return kotlin.runCatching { Parser.matchGroup1(ID_PATTERN, url) }.getOrNull()
                ?: Parser.matchGroup1(ID_PATTERN_API, url)
    }

    override fun getUrl(id: String): String {
        if (NETWORK_STREAM_LHF.onAcceptId(id)) {
            return NETWORK_STREAM_LHF.getUrl(id)
        }
        return STREAM_API_URL.format(id.split(":").last())
    }

    override fun onAcceptUrl(url: String?): Boolean {
        return Parser.isMatch(ID_PATTERN, url) || Parser.isMatch(ID_PATTERN_API, url) || NETWORK_STREAM_LHF.onAcceptUrl(url)
    }

    fun getRelatedStreamsUrl(id: String): String {
        if (NETWORK_STREAM_LHF.onAcceptId(id)) {
            return NETWORK_STREAM_LHF.getRelatedStreamsUrl(id)
        }
        return RELATED_STREAMS_API_URL.format(id)
    }

    fun onAcceptNetworkUrl(url: String): Boolean {
        return NETWORK_STREAM_LHF.onAcceptUrl(url)
    }

    fun onAcceptNetworkId(id: String): Boolean {
        return NETWORK_STREAM_LHF.onAcceptId(id)
    }

    @Throws(ParsingException::class)
    fun fromNetworkId(id: String): LinkHandler {
        if(NETWORK_STREAM_LHF.onAcceptId(id)) {
            return NETWORK_STREAM_LHF.fromId(id)
        } else {
            throw ParsingException("id is not of type network")
        }
    }

    private val NETWORK_STREAM_LHF = BSNetworkStreamLinkHandlerFactory
    private const val ID_PATTERN = "/sounds/play/([^/?&#:]*)"
    private const val ID_PATTERN_API = "/programmes/([^/?&#]*)/playable"
    private const val STREAM_API_URL = "https://rms.api.bbc.co.uk/v2/programmes/%s/playable"
    private const val RELATED_STREAMS_API_URL = "https://rms.api.bbc.co.uk/v2/programmes/playqueue/%s"

    private object BSNetworkStreamLinkHandlerFactory : LinkHandlerFactory() {
        override fun getUrl(id: String): String {
            return NETWORK_STREAM_API_URL.format(id.split(":").last())
        }

        override fun onAcceptUrl(url: String?): Boolean {
            return Parser.isMatch(ID_PATTERN_NETWORK_STREAM_API, url)
        }

        override fun getId(url: String?): String {
            val id = Parser.matchGroup1(ID_PATTERN_NETWORK_STREAM_API, url)
            // prefix to distinguish from normal stream
            return "$NETWORK_STREAM_ID_PREFIX$id"
        }

        fun onAcceptId(id: String): Boolean {
            return id.startsWith(NETWORK_STREAM_ID_PREFIX)
        }

        fun getRelatedStreamsUrl(id: String): String {
            return ""
        }

        private const val ID_PATTERN_NETWORK_STREAM_API = "/networks/([^/?&#]*)/playable"
        private const val NETWORK_STREAM_API_URL = "https://rms.api.bbc.co.uk/v2/networks/%s/playable"
        private const val NETWORK_STREAM_ID_PREFIX = "urn:bbc:radio:network:"
    }
}