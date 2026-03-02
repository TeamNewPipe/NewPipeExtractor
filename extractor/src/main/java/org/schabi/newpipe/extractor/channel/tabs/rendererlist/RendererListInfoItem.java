package org.schabi.newpipe.extractor.channel.tabs.rendererlist;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public class RendererListInfoItem extends InfoItem {

    private String title;
    private ListLinkHandler listLinkHandler;
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

    public ListLinkHandler getListLinkHandler() {
        return this.listLinkHandler;
    }

    public void setListLinkHandler(final ListLinkHandler listLinkHandler) {
        this.listLinkHandler = listLinkHandler;
    }

    public String getRendererListItemType() {
        return this.rendererListItemType;
    }

    public void setRendererListItemType(final String rendererListItemType) {
        this.rendererListItemType = rendererListItemType;
    }
}
