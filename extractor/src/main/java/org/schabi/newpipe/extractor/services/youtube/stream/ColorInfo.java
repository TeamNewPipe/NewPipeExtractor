package org.schabi.newpipe.extractor.services.youtube.stream;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing {@code colorInfo} objects as returned by YouTube for some video streams.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public final class ColorInfo implements Serializable {

    /**
     * The {@code primaries} value returned by YouTube.
     */
    @Nullable
    public final String primaries;

    /**
     * The {@code transferCharacteristics} value returned by YouTube.
     */
    @Nullable
    public final String transferCharacteristics;

    /**
     * The {@code matrixCoefficients} value returned by YouTube.
     */
    @Nullable
    public final String matrixCoefficients;

    /**
     * Construct a new {@link ColorInfo} instance.
     *
     * @param primaries               the {@code primaries} value
     * @param transferCharacteristics the {@code transferCharacteristics} value
     * @param matrixCoefficients      the {@code matrixCoefficients} value
     */
    public ColorInfo(@Nullable final String primaries,
                     @Nullable final String transferCharacteristics,
                     @Nullable final String matrixCoefficients) {
        this.primaries = primaries;
        this.transferCharacteristics = transferCharacteristics;
        this.matrixCoefficients = matrixCoefficients;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ColorInfo that)) {
            return false;
        }

        return Objects.equals(primaries, that.primaries)
                && Objects.equals(transferCharacteristics, that.transferCharacteristics)
                && Objects.equals(matrixCoefficients, that.matrixCoefficients);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(primaries);
        result = 31 * result + Objects.hashCode(transferCharacteristics);
        result = 31 * result + Objects.hashCode(matrixCoefficients);
        return result;
    }

    @Override
    public String toString() {
        return "ColorInfo{"
                + "primaries=" + primaries
                + ", transferCharacteristics=" + transferCharacteristics
                + ", matrixCoefficients=" + matrixCoefficients
                + "}";
    }
}
