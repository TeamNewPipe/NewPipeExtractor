package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.schabi.newpipe.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType

@Suppress("unused")
class BSNetworkStreamExtractorTest {

    class BSDefaultNetworkStreamExtractorTest {
        companion object {
            @JvmStatic
            private lateinit var extractor: StreamExtractor

            @JvmStatic
            @BeforeClass
            @Throws(Exception::class)
            fun setUp() {
                NewPipe.init(DownloaderTestImpl.getInstance())
                extractor = ServiceList.BBC_SOUNDS.getStreamExtractor("https://rms.api.bbc.co.uk/v2/networks/bbc_radio_one/playable")
                extractor.fetchPage()
            }
        }

        @Test
        fun testCounts() {
            Assert.assertEquals(-1, extractor.viewCount)
            Assert.assertEquals(-1, extractor.likeCount)
            Assert.assertEquals(-1, extractor.dislikeCount)
            Assert.assertEquals(0, extractor.ageLimit)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetInvalidTimeStamp() {
            Assert.assertTrue(extractor.timeStamp.toString() + "",
                    extractor.timeStamp <= 0)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetTitle() {
            Assert.assertTrue(extractor.name.startsWith("Radio 1"))
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetDescription() {
            Assert.assertNotNull("description is missing", extractor.description.content)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderName() {
            Assert.assertEquals("Radio 1", extractor.uploaderName)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderUrl() {
            ExtractorAsserts.assertIsSecureUrl(extractor.uploaderUrl)
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0", extractor.uploaderUrl)

        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderAvatarUrl() {
            ExtractorAsserts.assertIsSecureUrl(extractor.uploaderAvatarUrl)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetLength() {
            Assert.assertEquals(-1, extractor.length)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetThumbnailUrl() {
            ExtractorAsserts.assertIsSecureUrl(extractor.thumbnailUrl)
        }

        @Test
        @Throws(ParsingException::class)
        fun testStreamType() {
            Assert.assertTrue(extractor.streamType == StreamType.AUDIO_LIVE_STREAM)
        }

        @Test
        fun testGetAudioStreams() {
            Assert.assertTrue(extractor.audioStreams.isNotEmpty() && extractor.audioStreams[0].deliveryFormat != null)
        }

        @Test
        fun testGetDashMpdUrl() {
            Assert.assertFalse(extractor.dashMpdUrl.isBlank())
        }
    }
}