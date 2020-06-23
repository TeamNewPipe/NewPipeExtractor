package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class BSSearchQHFTest {

    companion object {
        @JvmStatic
        private lateinit var linkHandler: BSSearchQueryHandlerFactory

        @JvmStatic
        @BeforeClass
        fun setUp() {
            linkHandler = BSSearchQueryHandlerFactory
        }
    }

    @Test
    fun testSearchQueryUrl() {
        Assert.assertEquals("https://rms.api.bbc.co.uk/v2/experience/inline/search?q=breakfast", linkHandler.fromQuery("breakfast").url)
    }
}