package org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.simpleimpl.SimpleItagDeliveryDataBuilder;
import org.schabi.newpipe.extractor.services.youtube.itag.format.ItagFormat;
import org.schabi.newpipe.extractor.streamdata.format.MediaFormat;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractSimpleItagFormat<M extends MediaFormat> implements ItagFormat<M> {

    private final int id;
    private final M mediaFormat;
    private final ItagFormatDeliveryData deliveryData;

    protected AbstractSimpleItagFormat(
            final int id,
            final M mediaFormat,
            final ItagFormatDeliveryData deliveryData) {
        this.id = id;
        this.mediaFormat = Objects.requireNonNull(mediaFormat);
        this.deliveryData = Objects.requireNonNull(deliveryData);
    }

    protected AbstractSimpleItagFormat(final int id, final M mediaFormat) {
        this(id, mediaFormat, SimpleItagDeliveryDataBuilder.progressiveHTTP());
    }

    @Override
    public int id() {
        return id;
    }

    @Nonnull
    @Override
    public M mediaFormat() {
        return mediaFormat;
    }

    @Override
    public ItagFormatDeliveryData deliveryData() {
        return deliveryData;
    }
}
