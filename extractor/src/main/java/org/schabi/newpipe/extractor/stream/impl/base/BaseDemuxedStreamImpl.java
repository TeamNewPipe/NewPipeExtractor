package org.schabi.newpipe.extractor.stream.impl.base;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseDemuxedStream;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

public abstract class BaseDemuxedStreamImpl implements BaseDemuxedStream {

    @Nullable
    private final String id;
    @Nullable
    private final String name;
    @Nullable
    private final Locale locale;
    @Nullable
    private final Boolean isAutomaticallyServiceGenerated;

    protected BaseDemuxedStreamImpl(@Nullable final String id,
                                    @Nullable final String name,
                                    @Nullable final Locale locale,
                                    @Nullable final Boolean isAutomaticallyServiceGenerated) {
        this.id = id;
        this.name = name;
        this.locale = locale;
        this.isAutomaticallyServiceGenerated = isAutomaticallyServiceGenerated;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public Locale getLocale() {
        return locale;
    }

    @Nullable
    @Override
    public Boolean isAutomaticallyServiceGenerated() {
        return isAutomaticallyServiceGenerated;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseDemuxedStreamImpl that)) {
            return false;
        }

        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
                && Objects.equals(locale, that.locale)
                && Objects.equals(isAutomaticallyServiceGenerated,
                that.isAutomaticallyServiceGenerated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(locale);
        result = 31 * result + Objects.hashCode(isAutomaticallyServiceGenerated);
        return result;
    }

    @Override
    public String toString() {
        return "BaseDemuxedStreamImpl{"
                + "id=" + id
                + ", name=" + name
                + ", locale=" + locale
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated
                + "}";
    }
}
