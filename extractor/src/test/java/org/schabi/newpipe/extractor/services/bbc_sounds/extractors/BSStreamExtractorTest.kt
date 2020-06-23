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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
class BSStreamExtractorTest {

    class BSDefaultStreamExtractorTest {
        companion object {
            @JvmStatic
            private lateinit var extractor: StreamExtractor

            @JvmStatic
            @BeforeClass
            @Throws(Exception::class)
            fun setUp() {
                NewPipe.init(DownloaderTestImpl.getInstance())
                extractor = ServiceList.BBC_SOUNDS.getStreamExtractor("https://www.bbc.co.uk/sounds/play/p00cbxy3")
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
        @Throws(ParsingException::class, ParseException::class)
        fun testGetUploadDate() {
            val instance = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            instance.time = sdf.parse("2010-11-24T16:51:21Z")
            Assert.assertEquals(instance, extractor.uploadDate!!.date())
        }

        @Test
        fun testGetTextualUploadDate() {
            Assert.assertEquals("2010-11-24T16:51:21Z", extractor.textualUploadDate)
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
            Assert.assertEquals("The Roman Way | 4. Filling the Mind", extractor.name)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetDescription() {
            Assert.assertNotNull("description is missing", extractor.description.content)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderName() {
            Assert.assertEquals("Radio 4", extractor.uploaderName)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderUrl() {
            ExtractorAsserts.assertIsSecureUrl(extractor.uploaderUrl)
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_four&offset=0", extractor.uploaderUrl)

        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderAvatarUrl() {
            ExtractorAsserts.assertIsSecureUrl(extractor.uploaderAvatarUrl)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetLength() {
            Assert.assertEquals(1800, extractor.length)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetThumbnailUrl() {
            ExtractorAsserts.assertIsSecureUrl(extractor.thumbnailUrl)
        }

        @Test
        @Throws(ParsingException::class)
        fun testStreamType() {
            Assert.assertTrue(extractor.streamType == StreamType.AUDIO_STREAM)
        }

        @Test
        fun testGetAudioStreams() {
            Assert.assertTrue(extractor.audioStreams.isNotEmpty() && extractor.audioStreams[0].deliveryFormat != null)
        }

        @Test
        fun testGetSubChannelStuff() {
            Assert.assertEquals("The Roman Way", extractor.subChannelName)
            Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?container=b00wnmlf&offset=0", extractor.subChannelUrl)
            Assert.assertEquals("https://ichef.bbci.co.uk/images/ic/320x320/p01l672f.jpg", extractor.subChannelAvatarUrl)
        }
    }

}