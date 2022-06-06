package org.schabi.newpipe.extractor.streamdata.format.registry;

import org.schabi.newpipe.extractor.streamdata.format.AbstractMediaFormat;

import java.util.Arrays;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MediaFormatRegistry<F extends AbstractMediaFormat> {

    protected final F[] values;

    protected MediaFormatRegistry(final F[] values) {
        this.values = values;
    }

    public F[] values() {
        return values;
    }

    public <T> T getById(final int id,
                         final Function<F, T> field,
                         final T orElse) {
        return Arrays.stream(values())
                .filter(mediaFormat -> mediaFormat.id() == id)
                .map(field)
                .findFirst()
                .orElse(orElse);
    }

    /**
     * Return the friendly name of the media format with the supplied id
     *
     * @param id the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the friendly name of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    @Nonnull
    public String getNameById(final int id) {
        return getById(id, AbstractMediaFormat::name, "");
    }

    /**
     * Return the MIME type of the media format with the supplied id
     *
     * @param id the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the MIME type of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    @Nullable
    public String getMimeById(final int id) {
        return getById(id, AbstractMediaFormat::mimeType, null);
    }

    /**
     * Return the MediaFormat with the supplied mime type
     *
     * @return MediaFormat associated with this mime type,
     * or null if none match it.
     */
    @Nullable
    public F getFromMimeType(final String mimeType) {
        return Arrays.stream(values())
                .filter(mediaFormat -> mediaFormat.mimeType().equals(mimeType))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public F getFromSuffix(final String suffix) {
        return Arrays.stream(values())
                .filter(mediaFormat -> mediaFormat.suffix().equals(suffix))
                .findFirst()
                .orElse(null);
    }

}
