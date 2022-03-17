package org.schabi.newpipe.extractor.localization;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a country that should be used when fetching content.
 * <p>
 * YouTube, for example, give different results in their feed depending on which country is
 * selected.
 * </p>
 */
public class ContentCountry implements Serializable {
    public static final ContentCountry DEFAULT =
            new ContentCountry(Localization.DEFAULT.getCountryCode());

    @Nonnull
    private final String countryCode;

    public static List<ContentCountry> listFrom(final String... countryCodeList) {
        final List<ContentCountry> toReturn = new ArrayList<>();
        for (final String countryCode : countryCodeList) {
            toReturn.add(new ContentCountry(countryCode));
        }
        return Collections.unmodifiableList(toReturn);
    }

    public ContentCountry(@Nonnull final String countryCode) {
        this.countryCode = countryCode;
    }

    @Nonnull
    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public String toString() {
        return getCountryCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentCountry)) {
            return false;
        }

        final ContentCountry that = (ContentCountry) o;

        return countryCode.equals(that.countryCode);
    }

    @Override
    public int hashCode() {
        return countryCode.hashCode();
    }
}
