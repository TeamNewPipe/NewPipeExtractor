package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.URL;

public interface InvidiousLinkHandlerFactory extends YoutubeLikeLinkHandlerFactory {

    String getInvidiousBaseUrl();

    @Override
    default boolean isInvidiousUrl(final URL url) {
        return Utils.removeMAndWWWFromHost(url.getHost())
                .equalsIgnoreCase(Utils.getHostOrNull(getInvidiousBaseUrl()))
                && YoutubeUrlHelper.isInvidioURL(url);
    }
}
