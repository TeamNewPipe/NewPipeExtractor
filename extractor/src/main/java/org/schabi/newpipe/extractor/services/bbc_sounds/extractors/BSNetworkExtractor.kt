package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class BSNetworkExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {

    private lateinit var initPage: InfoItemsPage<StreamInfoItem>
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
        return BSExtractorHelper.parsePage(this, pageUrl, response)
    }

    override fun getSubscriberCount(): Long {
        return -1
    }

    override fun getName(): String {
        return network?.getString("short_title") ?: ""
    }

    override fun getAvatarUrl(): String {
        return network?.getString("logo_url")
                ?.replace("{type}", "colour")
                ?.replace("{size}", "default")
                ?.replace("{format}", "svg") ?: ""
    }

    override fun getBannerUrl(): String {
        return ""
    }

    override fun getFeedUrl(): String {
        return ""
    }

    override fun getDescription(): String {
        return ""
    }

    override fun getParentChannelName(): String {
        return ""
    }

    override fun getParentChannelUrl(): String {
        return ""
    }

    override fun getParentChannelAvatarUrl(): String {
        return ""
    }

    override fun getOriginalUrl(): String {
        return network?.getString("key")?.let {
            "https://www.bbc.co.uk/$it"
        } ?: ""
    }

}