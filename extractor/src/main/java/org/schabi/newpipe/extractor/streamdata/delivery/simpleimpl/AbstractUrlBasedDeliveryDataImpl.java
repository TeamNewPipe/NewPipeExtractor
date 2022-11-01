package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.UrlBasedDeliveryData;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractUrlBasedDeliveryDataImpl extends AbstractDeliveryDataImpl
    implements UrlBasedDeliveryData {

    @Nonnull
    private final String url;

    protected AbstractUrlBasedDeliveryDataImpl(@Nonnull final String url) {
        this.url = Objects.requireNonNull(url);
    }

    @Nonnull
    @Override
    public String url() {
        return url;
    }

}
