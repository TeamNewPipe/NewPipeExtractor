package org.schabi.newpipe.extractor.channel.tabs.rendererlist;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public interface RendererListInfoItemExtractor extends InfoItemExtractor {

    /**
     * Get the list link handler of the renderer list item for extraction
     * @return the ListLinkHandler of the list
     */
    ListLinkHandler getListLinkHandler() throws ParsingException;

    /**
     * Gets the item type as a string for this render list item
     * @return the item type of the render list string
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

    /**
     * Creates a render list index that can be used in the content filter
     * @return render list index string as a content filter
     */
    static String createIndexContentFilter(final int index) {
        return "rendererlist_index=" + index;
    }
}
