package org.schabi.newpipe.extractor.services.niconico;

import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jdk.vm.ci.meta.Local;

public class NiconicoServiceParsingHelper {
    public static DateWrapper parseSnapshotDateTime(final String textDateTime) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse(
                textDateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return new DateWrapper(zonedDateTime.toOffsetDateTime());
    }

    public static DateWrapper parseRSSDateTime(final String textDateTime) {
        final LocalDateTime localDateTime = LocalDateTime.parse(
                textDateTime, DateTimeFormatter.ofPattern("uuuu'年'MM'月'dd'日' HH'：'mm'：'ss"));
        // given datetime is
        return new DateWrapper(localDateTime.atOffset(ZoneOffset.ofHours(-9)));
    }
}
