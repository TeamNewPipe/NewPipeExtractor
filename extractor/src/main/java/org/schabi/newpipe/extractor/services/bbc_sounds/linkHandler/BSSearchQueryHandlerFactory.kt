package org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler

import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory

object BSSearchQueryHandlerFactory : SearchQueryHandlerFactory() {
    override fun getUrl(querry: String?, contentFilter: MutableList<String>?, sortFilter: String?): String {
        return API_URL.format(querry)
    }

    private const val API_URL = "https://rms.api.bbc.co.uk/v2/experience/inline/search?q=%s"
}