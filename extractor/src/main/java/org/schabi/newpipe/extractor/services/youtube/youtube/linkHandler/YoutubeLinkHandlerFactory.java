package org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeLinkHandlerFactory;

import java.net.URL;

public interface YoutubeLinkHandlerFactory extends YoutubeLikeLinkHandlerFactory {

    @Override
    default boolean isInvidiousUrl(final URL url) {
        return YoutubeUrlHelper.isInvidiousURL(url);
    }
}
