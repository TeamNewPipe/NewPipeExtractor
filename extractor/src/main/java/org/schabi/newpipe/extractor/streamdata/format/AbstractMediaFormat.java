package org.schabi.newpipe.extractor.streamdata.format;

import java.util.Objects;

public abstract class AbstractMediaFormat implements MediaFormat {
    private final int id;
    private final String name;
    private final String suffix;
    private final String mimeType;

    protected AbstractMediaFormat(
            final int id,
            final String name,
            final String suffix,
            final String mimeType
    ) {
        this.id = id;
        this.name = name;
        this.suffix = suffix;
        this.mimeType = mimeType;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String suffix() {
        return suffix;
    }

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
