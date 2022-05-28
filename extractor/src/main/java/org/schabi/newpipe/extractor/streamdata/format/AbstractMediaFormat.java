package org.schabi.newpipe.extractor.streamdata.format;

import java.util.Objects;

public abstract class AbstractMediaFormat {
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractMediaFormat)) return false;
        final AbstractMediaFormat that = (AbstractMediaFormat) o;
        return getId() == that.getId() && Objects.equals(getName(), that.getName()) && Objects.equals(getSuffix(), that.getSuffix()) && Objects.equals(getMimeType(), that.getMimeType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getSuffix(), getMimeType());
    }
}
