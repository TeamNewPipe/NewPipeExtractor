package org.schabi.newpipe.extractor.channel.tabs.rendererlist;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public interface RendererListInfoItemExtractor extends InfoItemExtractor {

    /**
     * Get the list link handler of the rendererlist
     * @return the ListLinkHandler of the list
     */
    ListLinkHandler getListLinkHandler() throws ParsingException;

    /**
     * Get the item type that exist in the renderer list
     * @return the item type of the render list
     */
    String getRendererListItemType() throws ParsingException;

    /**
     * Get the uploader including tab url
     * @return the uploader including tab url
     */
    String getUploaderTabUrl() throws ParsingException;

    /**
     * Get the uploader name
     * @return the uploader name
     */
    String getUploaderName() throws ParsingException;

    /**
     * Get the uploader url
     * @return the uploader url
     */
    String getUploaderUrl() throws ParsingException;

    /**
     * Get whether the uploader is verified
     * @return whether the uploader is verified
     */
    boolean isUploaderVerified() throws ParsingException;


    static String getRendererListIndexContentFilter(final int index) {
        return "rendererlist_index=" + index;
    }
}
