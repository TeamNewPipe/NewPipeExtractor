package org.schabi.newpipe.extractor.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class DonationLinkHelper {
    public enum DonationService {
        NO_DONATION,
        PATREON,
        PAYPAL
    }


    public static DonationService getServiceByLink(String link) throws MalformedURLException {
        URL url = new URL(link);
        switch (url.getHost()) {
            case "www.patreon.com":
                return DonationService.PATREON;
            case "patreon.com":
                return DonationService.PATREON;
            case "paypal.me":
                return DonationService.PAYPAL;
            case "www.paypal.me":
                return DonationService.PAYPAL;
            default:
                return DonationService.NO_DONATION;
        }
    }


}
