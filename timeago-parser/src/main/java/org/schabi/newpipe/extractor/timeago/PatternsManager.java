package org.schabi.newpipe.extractor.timeago;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class PatternsManager {
    /**
     * Return an holder object containing all the patterns array.
     *
     * @return an object containing the patterns. If not existent, {@code null}.
     */
    @Nullable
    public static PatternsHolder getPatterns(@Nonnull String languageCode, @Nullable String countryCode) {
        final String targetLocalizationClassName = languageCode +
                (countryCode == null || countryCode.isEmpty() ? "" : "_" + countryCode);

        try {
            final Class<?> targetClass = Class.forName(
                    "org.schabi.newpipe.extractor.timeago.patterns." + targetLocalizationClassName);

            return (PatternsHolder) targetClass.getDeclaredMethod("getInstance").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // Target localization is not supported
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
