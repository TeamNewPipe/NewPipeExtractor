package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import org.junit.BeforeClass
import org.schabi.newpipe.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest

@Suppress("unused")
class BSSearchExtractorTest {
    class BSDefaultSearchExtractorTest : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return ServiceList.BBC_SOUNDS
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "/search?q=$QUERY"
        }

        override fun expectedOriginalUrlContains(): String {
            return "/search?q=$QUERY"
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            @JvmStatic
            private lateinit var extractor: SearchExtractor
            private const val QUERY = "breakfast"

            @JvmStatic
            @BeforeClass
            @Throws(Exception::class)
            fun setUp() {
                NewPipe.init(DownloaderTestImpl.getInstance())
                extractor = ServiceList.BBC_SOUNDS.getSearchExtractor(QUERY)
                extractor.fetchPage()
            }
        }
    }
}