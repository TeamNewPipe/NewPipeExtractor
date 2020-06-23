package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.schabi.newpipe.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests

@Suppress("unused")
class BSNetworkExtractorTest {

    class BSBBC1ExtractorTest: BaseChannelExtractorTest {

        companion object {
            @JvmStatic
            private lateinit var extractor: ChannelExtractor

            @JvmStatic
            @BeforeClass
            @Throws(Exception::class)
            fun setUp() {
                NewPipe.init(DownloaderTestImpl.getInstance())
                extractor = ServiceList.BBC_SOUNDS.getChannelExtractor("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0")
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
            Assert.assertEquals("Radio 1", extractor.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assert.assertEquals("network:bbc_radio_one", extractor.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0", extractor.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assert.assertEquals("https://www.bbc.co.uk/radio1", extractor.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor)
        }

        @Test
        override fun testMoreRelatedItems() {
            DefaultTests.defaultTestMoreItems(extractor)
        }

        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            Assert.assertEquals("", extractor.description)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatarUrl() {
            val avatarUrl = extractor.avatarUrl
            ExtractorAsserts.assertIsSecureUrl(avatarUrl)
            Assert.assertEquals("https://sounds.files.bbci.co.uk/2.2.4/networks/bbc_radio_one/colour_default.svg", avatarUrl)
        }

        override fun testBannerUrl() {
            Assert.assertEquals("", extractor.bannerUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assert.assertEquals("", extractor.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            val subscribers = extractor.subscriberCount
            Assert.assertEquals(-1, subscribers)
        }
    }

}