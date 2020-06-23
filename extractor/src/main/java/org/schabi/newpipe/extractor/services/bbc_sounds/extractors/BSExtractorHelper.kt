package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.bbc_sounds.BSDashMpdParser
import org.schabi.newpipe.extractor.services.bbc_sounds.BSParsingHelper
import org.schabi.newpipe.extractor.services.bbc_sounds.BSService
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.*

internal object BSExtractorHelper {

    fun getStreamInfoItemExtractor(data: JsonObject): StreamInfoItemExtractor {
        val id = data.getString("urn")
        return if(BSStreamLinkHandlerFactory.onAcceptNetworkId(id)) {
            BSNetworkStreamInfoItemExtractor(data)
        } else {
            BSStreamInfoItemExtractor(data)
        }
    }

    fun getChannelExtractor(service: BSService, linkHandler: ListLinkHandler): ChannelExtractor {
        if(BSChannelLinkHandlerFactory.onAcceptNetworkUrl(linkHandler.url)) {
            return BSNetworkExtractor(service, linkHandler)
        }
        return BSChannelExtractor(service, linkHandler)
    }

    fun getStreamExtractor(service: BSService, linkHandler: LinkHandler): StreamExtractor {
        if(BSStreamLinkHandlerFactory.onAcceptNetworkUrl(linkHandler.url)) {
            return BSNetworkStreamExtractor(service, linkHandler)
        }
        return BSStreamExtractor(service, linkHandler)
    }

    fun fetchStreams(downloader: Downloader, data: JsonObject): List<AudioStream> {
        val audioStreams: MutableList<AudioStream> = ArrayList()
        //fetch stream links
        var streamData: JsonObject? = null
        MEDIASELECTOR_URLS.forEach {
            kotlin.runCatching {
                val vpId = data.getString("id")
                val response = downloader.get(it.format(vpId))?.responseBody()
                JsonParser.`object`().from(response)?.takeIf { it.containsKey("media") }.apply {
                    streamData = this
                    return@forEach
                }
            }
        }

        streamData?.getArray("media")?.forEach { media ->
            (media as? JsonObject)?.takeIf { it.getString("kind") == "audio" }?.let {
                val abr = it.getString("bitrate").toInt()
                it.getArray("connection")?.forEach { connection ->
                    (connection as? JsonObject)?.takeIf { it.getString("protocol") == "https" && it.getString("href") != null }?.apply {
                        val transferFormat = this.getString("transferFormat")
                        val url = this.getString("href")
                        when (transferFormat) {
                            "hls", "hds" -> {
                                // don't bother atm, dash works perfect
                            }
                            "dash" -> kotlin.runCatching {
                                BSDashMpdParser.getStreams(url).audioStreams?.forEach { stream ->
                                    if (!AudioStream.containSimilarStream(stream, audioStreams)) {
                                        audioStreams.add(stream)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return audioStreams
    }

    fun parsePage(extractor: Extractor, pageUrl: String, pageContent: JsonObject): ListExtractor.InfoItemsPage<StreamInfoItem> {
        val collector = StreamInfoItemsCollector(extractor.serviceId)
        pageContent.getArray("data")?.forEach {item ->
            (item as? JsonObject)?.apply {
                collector.commit(getStreamInfoItemExtractor(this))
            }
        }
        val limit = pageContent.getInt("limit")
        val total = pageContent.getInt("total")
        return ListExtractor.InfoItemsPage(collector, BSParsingHelper.getNextPageUrl(pageUrl, limit, total))
    }


    private val MEDIASELECTOR_URLS = listOf(
            "https://open.live.bbc.co.uk/mediaselector/6/select/version/2.0/mediaset/pc/vpid/%s"
    )
}