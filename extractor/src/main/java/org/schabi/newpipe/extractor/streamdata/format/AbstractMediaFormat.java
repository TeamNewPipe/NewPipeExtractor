package org.schabi.newpipe.extractor.streamdata.format;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractMediaFormat implements MediaFormat {
    private final int id;
    private final String name;
    private final String suffix;
    private final String mimeType;

    protected AbstractMediaFormat(
            final int id,
            @Nonnull final String name,
            @Nonnull final String suffix,
            @Nonnull final String mimeType
    ) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.suffix = Objects.requireNonNull(suffix);
        this.mimeType = Objects.requireNonNull(mimeType);
    }

    @Override
    public int id() {
        return id;
    }

    @Nonnull
    @Override
    public String name() {
        return name;
    }

    @Nonnull
    @Override
    public String suffix() {
        return suffix;
    }

    @Nonnull
    @Override
    public String mimeType() {
        return mimeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractMediaFormat)) return false;
        final AbstractMediaFormat that = (AbstractMediaFormat) o;
        return id() == that.id()
                && Objects.equals(name(), that.name())
                && Objects.equals(suffix(), that.suffix())
                && Objects.equals(mimeType(), that.mimeType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id(), name(), suffix(), mimeType());
    }
}
