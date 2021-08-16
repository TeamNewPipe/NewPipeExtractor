package org.schabi.newpipe.extractor.services.youtube;

import java.io.Serializable;

public class ItagInfo implements Serializable {
    private final String content;
    private final ItagItem itagItem;
    private boolean isUrl;

    public ItagInfo(final String content,
                    final ItagItem itagItem) {
        this.content = content;
        this.itagItem = itagItem;
    }

    public void setIsUrl(final boolean isUrl) {
        this.isUrl = isUrl;
    }

    public String getContent() {
        return content;
    }

    public ItagItem getItagItem() {
        return itagItem;
    }

    public boolean getIsUrl() {
        return isUrl;
    }
}
