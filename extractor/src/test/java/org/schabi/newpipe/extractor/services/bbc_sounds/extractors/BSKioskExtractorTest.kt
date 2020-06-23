package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.schabi.newpipe.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.BaseListExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests

@Suppress("unused")
class BSKioskExtractorTest {

    class BSRadioKioskTest : BaseListExtractorTest {

        companion object {
            @JvmStatic
            private lateinit var extractor: BSKioskExtractor

            @JvmStatic
            @BeforeClass
            @Throws(Exception::class)
            fun setUp() {
                NewPipe.init(DownloaderTestImpl.getInstance())
                extractor = ServiceList.BBC_SOUNDS.kioskList.getExtractorById("Radio (Live)", null) as BSKioskExtractor
                extractor.fetchPage()
            }
        }


        @Test
        override fun testServiceId() {
            Assert.assertEquals(ServiceList.BBC_SOUNDS.serviceId.toLong(), extractor.serviceId.toLong())
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assert.assertEquals("Radio (Live)", extractor.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assert.assertEquals("Radio (Live)", extractor.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/networks/playable", extractor.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/networks/playable", extractor.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor)
        }

        @Test
        override fun testMoreRelatedItems() {
            Assert.assertEquals("", extractor.nextPageUrl)
        }
    }
}