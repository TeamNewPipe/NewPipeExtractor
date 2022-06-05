package org.schabi.newpipe.extractor.services.youtube.itag.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.ItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.simpleimpl.SimpleItagDeliveryDataBuilder;

public abstract class AbstractItagFormat implements ItagFormat {

    private final int id;
    private final ItagFormatDeliveryData deliveryData;

    protected AbstractItagFormat(final int id, final ItagFormatDeliveryData deliveryData) {
        this.id = id;
        this.deliveryData = deliveryData;
    }

    protected AbstractItagFormat(final int id) {
        this(id, SimpleItagDeliveryDataBuilder.progressiveHTTP());
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public ItagFormatDeliveryData deliveryData() {
        return deliveryData;
    }
}
