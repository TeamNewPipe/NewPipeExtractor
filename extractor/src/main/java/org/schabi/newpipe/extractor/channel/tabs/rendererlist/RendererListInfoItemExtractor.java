package org.schabi.newpipe.extractor.channel.tabs.rendererlist;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public interface RendererListInfoItemExtractor extends InfoItemExtractor {

    /**
     * Get the index of the parent list
     * @return the index of the parent list
     */
    int getParentListIndex() throws ParsingException;

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
}
