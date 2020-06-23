package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.schabi.newpipe.extractor.exceptions.ParsingException

class BSStreamLHFTest {

    companion object {
        @JvmStatic
        private lateinit var linkHandler: BSStreamLinkHandlerFactory

        @JvmStatic
        @BeforeClass
        fun setUp() {
            linkHandler = BSStreamLinkHandlerFactory
        }
    }

    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assert.assertTrue(linkHandler.acceptUrl("https://www.bbc.co.uk/sounds/play/p00cbxy3"))
        Assert.assertTrue(linkHandler.acceptUrl("https://rms.api.bbc.co.uk/v2/programmes/p00cbxy3/playable"))
        Assert.assertTrue(linkHandler.acceptUrl("https://rms.api.bbc.co.uk/v2/networks/bbc_radio_one/playable"))
    }

    @Test
    @Throws(ParsingException::class)
    fun getIdFromUrl() {
        Assert.assertEquals("p00cbxy3", linkHandler.fromUrl("https://www.bbc.co.uk/sounds/play/p00cbxy3").id)
        Assert.assertEquals("p00cbxy3", linkHandler.fromUrl("https://rms.api.bbc.co.uk/v2/programmes/p00cbxy3/playable").id)
        Assert.assertEquals("urn:bbc:radio:network:bbc_radio_one", linkHandler.fromUrl("https://rms.api.bbc.co.uk/v2/networks/bbc_radio_one/playable").id)
    }

    @Test
    @Throws(ParsingException::class)
    fun getUrlFromId() {
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/p00cbxy3/playable", linkHandler.fromId("p00cbxy3").url)
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/p00cbxy3/playable", linkHandler.fromId("p00cbxy3").url)
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/networks/bbc_radio_one/playable", linkHandler.fromId("urn:bbc:radio:network:bbc_radio_one").url)
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/networks/bbc_radio_one/playable", linkHandler.fromNetworkId("urn:bbc:radio:network:bbc_radio_one").url)
    }
}