package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DateUtils {
    private static Date tryParseDateTimeString(String dateTime) throws ParsingException {
        // Since SimpleDateFormat::parse is not thread-safe, we create new SimpleDateFormat instances here
        List<SimpleDateFormat> possibleDateTimeFormats = Arrays.asList(
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00"),    // Youtube RSS feed
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),       // SoundCloud API
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss +0000"),      // SoundCloud API
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")    // MediaCCC API
        );

        ParseException ex = null;
        for (SimpleDateFormat format: possibleDateTimeFormats) {
            try {
                return format.parse(dateTime);
            } catch (ParseException e) {
                ex = e;
            }
        }
        throw new ParsingException(ex.getMessage(), ex);
    }

    /**
     * Parse a datetime string and convert it to "yyyy-MM-dd'T'HH:mm:ss'Z'" format
     * @param dateTime          Any datetime string matches formats inside `possibleDateTimeFormats`
     * @return                  ISO datetime string
     * @throws ParsingException Throw parsing exception if the datetime string does not match
     *                          formats in `possibleDateTimeFormats`
     */
    public static String toISODateTimeString(String dateTime) throws ParsingException {
        Date date = tryParseDateTimeString(dateTime);
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
    }
}
