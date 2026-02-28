package org.schabi.newpipe.extractor.channel.tabs.rendererlist;

import org.schabi.newpipe.extractor.InfoItem;

public class RendererListInfoItem extends InfoItem {

    private String title;
    private int parentListIndex;

    private String rendererListItemType;

    public RendererListInfoItem(final int serviceId, final String url, final String name) {
        super(InfoType.RENDERERLIST, serviceId, url, name);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getParentListIndex() {
        return parentListIndex;
    }

    public void setParentListIndex(final int parentListIndex) {
        this.parentListIndex = parentListIndex;
    }

    public String getRendererListItemType() {
        return this.rendererListItemType;
    }

    public void setRendererListItemType(final String rendererListItemType) {
        this.rendererListItemType = rendererListItemType;
    }
}
