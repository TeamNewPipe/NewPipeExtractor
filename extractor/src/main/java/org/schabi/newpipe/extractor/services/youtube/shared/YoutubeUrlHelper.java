package org.schabi.newpipe.extractor.services.youtube.shared;

import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

public final class YoutubeUrlHelper {
    private YoutubeUrlHelper() {
        // No impl
    }

    public static boolean isGoogleURL(final String url) {
        final String cachedUrl = extractCachedUrlIfNeeded(url);
        try {
            final URL u = new URL(cachedUrl);
            final String host = Utils.removeMAndWWWFromHost(u.getHost());
            return host.startsWith("google.");
        } catch (final MalformedURLException e) {
            return false;
        }
    }

    public static boolean isYoutubeURL(@Nonnull final URL url) {
        final String host = Utils.removeMAndWWWFromHost(url.getHost());
        return host.equalsIgnoreCase("youtube.com")
                || host.equalsIgnoreCase("music.youtube.com");
    }

    public static boolean isYoutubeServiceURL(@Nonnull final URL url) {
        final String host = Utils.removeMAndWWWFromHost(url.getHost());
        return host.equalsIgnoreCase("youtube-nocookie.com")
                || host.equalsIgnoreCase("youtu.be");
    }

    public static boolean isHooktubeURL(@Nonnull final URL url) {
        final String host = Utils.removeMAndWWWFromHost(url.getHost());
        return host.equalsIgnoreCase("hooktube.com");
    }

    public static boolean isInvidiousURL(@Nonnull final URL url) {
        // Valid, working instances as of 2022-03
        final Set<String> invInstances = new HashSet<>(Arrays.asList(
                "invidious.snopyta.org",
                "yewtu.be",
                "tube.connect.cafe",
                "tubus.eduvid.org",
                "invidious.kavin.rocks",
                "invidious-us.kavin.rocks",
                "piped.kavin.rocks", // Not sure why this is here - it's not an invidious instance
                "invidious.fdn.fr",
                "invidious.zee.li",
                "vid.puffyan.us",
                "invidious.namazso.eu",
                "inv.riverside.rocks",
                "ytb.trom.tf",
                "invidio.xamh.de",
                "y.com.cm"
        ));
        final String host = Utils.removeMAndWWWFromHost(url.getHost()).toLowerCase();
        return invInstances.contains(host);
    }

    public static boolean isY2ubeURL(@Nonnull final URL url) {
        return Utils.removeMAndWWWFromHost(url.getHost()).equalsIgnoreCase("y2u.be");
    }

    /**
     * Sometimes, YouTube provides URLs which use Google's cache. They look like
     * {@code https://webcache.googleusercontent.com/search?q=cache:CACHED_URL}
     *
     * @param url the URL which might refer to the Google's webcache
     * @return the URL which is referring to the original site
     */
    public static String extractCachedUrlIfNeeded(final String url) {
        if (url == null) {
            return null;
        }
        if (url.contains("webcache.googleusercontent.com")) {
            return url.split("cache:")[1];
        }
        return url;
    }
}
