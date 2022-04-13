package org.schabi.newpipe.extractor.subscription;

import java.io.Serializable;

public class SubscriptionItem implements Serializable {
    private final int serviceId;
    private final String url;
    private final String name;

    public SubscriptionItem(final int serviceId, final String url, final String name) {
        this.serviceId = serviceId;
        this.url = url;
        this.name = name;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "[name=" + name + " > " + serviceId + ":" + url + "]";
    }
}
