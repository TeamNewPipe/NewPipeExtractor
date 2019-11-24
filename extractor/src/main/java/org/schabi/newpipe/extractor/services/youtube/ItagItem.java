package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import static org.schabi.newpipe.extractor.MediaFormat.*;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType.*;

public class ItagItem {
    /**
     * List can be found here https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py#L360
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
            // Don't add VideoOnly streams that have normal variants
            new ItagItem(160, VIDEO_ONLY, MPEG_4, "144p"),
            new ItagItem(133, VIDEO_ONLY, MPEG_4, "240p"),
//          new ItagItem(134, VIDEO_ONLY, MPEG_4, "360p"),
            new ItagItem(135, VIDEO_ONLY, MPEG_4, "480p"),
            new ItagItem(212, VIDEO_ONLY, MPEG_4, "480p"),
//          new ItagItem(136, VIDEO_ONLY, MPEG_4, "720p"),
            new ItagItem(298, VIDEO_ONLY, MPEG_4, "720p60", 60),
            new ItagItem(137, VIDEO_ONLY, MPEG_4, "1080p"),
            new ItagItem(299, VIDEO_ONLY, MPEG_4, "1080p60", 60),
            new ItagItem(266, VIDEO_ONLY, MPEG_4, "2160p"),

            new ItagItem(278, VIDEO_ONLY, WEBM, "144p"),
            new ItagItem(242, VIDEO_ONLY, WEBM, "240p"),
//          new ItagItem(243, VIDEO_ONLY, WEBM, "360p"),
            new ItagItem(244, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(245, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(246, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(247, VIDEO_ONLY, WEBM, "720p"),
            new ItagItem(248, VIDEO_ONLY, WEBM, "1080p"),
            new ItagItem(271, VIDEO_ONLY, WEBM, "1440p"),
            // #272 is either 3840x2160 (e.g. RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            new ItagItem(272, VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(302, VIDEO_ONLY, WEBM, "720p60", 60),
            new ItagItem(303, VIDEO_ONLY, WEBM, "1080p60", 60),
            new ItagItem(308, VIDEO_ONLY, WEBM, "1440p60", 60),
            new ItagItem(313, VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(315, VIDEO_ONLY, WEBM, "2160p60", 60)
    };
    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public static boolean isSupported(int itag) {
        for (ItagItem item : ITAG_LIST) {
            if (itag == item.id) {
                return true;
            }
        }
        return false;
    }

    public static ItagItem getItag(int itagId) throws ParsingException {
        for (ItagItem item : ITAG_LIST) {
            if (itagId == item.id) {
                return item;
            }
        }
        throw new ParsingException("itag=" + Integer.toString(itagId) + " not supported");
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

}
