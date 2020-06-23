package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector
import org.schabi.newpipe.extractor.search.SearchExtractor

class BSSearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : SearchExtractor(service, linkHandler) {

   private lateinit var initPage: InfoItemsPage<InfoItem>

    override fun onFetchPage(downloader: Downloader) {
        initPage = getPage(url)
    }

    override fun getInitialPage(): InfoItemsPage<InfoItem> {
        super.fetchPage()
        return initPage
    }

    override fun getNextPageUrl(): String {
        super.fetchPage()
        return initPage.nextPageUrl
    }

    override fun getPage(pageUrl: String): InfoItemsPage<InfoItem> {
        val collector = InfoItemsSearchCollector(serviceId)
        val response = JsonParser.`object`().from(downloader.get(pageUrl).responseBody())
        response.getArray("data")?.firstOrNull {
            (it as? JsonObject)?.getString("id") == "playable_search"
        }?.let {
            (it as? JsonObject)?.getArray("data")?.forEach { item ->
                (item as? JsonObject)?.apply {
                    collector.commit(BSExtractorHelper.getStreamInfoItemExtractor(this))
                }
            }
        }
        return InfoItemsPage(collector, "")
    }

    override fun getSearchSuggestion(): String {
        return ""
    }

    override fun isCorrectedSearch(): Boolean {
        return false
    }

}