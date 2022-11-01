package org.schabi.newpipe.extractor.services.youtube.itag.delivery.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.DASHItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.HLSItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ProgressiveHTTPItagFormatDeliveryData;

public final class SimpleItagDeliveryDataBuilder {
    private SimpleItagDeliveryDataBuilder() {
        // No impl
    }

    public static ProgressiveHTTPItagFormatDeliveryData progressiveHTTP() {
        return new SimpleProgressiveHTTPItagFormatDeliveryData();
    }

    public static HLSItagFormatDeliveryData hls() {
        return new SimpleHLSItagFormatDeliveryData();
    }

    public static DASHItagFormatDeliveryData dash() {
        return new SimpleDASHItagFormatDeliveryData();
    }
}
