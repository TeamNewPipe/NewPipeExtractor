package org.schabi.newpipe.extractor.services.media_ccc.extractors.data;

import com.grack.nanojson.JsonObject;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;


/** A recording stream of a talk/event. Switch on the implementation to get the actual data. */
public interface MediaCCCRecording {

    /** A recording stream of a talk/event.
     * These files usually have one or more audio streams in different languages. */
    class Video implements MediaCCCRecording {
        public String filename;
        public VideoType recordingType;
        public String mimeType;
        /** Each language is one separate audio track on the video. */
        public List<Locale> languages;
        public String url;
        public int lengthSeconds;
        public int width;
        public int height;
    }

    /** Some talks have multiple kinds of video. */
    enum VideoType {
        /** The main recording of a talk/event. */
        MAIN,
        /** A side-recording of a talk/event that has the slides full-screen.
         * Usually if there is a slide-recording there is a MAIN recording as well */
        SLIDES
    }

    /** An audio recording of a talk/event.
     * These audio streams are usually also available in their respective video streams.
     */
    class Audio implements MediaCCCRecording {
        public String filename;
        public String mimeType;
        public @Nullable Locale language;
        public String url;
        public int lengthSeconds;
    }

    /** A subtitle file of a talk/event. */
    class Subtitle implements MediaCCCRecording {
        public String filename;
        public String mimeType;
        public @Nullable Locale language;
        public String url;
    }

    /** The Slides of the talk, usually as PDF file. */
    class Slides implements MediaCCCRecording {
        public String filename;
        public String mimeType;
        public String url;
        public @Nullable Locale language;
    }

    /** Anything we can’t put in any of the other categories. */
    class Unknown implements MediaCCCRecording {
        public String filename;
        public String mimeType;
        public String url;
        /** The raw object for easier debugging. */
        public JsonObject rawObject;
    }
}
