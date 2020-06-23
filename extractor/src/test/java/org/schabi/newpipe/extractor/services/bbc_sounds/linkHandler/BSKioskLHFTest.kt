package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.schabi.newpipe.extractor.exceptions.ParsingException

class BSKioskLHFTest {

    companion object {
        @JvmStatic
        private lateinit var linkHandler: BSKioskLinkHandlerFactory

        @JvmStatic
        @BeforeClass
        fun setUp() {
            linkHandler = BSKioskLinkHandlerFactory
        }
    }

    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assert.assertTrue(linkHandler.acceptUrl("https://rms.api.bbc.co.uk/v2/networks/playable"))
        Assert.assertTrue(linkHandler.acceptUrl("https://rms.api.bbc.co.uk/v2/programmes/playable?category=mixes&offset=0"))
    }

    @Test
    @Throws(ParsingException::class)
    fun getIdFromUrl() {
        Assert.assertEquals("Radio (Live)", linkHandler.fromUrl("https://rms.api.bbc.co.uk/v2/networks/playable").id)
        Assert.assertEquals("Mixes", linkHandler.fromUrl("https://rms.api.bbc.co.uk/v2/programmes/playable?category=mixes&offset=0").id)
    }

    @Test
    @Throws(ParsingException::class)
    fun getUrlFromId() {
        Assert.assertEquals( "https://rms.api.bbc.co.uk/v2/networks/playable",linkHandler.fromId("Radio (Live)").url)
        Assert.assertEquals( "https://rms.api.bbc.co.uk/v2/programmes/playable?category=mixes&offset=0", linkHandler.fromId("Mixes").url)
    }
}