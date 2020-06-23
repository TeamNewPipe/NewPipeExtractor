package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.bbc_sounds.BSParsingHelper.parseDate
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType

internal class BSNetworkStreamInfoItemExtractor(val data: JsonObject): StreamInfoItemExtractor {

    init {
        val id = data.getString("urn")
        if(!BSStreamLinkHandlerFactory.onAcceptNetworkId(id)) {
            throw IllegalArgumentException("only network streams are allowed")
        }
    }

    override fun getUrl(): String {
        val id = data.getString("urn")
        return BSStreamLinkHandlerFactory.fromNetworkId(id).url
    }

    override fun getDuration(): Long {
        return -1
    }

    override fun getName(): String {
        val networkName = data.getObject("network")?.getString("short_title")
        val showName = data.getObject("titles")?.getString("primary")
        return "$networkName | $showName"
    }

    override fun getThumbnailUrl(): String? {
        return data.getString("image_url")?.replace("{recipe}", "640x360") ?: ""
    }

    override fun getStreamType(): StreamType {
        return StreamType.AUDIO_LIVE_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }

    override fun getTextualUploadDate(): String? {
        return data.getObject("release")?.getString("date")
    }

    override fun getUploadDate(): DateWrapper? {
        return textualUploadDate?.let { parseDate(it) }?.let { DateWrapper(it) }
    }

    override fun getUploaderName(): String {
        return data.getObject("network")?.getString("short_title") ?: ""
    }

    override fun getUploaderUrl(): String {
        val id = BSChannelLinkHandlerFactory.NETWORK_ID_PREFIX + data.getObject("network").getString("id")
        return BSChannelLinkHandlerFactory.fromNetworkId(id).url
    }

    override fun getViewCount(): Long {
        return -1
    }

}