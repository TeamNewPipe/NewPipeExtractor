package org.schabi.newpipe.extractor.channel.tabs.rendererlist;

import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public final class RendererListInfoItemsCollector extends
        InfoItemsCollector<RendererListInfoItem, RendererListInfoItemExtractor> {

    public RendererListInfoItemsCollector(final int serviceId) {
        super(serviceId);
    }

    @Override
    public RendererListInfoItem extract(final RendererListInfoItemExtractor extractor)
            throws ParsingException {

        final RendererListInfoItem resultItem = new RendererListInfoItem(
                    getServiceId(), extractor.getUrl(), extractor.getName());

            try {
                resultItem.setTitle(extractor.getName());
            } catch (final Exception e) {
                addError(e);
            }
            try {
                resultItem.setParentListIndex(extractor.getParentListIndex());
            } catch (final Exception e) {
                addError(e);
            }
        try {
            resultItem.setRendererListItemType(extractor.getRendererListItemType());
        } catch (final Exception e) {
            addError(e);
        }


            return resultItem;
    }
}


