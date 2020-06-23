package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.bbc_sounds.BSParsingHelper.parseDate
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType

class BSStreamInfoItemExtractor(val data: JsonObject) : StreamInfoItemExtractor {

    init {
        val id = data.getString("urn")
        if(BSStreamLinkHandlerFactory.onAcceptNetworkId(id)) {
            throw IllegalArgumentException("network streams not allowed")
        }
    }

    override fun getUrl(): String {
        val id = data.getString("urn")
        return BSStreamLinkHandlerFactory.fromId(id).url
    }

    override fun getDuration(): Long {
        return data.getObject("duration")?.getLong("value") ?: 0L
    }

    override fun getName(): String {
        var name = data.getObject("titles").getString("primary")
        data.getObject("titles").getString("secondary")?.run { name = "$name | $this" }
        return name
    }

    override fun getThumbnailUrl(): String {
        return data.getString("image_url")?.replace("{recipe}", "640x360") ?: ""
    }

    override fun getStreamType(): StreamType {
        if("live".equals(data.getObject("availability")?.getString("label"), true)){
            return StreamType.AUDIO_LIVE_STREAM
        }
        return StreamType.AUDIO_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }

    override fun getViewCount(): Long {
        return -1
    }

    override fun getUploaderName(): String {
        return data.getObject("network")?.getString("short_title") ?: ""
    }

    override fun getUploaderUrl(): String {
        val id = BSChannelLinkHandlerFactory.NETWORK_ID_PREFIX + data.getObject("network").getString("id")
        return BSChannelLinkHandlerFactory.fromNetworkId(id).url
    }

    override fun getTextualUploadDate(): String? {
        return data.getObject("availability")?.getString("from")
    }

    override fun getUploadDate(): DateWrapper? {
        return textualUploadDate?.let { parseDate(it) }?.let { DateWrapper(it) }
    }
}
