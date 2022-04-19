package org.schabi.newpipe.extractor.services.youtube.shared.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper;

import java.net.URL;

public interface YoutubeLikeLinkHandlerFactory {

    default boolean isSupportedYouTubeLikeHost(final URL url) {
        return YoutubeUrlHelper.isYoutubeURL(url)
                || YoutubeUrlHelper.isYoutubeServiceURL(url)
                || isInvidiousUrl(url)
                || YoutubeUrlHelper.isHooktubeURL(url)
                || YoutubeUrlHelper.isY2ubeURL(url);
    }

    boolean isInvidiousUrl(final URL url);
}
