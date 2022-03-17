package org.schabi.newpipe.extractor.utils;

import java.net.MalformedURLException;

/**
 * Note: this class seems unused? Should it be removed?
 */
public final class DonationLinkHelper {
    public enum DonationService {
        NO_DONATION,
        PATREON,
        PAYPAL,
    }

    public enum AffiliateService {
        NO_AFFILIATE,
        AMAZON,
    }

    private DonationLinkHelper() {
    }

    public static DonationService getDonationServiceByLink(final String link)
            throws MalformedURLException {
        switch (Utils.stringToURL(link).getHost()) {
            case "www.patreon.com":
            case "patreon.com":
                return DonationService.PATREON;
            case "www.paypal.me":
            case "paypal.me":
                return DonationService.PAYPAL;
            default:
                return DonationService.NO_DONATION;
        }
    }

    public static AffiliateService getAffiliateServiceByLink(final String link)
            throws MalformedURLException {
        if ("amzn.to".equals(Utils.stringToURL(link).getHost())) {
            return AffiliateService.AMAZON;
        }
        return AffiliateService.NO_AFFILIATE;
    }
}
