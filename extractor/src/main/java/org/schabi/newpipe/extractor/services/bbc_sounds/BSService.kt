package org.schabi.newpipe.extractor.services.bbc_sounds

import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO
import org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.LIVE
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.kiosk.KioskList
import org.schabi.newpipe.extractor.kiosk.KioskList.KioskExtractorFactory
import org.schabi.newpipe.extractor.linkhandler.*
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.bbc_sounds.extractors.BSExtractorHelper
import org.schabi.newpipe.extractor.services.bbc_sounds.extractors.BSKioskExtractor
import org.schabi.newpipe.extractor.services.bbc_sounds.extractors.BSSearchExtractor
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSKioskLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor

class BSService(id: Int) : StreamingService(id, "BBC Sounds", listOf(AUDIO, LIVE)) {
    override fun getBaseUrl(): String {
        return "https://bbc.co.uk/sounds"
    }

    override fun getStreamLHFactory(): LinkHandlerFactory {
        return BSStreamLinkHandlerFactory
    }

    override fun getChannelLHFactory(): ListLinkHandlerFactory {
        return BSChannelLinkHandlerFactory
    }

    override fun getPlaylistLHFactory(): ListLinkHandlerFactory? {
        return null
    }

    override fun getSearchQHFactory(): SearchQueryHandlerFactory {
        return BSSearchQueryHandlerFactory
    }

    override fun getCommentsLHFactory(): ListLinkHandlerFactory? {
        return null
    }

    override fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor {
        // TODO fix this to return brands,series also
        return BSSearchExtractor(this, queryHandler)
    }

    override fun getSuggestionExtractor(): SuggestionExtractor? {
        return null
    }

    override fun getSubscriptionExtractor(): SubscriptionExtractor? {
        return null
    }

    override fun getKioskList(): KioskList {
        val kioskLHF = BSKioskLinkHandlerFactory

        val kioskFactory = KioskExtractorFactory { service, _, id ->
            BSKioskExtractor(service,
                    kioskLHF.fromId(id), id)
        }

        val list = KioskList(this)
        BSKioskLinkHandlerFactory.kiosks.forEach {
            list.addKioskEntry(kioskFactory, kioskLHF, it.key)
        }
        list.setDefaultKiosk(BSKioskLinkHandlerFactory.DEFAULT_KIOSK_ID)

        return list
    }

    override fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return BSExtractorHelper.getChannelExtractor(this, linkHandler)
    }

    override fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor? {
        return null
    }

    override fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor {
        return BSExtractorHelper.getStreamExtractor(this, linkHandler)
    }

    override fun getCommentsExtractor(linkHandler: ListLinkHandler?): CommentsExtractor? {
        return null
    }
}