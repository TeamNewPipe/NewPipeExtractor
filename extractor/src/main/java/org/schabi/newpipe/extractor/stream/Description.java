package org.schabi.newpipe.extractor.stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public record Description(@Nonnull String content, @Nonnull Type type) implements Serializable {
    @Nonnull
    public static final Description EMPTY_DESCRIPTION = new Description("", Type.PLAIN_TEXT);

    public enum Type {
        HTML, MARKDOWN, PLAIN_TEXT
    }

    @Nonnull
    public static Description of(@Nullable final String content, @Nonnull final Type type) {
        if (isNullOrEmpty(content)) {
            return EMPTY_DESCRIPTION;
        }
        return new Description(content, type);
    }
}
