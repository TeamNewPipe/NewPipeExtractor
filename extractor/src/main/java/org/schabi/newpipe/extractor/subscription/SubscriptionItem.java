package org.schabi.newpipe.extractor.subscription;

import java.io.Serializable;

public record SubscriptionItem(int serviceId, String url, String name) implements Serializable {
    public static final SubscriptionItem INVALID =
            new SubscriptionItem(Integer.MIN_VALUE, "Invalid", "Invalid");
}
