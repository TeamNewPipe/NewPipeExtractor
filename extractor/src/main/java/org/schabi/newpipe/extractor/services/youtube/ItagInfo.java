package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nonnull;
import java.io.Serializable;

public final class ItagInfo implements Serializable {

    @Nonnull
    private final String content;
    @Nonnull
    private final ItagItem itagItem;
    private boolean isUrl;

    public ItagInfo(@Nonnull final String content,
                    @Nonnull final ItagItem itagItem) {
        this.content = content;
        this.itagItem = itagItem;
    }

    public void setIsUrl(final boolean isUrl) {
        this.isUrl = isUrl;
    }

    @Nonnull
    public String getContent() {
        return content;
    }

    @Nonnull
    public ItagItem getItagItem() {
        return itagItem;
    }

    public boolean getIsUrl() {
        return isUrl;
    }
}
