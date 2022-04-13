package org.schabi.newpipe.extractor;

/*
 * Created by Adam Howard on 08/11/15.
 *
 * Copyright (c) Christian Schabesberger <chris.schabesberger@mailbox.org>
 *     and Adam Howard <achdisposable1@gmail.com> 2015
 *
 * MediaFormat.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Arrays;
import java.util.function.Function;

/**
 * Static data about various media formats support by NewPipe, eg mime type, extension
 */

@SuppressWarnings("MethodParamPad") // we want the media format table below to be aligned
public enum MediaFormat {
    // @formatter:off
    //video and audio combined formats
    //         id     name         suffix  mimeType
    MPEG_4    (0x0,   "MPEG-4",    "mp4",  "video/mp4"),
    v3GPP     (0x10,  "3GPP",      "3gp",  "video/3gpp"),
    WEBM      (0x20,  "WebM",      "webm", "video/webm"),
    // audio formats
    M4A       (0x100, "m4a",       "m4a",  "audio/mp4"),
    WEBMA     (0x200, "WebM",      "webm", "audio/webm"),
    MP3       (0x300, "MP3",       "mp3",  "audio/mpeg"),
    OPUS      (0x400, "opus",      "opus", "audio/opus"),
    OGG       (0x500, "ogg",       "ogg",  "audio/ogg"),
    WEBMA_OPUS(0x200, "WebM Opus", "webm", "audio/webm"),
    // subtitles formats
    VTT        (0x1000, "WebVTT",                     "vtt",  "text/vtt"),
    TTML       (0x2000, "Timed Text Markup Language", "ttml", "application/ttml+xml"),
    TRANSCRIPT1(0x3000, "TranScript v1",              "srv1", "text/xml"),
    TRANSCRIPT2(0x4000, "TranScript v2",              "srv2", "text/xml"),
    TRANSCRIPT3(0x5000, "TranScript v3",              "srv3", "text/xml"),
    SRT        (0x6000, "SubRip file format",         "srt",  "text/srt");
    // @formatter:on

    public final int id;
    public final String name;
    public final String suffix;
    public final String mimeType;

    MediaFormat(final int id, final String name, final String suffix, final String mimeType) {
        this.id = id;
        this.name = name;
        this.suffix = suffix;
        this.mimeType = mimeType;
    }

    private static <T> T getById(final int id,
                                 final Function<MediaFormat, T> field,
                                 final T orElse) {
        return Arrays.stream(MediaFormat.values())
                .filter(mediaFormat -> mediaFormat.id == id)
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
    public static String getNameById(final int id) {
        return getById(id, MediaFormat::getName, "");
    }

    /**
     * Return the file extension of the media format with the supplied id
     *
     * @param id the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the file extension of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    public static String getSuffixById(final int id) {
        return getById(id, MediaFormat::getSuffix, "");
    }

    /**
     * Return the MIME type of the media format with the supplied id
     *
     * @param id the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the MIME type of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    public static String getMimeById(final int id) {
        return getById(id, MediaFormat::getMimeType, null);
    }

    /**
     * Return the MediaFormat with the supplied mime type
     *
     * @return MediaFormat associated with this mime type,
     * or null if none match it.
     */
    public static MediaFormat getFromMimeType(final String mimeType) {
        return Arrays.stream(MediaFormat.values())
                .filter(mediaFormat -> mediaFormat.mimeType.equals(mimeType))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the media format by its id.
     *
     * @param id the id
     * @return the id of the media format or null.
     */
    public static MediaFormat getFormatById(final int id) {
        return getById(id, mediaFormat -> mediaFormat, null);
    }

    public static MediaFormat getFromSuffix(final String suffix) {
        return Arrays.stream(MediaFormat.values())
                .filter(mediaFormat -> mediaFormat.suffix.equals(suffix))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the name of the format
     *
     * @return the name of the format
     */
    public String getName() {
        return name;
    }

    /**
     * Get the filename extension
     *
     * @return the filename extension
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Get the mime type
     *
     * @return the mime type
     */
    public String getMimeType() {
        return mimeType;
    }

}
