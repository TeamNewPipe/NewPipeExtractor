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

/**
 * Static data about various media formats support by NewPipe, eg mime type, extension
 */

public enum MediaFormat {
    //video and audio combined formats
    //           id      name    suffix  mime type
    MPEG_4      (0x0,   "MPEG-4", "mp4", "video/mp4"),
    v3GPP       (0x10,   "3GPP",   "3gp", "video/3gpp"),
    WEBM        (0x20,   "WebM",  "webm", "video/webm"),
    // audio formats
    M4A         (0x100,   "m4a",   "m4a",  "audio/mp4"),
    WEBMA       (0x200,   "WebM",  "webm", "audio/webm"),
    MP3         (0x300,   "MP3",   "mp3",  "audio/mpeg"),
    OPUS        (0x400,   "opus",  "opus", "audio/opus"),
    OGG         (0x500, "ogg", "ogg", "audio/ogg"),
    WEBMA_OPUS  (0x200,   "WebM Opus",  "webm", "audio/webm"),
    // subtitles formats
    VTT         (0x1000,   "WebVTT",                      "vtt",   "text/vtt"),
    TTML        (0x2000,   "Timed Text Markup Language",  "ttml",  "application/ttml+xml"),
    TRANSCRIPT1 (0x3000,   "TranScript v1",               "srv1",  "text/xml"),
    TRANSCRIPT2 (0x4000,   "TranScript v2",               "srv2",  "text/xml"),
    TRANSCRIPT3 (0x5000,   "TranScript v3",               "srv3",  "text/xml"),
    SRT         (0x6000,   "SubRip file format",          "srt",   "text/srt");

    public final int id;
    public final String name;
    public final String suffix;
    public final String mimeType;

    MediaFormat(int id, String name, String suffix, String mimeType) {
        this.id = id;
        this.name = name;
        this.suffix = suffix;
        this.mimeType = mimeType;
    }

    /**
     * Return the friendly name of the media format with the supplied id
     *
     * @param ident the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the friendly name of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    public static String getNameById(int ident) {
        for (MediaFormat vf : MediaFormat.values()) {
            if (vf.id == ident) return vf.name;
        }
        return "";
    }

    /**
     * Return the file extension of the media format with the supplied id
     *
     * @param ident the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the file extension of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    public static String getSuffixById(int ident) {
        for (MediaFormat vf : MediaFormat.values()) {
            if (vf.id == ident) return vf.suffix;
        }
        return "";
    }

    /**
     * Return the MIME type of the media format with the supplied id
     *
     * @param ident the id of the media format. Currently an arbitrary, NewPipe-specific number.
     * @return the MIME type of the MediaFormat associated with this ids,
     * or an empty String if none match it.
     */
    public static String getMimeById(int ident) {
        for (MediaFormat vf : MediaFormat.values()) {
            if (vf.id == ident) return vf.mimeType;
        }
        return "";
    }

    /**
     * Return the MediaFormat with the supplied mime type
     *
     * @return MediaFormat associated with this mime type,
     * or null if none match it.
     */
    public static MediaFormat getFromMimeType(String mimeType) {
        for (MediaFormat vf : MediaFormat.values()) {
            if (vf.mimeType.equals(mimeType)) return vf;
        }
        return null;
    }

    /**
     * Get the media format by its id.
     *
     * @param id the id
     * @return the id of the media format or null.
     */
    public static MediaFormat getFormatById(int id) {
        for (MediaFormat vf : values()) {
            if (vf.id == id) return vf;
        }
        return null;
    }

    public static MediaFormat getFromSuffix(String suffix) {
        for (MediaFormat vf : values()) {
            if (vf.suffix.equals(suffix)) return vf;
        }
        return null;
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
