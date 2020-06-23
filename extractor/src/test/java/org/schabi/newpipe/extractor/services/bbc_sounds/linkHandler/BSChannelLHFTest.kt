package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.schabi.newpipe.extractor.exceptions.ParsingException

class BSChannelLHFTest {

    companion object {
        @JvmStatic
        private lateinit var linkHandler: BSChannelLinkHandlerFactory

        @JvmStatic
        @BeforeClass
        fun setUp() {
            linkHandler = BSChannelLinkHandlerFactory
        }
    }

    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assert.assertTrue(linkHandler.acceptUrl("https://www.bbc.co.uk/sounds/series/p02nrsln"))
        Assert.assertTrue(linkHandler.acceptUrl("https://rms.api.bbc.co.uk/v2/programmes/playable?container=p02nrsln&offset=0"))
        Assert.assertTrue(linkHandler.acceptUrl("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0"))
    }

    @Test
    @Throws(ParsingException::class)
    fun getIdFromUrl() {
        Assert.assertEquals("p02nrsln", linkHandler.fromUrl("https://www.bbc.co.uk/sounds/series/p02nrsln").id)
        Assert.assertEquals("p02nrsln", linkHandler.fromUrl("https://rms.api.bbc.co.uk/v2/programmes/playable?container=p02nrsln&offset=0").id)
        Assert.assertEquals("network:bbc_radio_one", linkHandler.fromUrl("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0").id)
    }

    @Test
    @Throws(ParsingException::class)
    fun getUrlFromId() {
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?container=p02nrsln&offset=0", linkHandler.fromId("p02nrsln").url)
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?container=p02nrsln&offset=0", linkHandler.fromId("p02nrsln").url)
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0", linkHandler.fromId("network:bbc_radio_one").url)
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/programmes/playable?network=bbc_radio_one&offset=0", linkHandler.fromNetworkId("network:bbc_radio_one").url)
    }
}