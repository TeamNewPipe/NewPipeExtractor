package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.instance.AbstractInstance;

/**
 * A YouTube-like instance (or proxy)
 *
 * @param <S>
 */
public abstract class YoutubeLikeInstance<S extends YoutubeLikeStreamingService>
        extends AbstractInstance {

    protected YoutubeLikeInstance(final String url, final String name) {
        super(url, name);
    }

    public abstract S getNewStreamingService(int id);
}
