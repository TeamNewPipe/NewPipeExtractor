package org.schabi.newpipe.extractor.services.youtube.itag;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;

public interface ItagFormat {
    int id();

    ItagFormatDeliveryData deliveryData();
}
