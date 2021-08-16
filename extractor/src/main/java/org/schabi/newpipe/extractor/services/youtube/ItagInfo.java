package org.schabi.newpipe.extractor.services.youtube;

import java.io.Serializable;

public class ItagInfo implements Serializable {
    public String content;
    public ItagItem itagItem;
    public boolean isUrl;

    public ItagInfo(final String content,
                    final ItagItem itagItem,
                    final boolean isUrl) {
        this.content = content;
        this.itagItem = itagItem;
        this.isUrl = isUrl;
    }
}
