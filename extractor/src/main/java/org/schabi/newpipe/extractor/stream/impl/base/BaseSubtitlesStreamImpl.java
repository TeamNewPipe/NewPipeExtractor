package org.schabi.newpipe.extractor.stream.impl.base;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.mediaformat.SubtitlesMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class BaseSubtitlesStreamImpl extends BaseDemuxedStreamImpl implements BaseSubtitlesStream {

    @Nonnull
    private final SubtitlesMediaFormat mediaFormat;
    @Nonnull
    private final List<BaseSubtitlesStream> translations;

    public BaseSubtitlesStreamImpl(@Nullable final String id,
                                   @Nullable final String name,
                                   @Nullable final Locale locale,
                                   @Nullable final Boolean isAutomaticallyServiceGenerated,
                                   @Nonnull final SubtitlesMediaFormat mediaFormat,
                                   @Nonnull final List<BaseSubtitlesStream> translations) {
        super(id, name, locale, isAutomaticallyServiceGenerated);
        this.mediaFormat = mediaFormat;
        this.translations = translations;
    }

    @Nonnull
    @Override
    public SubtitlesMediaFormat getMediaFormat() {
        return mediaFormat;
    }

    @Nonnull
    @Override
    public List<? extends BaseSubtitlesStream> getTranslations() {
        return translations;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseSubtitlesStreamImpl that) || !super.equals(o)) {
            return false;
        }

        return mediaFormat.equals(that.mediaFormat) && translations.equals(that.translations);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mediaFormat.hashCode();
        result = 31 * result + translations.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BaseSubtitlesStreamImpl{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + mediaFormat
                + ", translations=" + translations
                + "}";
    }
}
