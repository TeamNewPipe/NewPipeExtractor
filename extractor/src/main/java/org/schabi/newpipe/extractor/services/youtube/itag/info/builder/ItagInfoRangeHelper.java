package org.schabi.newpipe.extractor.services.youtube.itag.info.builder;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.services.youtube.itag.info.ItagInfoRange;

import javax.annotation.Nonnull;

public final class ItagInfoRangeHelper {
    private ItagInfoRangeHelper() {
        // No impl
    }

    public static ItagInfoRange buildFrom(@Nonnull final JsonObject jsonRangeObj) {
        try {
            return new ItagInfoRange(
                    Integer.parseInt(jsonRangeObj.getString("start", "-1")),
                    Integer.parseInt(jsonRangeObj.getString("end", "-1"))
            );
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }
}
