package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import static org.schabi.newpipe.extractor.MediaFormat.*;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType.*;

public class ItagItem {
    /**
     * List can be found here https://github.com/ytdl-org/youtube-dl/blob/9fc5eafb8e384453a49f7cfe73147be491f0b19d/youtube_dl/extractor/youtube.py#L1071
     */
    private static final ItagItem[] ITAG_LIST = {
            /////////////////////////////////////////////////////
            // VIDEO     ID  Type   Format  Resolution  FPS  ///
            ///////////////////////////////////////////////////
            new ItagItem(17, VIDEO, v3GPP, "144p"),
            new ItagItem(36, VIDEO, v3GPP, "240p"),

            new ItagItem(18, VIDEO, MPEG_4, "360p"),
            new ItagItem(34, VIDEO, MPEG_4, "360p"),
            new ItagItem(35, VIDEO, MPEG_4, "480p"),
            new ItagItem(59, VIDEO, MPEG_4, "480p"),
            new ItagItem(78, VIDEO, MPEG_4, "480p"),
            new ItagItem(22, VIDEO, MPEG_4, "720p"),
            new ItagItem(37, VIDEO, MPEG_4, "1080p"),
            new ItagItem(38, VIDEO, MPEG_4, "1080p"),

            new ItagItem(43, VIDEO, WEBM, "360p"),
            new ItagItem(44, VIDEO, WEBM, "480p"),
            new ItagItem(45, VIDEO, WEBM, "720p"),
            new ItagItem(46, VIDEO, WEBM, "1080p"),

            ////////////////////////////////////////////////////////////////////
            // AUDIO     ID      ItagType          Format        Bitrate    ///
            //////////////////////////////////////////////////////////////////
            new ItagItem(171, AUDIO, WEBMA, 128),
            new ItagItem(172, AUDIO, WEBMA, 256),
            new ItagItem(139, AUDIO, M4A, 48),
            new ItagItem(140, AUDIO, M4A, 128),
            new ItagItem(141, AUDIO, M4A, 256),
            new ItagItem(249, AUDIO, WEBMA_OPUS, 50),
            new ItagItem(250, AUDIO, WEBMA_OPUS, 70),
            new ItagItem(251, AUDIO, WEBMA_OPUS, 160),

            /// VIDEO ONLY ////////////////////////////////////////////
            //           ID      Type     Format  Resolution  FPS  ///
            /////////////////////////////////////////////////////////
            new ItagItem(160, VIDEO_ONLY, MPEG_4, "144p"),
            new ItagItem(133, VIDEO_ONLY, MPEG_4, "240p"),
            new ItagItem(134, VIDEO_ONLY, MPEG_4, "360p"),
            new ItagItem(135, VIDEO_ONLY, MPEG_4, "480p"),
            new ItagItem(212, VIDEO_ONLY, MPEG_4, "480p"),
            new ItagItem(136, VIDEO_ONLY, MPEG_4, "720p"),
            new ItagItem(298, VIDEO_ONLY, MPEG_4, "720p60", 60),
            new ItagItem(137, VIDEO_ONLY, MPEG_4, "1080p"),
            new ItagItem(299, VIDEO_ONLY, MPEG_4, "1080p60", 60),
            new ItagItem(266, VIDEO_ONLY, MPEG_4, "2160p"),
            new ItagItem(402, VIDEO_ONLY, MPEG_4, "4320p"), // can be 4320p60 as well
            new ItagItem(571, VIDEO_ONLY, MPEG_4, "4320p"), // can be 4320p60 HDR as well (1La4QzGeaaQ)
            new ItagItem(402, VIDEO_ONLY, MPEG_4, "4320p60"),

            new ItagItem(278, VIDEO_ONLY, WEBM, "144p"),
            new ItagItem(242, VIDEO_ONLY, WEBM, "240p"),
            new ItagItem(243, VIDEO_ONLY, WEBM, "360p"),
            new ItagItem(244, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(245, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(246, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(247, VIDEO_ONLY, WEBM, "720p"),
            new ItagItem(248, VIDEO_ONLY, WEBM, "1080p"),
            new ItagItem(271, VIDEO_ONLY, WEBM, "1440p"),
            new ItagItem(302, VIDEO_ONLY, WEBM, "720p60", 60),
            new ItagItem(303, VIDEO_ONLY, WEBM, "1080p60", 60),
            new ItagItem(308, VIDEO_ONLY, WEBM, "1440p60", 60),
            new ItagItem(313, VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(315, VIDEO_ONLY, WEBM, "2160p60", 60),
            new ItagItem(272, VIDEO_ONLY, WEBM, "4320p60", 60)
    };

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @Deprecated
    public static ItagItem getItag(int itagId) throws ParsingException {
        for (ItagItem item : ITAG_LIST) {
            if (itagId == item.id) {
                return item;
            }
        }
        throw new ParsingException("itag=" + itagId + " not supported");
    }

    public static ItagItem getItag(int itagId, int averageBitrate, int fps, String qualityLabel, String mimeType) throws ParsingException {

        String[] split = mimeType.split(";")[0].split("/");
        String streamType = split[0];
        String fileType = split[1];
        String codec = mimeType.split("\"")[1];

        MediaFormat format = null;
        ItagType itagType = null;

        if (codec.contains(",")) // muxed streams have both an audio and video codec
            itagType = VIDEO;
        else {
            if (streamType.equals("video"))
                itagType = VIDEO_ONLY;
            if (streamType.equals("audio"))
                itagType = AUDIO;
        }

        if (itagType == AUDIO) {
            if (fileType.equals("mp4") && (codec.startsWith("m4a") || codec.startsWith("mp4a") ))
                format = M4A;
            if (fileType.startsWith("webm") && codec.equals("opus"))
                format = WEBMA_OPUS;
        }

        if (itagType == VIDEO) {
            if (fileType.equals("mp4"))
                format = MPEG_4;
            if(fileType.equals("3gpp"))
                format = v3GPP;
        }

        if (itagType == VIDEO_ONLY) {
            if (fileType.equals("mp4"))
                format = MPEG_4;
            if (fileType.equals("webm"))
                format = WEBM;
        }

        if (itagType == null || format == null)
            throw new ParsingException("Unknown mimeType: " + mimeType);

        return itagType == AUDIO ? new ItagItem(itagId, itagType, format, Math.round(averageBitrate / 1024f)) : new ItagItem(itagId, itagType, format, qualityLabel, fps);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contructors and misc
    //////////////////////////////////////////////////////////////////////////*/

    public enum ItagType {
        AUDIO,
        VIDEO,
        VIDEO_ONLY
    }

    /**
     * Call {@link #ItagItem(int, ItagType, MediaFormat, String, int)} with the fps set to 30.
     */
    public ItagItem(int id, ItagType type, MediaFormat format, String resolution) {
        this.id = id;
        this.itagType = type;
        this.mediaFormat = format;
        this.resolutionString = resolution;
        this.fps = 30;
    }

    /**
     * Constructor for videos.
     *
     * @param resolution string that will be used in the frontend
     */
    public ItagItem(int id, ItagType type, MediaFormat format, String resolution, int fps) {
        this.id = id;
        this.itagType = type;
        this.mediaFormat = format;
        this.resolutionString = resolution;
        this.fps = fps;
    }

    public ItagItem(int id, ItagType type, MediaFormat format, int avgBitrate) {
        this.id = id;
        this.itagType = type;
        this.mediaFormat = format;
        this.avgBitrate = avgBitrate;
    }

    private final MediaFormat mediaFormat;


    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    public final int id;
    public final ItagType itagType;

    // Audio fields
    public int avgBitrate = -1;

    // Video fields
    public String resolutionString;
    public int fps = -1;

    // Fields for Dash
    private int bitrate;
    private int width;
    private int height;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private String quality;
    private String codec;

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getInitStart() {
        return initStart;
    }

    public void setInitStart(int initStart) {
        this.initStart = initStart;
    }

    public int getInitEnd() {
        return initEnd;
    }

    public void setInitEnd(int initEnd) {
        this.initEnd = initEnd;
    }

    public int getIndexStart() {
        return indexStart;
    }

    public void setIndexStart(int indexStart) {
        this.indexStart = indexStart;
    }

    public int getIndexEnd() {
        return indexEnd;
    }

    public void setIndexEnd(int indexEnd) {
        this.indexEnd = indexEnd;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }
}
