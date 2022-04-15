package org.schabi.newpipe.extractor.instance;

import org.schabi.newpipe.extractor.utils.Utils;

import java.net.URI;
import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractInstance implements Instance {

    protected Boolean valid = null;

    protected final String url;
    protected String name;

    protected AbstractInstance(final String url, final String name) {
        this.url = removeTrailingSlashes(Objects.requireNonNull(url));
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getUrl() {
        return url;
    }

    public void setName(final String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public void fetchMetadata() {
        // Default: Do nothing
    }

    public static String removeTrailingSlashes(final String input) {
        return input.replaceAll("/*$", "");
    }

    public static String tryExtractDomainFromUrl(final String url, final String fallback) {
        Objects.requireNonNull(url);
        try {
            return Utils.removeMAndWWWFromHost(new URI(url).getHost());
        } catch (final Exception ignored) {
            return fallback;
        }
    }
}
