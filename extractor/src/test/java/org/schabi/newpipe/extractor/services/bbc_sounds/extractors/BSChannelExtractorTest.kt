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
class BSChannelExtractorTest {

    class BSDefaultChannelExtractorTest : BaseChannelExtractorTest {

        companion object {
            @JvmStatic
            private lateinit var extractor: ChannelExtractor

            @JvmStatic
            @BeforeClass
            @Throws(Exception::class)
            fun setUp() {
                NewPipe.init(DownloaderTestImpl.getInstance())
                extractor = ServiceList.BBC_SOUNDS.getChannelExtractor("https://www.bbc.co.uk/sounds/series/p02nrsln")
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
            Assert.assertEquals("Football Daily", extractor.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assert.assertEquals("p02nrsln", extractor.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?container=p02nrsln&offset=0", extractor.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assert.assertEquals("https://bbc.co.uk/sounds/brand/p02nrsln", extractor.originalUrl)
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
            val description = extractor.description
            Assert.assertTrue(description, description.contains("latest football news"))
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatarUrl() {
            val avatarUrl = extractor.avatarUrl
            ExtractorAsserts.assertIsSecureUrl(avatarUrl)
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