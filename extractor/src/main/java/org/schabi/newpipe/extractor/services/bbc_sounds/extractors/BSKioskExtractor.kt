package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class BSKioskExtractor(streamingService: StreamingService?, linkHandler: ListLinkHandler?, kioskId: String?) : KioskExtractor<StreamInfoItem>(streamingService, linkHandler, kioskId) {

    private lateinit var initPage: InfoItemsPage<StreamInfoItem>

    override fun getName(): String {
        return id
    }

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
        return BSExtractorHelper.parsePage(this, pageUrl, response)
    }
}

