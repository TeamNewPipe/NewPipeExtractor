package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class BSChannelExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {

    private lateinit var initPage: InfoItemsPage<StreamInfoItem>
    private var container: JsonObject? = null
    private var network: JsonObject? = null

    override fun onFetchPage(downloader: Downloader) {
        initPage = getPage(url)
    }

    override fun getInitialPage(): InfoItemsPage<StreamInfoItem> {
        super.fetchPage()
        return initPage
    }

    override fun getNextPageUrl(): String {
        super.fetchPage()
        return initPage.nextPageUrl
    }

    override fun getPage(pageUrl: String): InfoItemsPage<StreamInfoItem> {
        val response = JsonParser.`object`().from(downloader.get(pageUrl).responseBody())
        if(network == null) {
            network = response.getArray("data")?.get(0)?.let { it as? JsonObject }?.getObject("network")
        }
        if(container == null) {
            container = response.getArray("data")?.get(0)?.let { it as? JsonObject }?.getObject("container")
        }
        return BSExtractorHelper.parsePage(this, pageUrl, response)
    }

    private fun getDescription(synopses: JsonObject): String {
        synopses.getString("long")?.let { return it }
        synopses.getString("medium")?.let { return it }
        synopses.getString("short")?.let { return it }
        return ""
    }

    override fun getSubscriberCount(): Long {
        return -1
    }

    override fun getName(): String {
        return container?.getString("title") ?: ""
    }

    override fun getAvatarUrl(): String {
        return initPage.items?.get(0)?.thumbnailUrl ?: ""
    }

    override fun getBannerUrl(): String {
        return ""
    }

    override fun getFeedUrl(): String {
        return ""
    }

    override fun getDescription(): String {
        return container?.getObject("synopses")?.let { getDescription(it) } ?: ""
    }

    override fun getParentChannelName(): String {
        return network?.getString("short_title") ?: ""
    }

    override fun getParentChannelUrl(): String {
        return network?.getString("id")?.let {
            val id = BSChannelLinkHandlerFactory.NETWORK_ID_PREFIX + it
            BSChannelLinkHandlerFactory.fromNetworkId(id).url
        } ?: ""
    }

    override fun getParentChannelAvatarUrl(): String {
        return network?.getString("logo_url")
                ?.replace("{type}", "colour")
                ?.replace("{size}", "default")
                ?.replace("{format}", "svg") ?: ""

    }

    override fun getOriginalUrl(): String {
        return "${service.baseUrl}/brand/$id"
    }
}