package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.LocaleCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Localization implements Serializable {
    public static final Localization DEFAULT = new Localization("en", "GB");

    @Nonnull
    private final String languageCode;
    @Nullable
    private final String countryCode;

    /**
     * @param localizationCodeList a list of localization code, formatted like {@link
     *                             #getLocalizationCode()}
     */
    public static List<Localization> listFrom(final String... localizationCodeList) {
        final List<Localization> toReturn = new ArrayList<>();
        for (final String localizationCode : localizationCodeList) {
            toReturn.add(fromLocalizationCode(localizationCode));
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * @param localizationCode a localization code, formatted like {@link #getLocalizationCode()}
     */
    public static Localization fromLocalizationCode(final String localizationCode) {
        return fromLocale(LocaleCompat.forLanguageTag(localizationCode));
    }

    public Localization(@Nonnull final String languageCode, @Nullable final String countryCode) {
        this.languageCode = languageCode;
        this.countryCode = countryCode;
    }

    public Localization(@Nonnull final String languageCode) {
        this(languageCode, null);
    }

    @Nonnull
    public String getLanguageCode() {
        return languageCode;
    }

    @Nonnull
    public String getCountryCode() {
        return countryCode == null ? "" : countryCode;
    }

    public Locale asLocale() {
        return new Locale(getLanguageCode(), getCountryCode());
    }

    public static Localization fromLocale(@Nonnull final Locale locale) {
        return new Localization(locale.getLanguage(), locale.getCountry());
    }

    /**
     * Return a formatted string in the form of: {@code language-Country}, or
     * just {@code language} if country is {@code null}.
     */
    public String getLocalizationCode() {
        return languageCode + (countryCode == null ? "" : "-" + countryCode);
    }

    @Override
    public String toString() {
        return "Localization[" + getLocalizationCode() + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Localization)) {
            return false;
        }

        final Localization that = (Localization) o;

        return languageCode.equals(that.languageCode)
                && Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        int result = languageCode.hashCode();
        result = 31 * result + Objects.hashCode(countryCode);
        return result;
    }

    /**
     * Converts a three letter language code (ISO 639-2/T) to a Locale
     * because limits of Java Locale class.
     *
     * @param code a three letter language code
     * @return the Locale corresponding
     */
    public static Locale getLocaleFromThreeLetterCode(@Nonnull final String code)
            throws ParsingException {
        final String[] languages = Locale.getISOLanguages();
        final Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (final String language : languages) {
            final Locale locale = new Locale(language);
            localeMap.put(locale.getISO3Language(), locale);
        }
        if (localeMap.containsKey(code)) {
            return localeMap.get(code);
        } else {
            throw new ParsingException(
                    "Could not get Locale from this three letter language code" + code);
        }
    }
}
