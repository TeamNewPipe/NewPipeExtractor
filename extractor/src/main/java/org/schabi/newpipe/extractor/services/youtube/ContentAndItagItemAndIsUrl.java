package org.schabi.newpipe.extractor.services.youtube;

public class ContentAndItagItemAndIsUrl {
    public String content;
    public ItagItem itagItem;
    public boolean isUrl;

    public ContentAndItagItemAndIsUrl(final String content,
                                      final ItagItem itagItem,
                                      final boolean isUrl) {
        this.content = content;
        this.itagItem = itagItem;
        this.isUrl = isUrl;
    }
}
