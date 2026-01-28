package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 04.03.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * AudioStream.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

public class AudioStream extends Stream {
    public static final int UNKNOWN_BITRATE = -1;

    private final int averageBitrate;

    // Fields for DASH
    private int itag = ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE;
    private int bitrate;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private String quality;
    private String codec;

    // Fields about the audio track id/name
    @Nullable
    private final String audioTrackId;
    @Nullable
    private final String audioTrackName;
    @Nullable
    private final Locale audioLocale;
    @Nullable
    private final AudioTrackType audioTrackType;

    @Nullable
    private ItagItem itagItem;

    /**
     * Class to build {@link AudioStream} objects.
     */
    @SuppressWarnings("checkstyle:hiddenField")
    public static class Builder {
        private String id;
        private String content;
        private boolean isUrl;
        private DeliveryMethod deliveryMethod = DeliveryMethod.PROGRESSIVE_HTTP;
        @Nullable
        private MediaFormat mediaFormat;
        @Nullable
        private String manifestUrl;
        private int averageBitrate = UNKNOWN_BITRATE;
        @Nullable
        private String audioTrackId;
        @Nullable
        private String audioTrackName;
        @Nullable
        private Locale audioLocale;
        @Nullable
        private AudioTrackType audioTrackType;
        @Nullable
        private ItagItem itagItem;

        /**
         * Create a new {@link Builder} instance with its default values.
         */
        public Builder() {
        }

        /**
         * Set the identifier of the {@link AudioStream} which uniquely identifies the stream,
         * e.g. for YouTube this would be the itag
         *
         * <p>
         * It <b>must not be null</b> and should be non empty.
         * </p>
         *
         * <p>
         * If you are not able to get an identifier, use the static constant {@link
         * Stream#ID_UNKNOWN ID_UNKNOWN} of the {@link Stream} class.
         * </p>
         *
         * @param id the identifier of the {@link AudioStream}, which must not be null
         * @return this {@link Builder} instance
         */
        public Builder setId(@Nonnull final String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the content or the URL of the {@link AudioStream}, depending on whether isUrl is
         * true
         * <p>
         * It must not be null, and should be non empty.
         * </p>
         *
         * @param content the content of the {@link AudioStream}
         * @param isUrl   whether content is the URL or the actual content of e.g. a DASH manifest
         * @return this {@link Builder} instance
         */
        public Builder setContent(@Nonnull final String content,
                                  final boolean isUrl) {
            this.content = content;
            this.isUrl = isUrl;
            return this;
        }

        /**
         * Set the {@link MediaFormat} used by the {@link AudioStream}, which can be null
         *
         * <p>
         * It should be one of the audio {@link MediaFormat}s ({@link MediaFormat#M4A M4A},
         * {@link MediaFormat#WEBMA WEBMA}, {@link MediaFormat#MP3 MP3}, {@link MediaFormat#OPUS
         * OPUS}, {@link MediaFormat#OGG OGG}, or {@link MediaFormat#WEBMA_OPUS WEBMA_OPUS}) but
         * can be {@code null} if the media format could not be determined.
         * </p>
         *
         * <p>
         * The default value is {@code null}.
         * </p>
         *
         * @param mediaFormat the {@link MediaFormat} of the {@link AudioStream}, which can be null
         * @return this {@link Builder} instance
         */
        public Builder setMediaFormat(@Nullable final MediaFormat mediaFormat) {
            this.mediaFormat = mediaFormat;
            return this;
        }

        /**
         * Set the {@link DeliveryMethod} of the {@link AudioStream}.
         *
         * <p>
         * It must not be null.
         * </p>
         *
         * <p>
         * The default delivery method is {@link DeliveryMethod#PROGRESSIVE_HTTP}.
         * </p>
         *
         * @param deliveryMethod the {@link DeliveryMethod} of the {@link AudioStream}, which must
         *                       not be null
         * @return this {@link Builder} instance
         */
        public Builder setDeliveryMethod(@Nonnull final DeliveryMethod deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
            return this;
        }

        /**
         * Sets the URL of the manifest this stream comes from (if applicable, otherwise null).
         *
         * @param manifestUrl the URL of the manifest this stream comes from or {@code null}
         * @return this {@link Builder} instance
         */
        public Builder setManifestUrl(@Nullable final String manifestUrl) {
            this.manifestUrl = manifestUrl;
            return this;
        }

        /**
         * Set the average bitrate of the {@link AudioStream}.
         *
         * <p>
         * The default value is {@link #UNKNOWN_BITRATE}.
         * </p>
         *
         * @param averageBitrate the average bitrate of the {@link AudioStream}, which should
         *                       positive
         * @return this {@link Builder} instance
         */
        public Builder setAverageBitrate(final int averageBitrate) {
            this.averageBitrate = averageBitrate;
            return this;
        }

        /**
         * Set the audio track id of the {@link AudioStream}.
         *
         * <p>
         * The default value is {@code null}.
         * </p>
         *
         * @param audioTrackId the audio track id of the {@link AudioStream}, which can be null
         * @return this {@link Builder} instance
         */
        public Builder setAudioTrackId(@Nullable final String audioTrackId) {
            this.audioTrackId = audioTrackId;
            return this;
        }

        /**
         * Set the audio track name of the {@link AudioStream}.
         *
         * <p>
         * The default value is {@code null}.
         * </p>
         *
         * @param audioTrackName the audio track name of the {@link AudioStream}, which can be null
         * @return this {@link Builder} instance
         */
        public Builder setAudioTrackName(@Nullable final String audioTrackName) {
            this.audioTrackName = audioTrackName;
            return this;
        }

        /**
         * Set the {@link AudioTrackType} of the {@link AudioStream}.
         *
         * <p>
         * The default value is {@code null}.
         * </p>
         *
         * @param audioTrackType the audio track type of the {@link AudioStream}, which can be null
         * @return this {@link Builder} instance
         */
        public Builder setAudioTrackType(final AudioTrackType audioTrackType) {
            this.audioTrackType = audioTrackType;
            return this;
        }

        /**
         * Set the {@link Locale} of the audio which represents its language.
         *
         * <p>
         * The default value is {@code null}, which means that the {@link Locale} is unknown.
         * </p>
         *
         * @param audioLocale the {@link Locale} of the audio, which could be {@code null}
         * @return this {@link Builder} instance
         */
        public Builder setAudioLocale(@Nullable final Locale audioLocale) {
            this.audioLocale = audioLocale;
            return this;
        }

        /**
         * Set the {@link ItagItem} corresponding to the {@link AudioStream}.
         *
         * <p>
         * {@link ItagItem}s are YouTube specific objects, so they are only known for this service
         * and can be null.
         * </p>
         *
         * <p>
         * The default value is {@code null}.
         * </p>
         *
         * @param itagItem the {@link ItagItem} of the {@link AudioStream}, which can be null
         * @return this {@link Builder} instance
         */
        public Builder setItagItem(@Nullable final ItagItem itagItem) {
            this.itagItem = itagItem;
            return this;
        }

        /**
         * Build an {@link AudioStream} using the builder's current values.
         *
         * <p>
         * The identifier and the content (and thus {@code isUrl}) properties must have
         * been set.
         * </p>
         *
         * @return a new {@link AudioStream} using the builder's current values
         * @throws IllegalStateException if {@code id}, {@code content} (and thus {@code isUrl}) or
         * {@code deliveryMethod} have been not set, or have been set as {@code null}
         */
        @Nonnull
        public AudioStream build() {
            validateBuild();

            return new AudioStream(this);
        }

        void validateBuild() {
            if (id == null) {
                throw new IllegalStateException(
                        "The identifier of the audio stream has been not set or is null. If you "
                                + "are not able to get an identifier, use the static constant "
                                + "ID_UNKNOWN of the Stream class.");
            }

            if (content == null) {
                throw new IllegalStateException("The content of the audio stream has been not set "
                        + "or is null. Please specify a non-null one with setContent.");
            }

            if (deliveryMethod == null) {
                throw new IllegalStateException(
                        "The delivery method of the audio stream has been set as null, which is "
                                + "not allowed. Pass a valid one instead with setDeliveryMethod.");
            }
        }
    }


    /**
     * Create a new audio stream using the given {@link Builder}.
     *
     * @param builder The {@link Builder} to use to create the audio stream
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    AudioStream(final Builder builder) {
        super(builder.id,
              builder.content,
              builder.isUrl,
              builder.mediaFormat,
              builder.deliveryMethod,
              builder.manifestUrl);
        if (builder.itagItem != null) {
            this.itagItem = builder.itagItem;
            this.itag = builder.itagItem.id;
            this.quality = builder.itagItem.getQuality();
            this.bitrate = builder.itagItem.getBitrate();
            this.initStart = builder.itagItem.getInitStart();
            this.initEnd = builder.itagItem.getInitEnd();
            this.indexStart = builder.itagItem.getIndexStart();
            this.indexEnd = builder.itagItem.getIndexEnd();
            this.codec = builder.itagItem.getCodec();
        }
        this.averageBitrate = builder.averageBitrate;
        this.audioTrackId = builder.audioTrackId;
        this.audioTrackName = builder.audioTrackName;
        this.audioLocale = builder.audioLocale;
        this.audioTrackType = builder.audioTrackType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalStats(final Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof AudioStream
                && averageBitrate == ((AudioStream) cmp).averageBitrate
                && Objects.equals(audioTrackId, ((AudioStream) cmp).audioTrackId)
                && audioTrackType == ((AudioStream) cmp).audioTrackType
                && Objects.equals(audioLocale, ((AudioStream) cmp).audioLocale);
    }

    /**
     * Get the average bitrate of the stream.
     *
     * @return the average bitrate or {@link #UNKNOWN_BITRATE} if it is unknown
     */
    public int getAverageBitrate() {
        return averageBitrate;
    }

    /**
     * Get the itag identifier of the stream.
     *
     * <p>
     * Always equals to {@link #ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE} for other streams than the
     * ones of the YouTube service.
     * </p>
     *
     * @return the number of the {@link ItagItem} passed in the constructor of the audio stream.
     */
    public int getItag() {
        return itag;
    }

    /**
     * Get the bitrate of the stream.
     *
     * @return the bitrate set from the {@link ItagItem} passed in the constructor of the stream.
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Get the initialization start of the stream.
     *
     * @return the initialization start value set from the {@link ItagItem} passed in the
     * constructor of the stream.
     */
    public int getInitStart() {
        return initStart;
    }

    /**
     * Get the initialization end of the stream.
     *
     * @return the initialization end value set from the {@link ItagItem} passed in the constructor
     * of the stream.
     */
    public int getInitEnd() {
        return initEnd;
    }

    /**
     * Get the index start of the stream.
     *
     * @return the index start value set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getIndexStart() {
        return indexStart;
    }

    /**
     * Get the index end of the stream.
     *
     * @return the index end value set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getIndexEnd() {
        return indexEnd;
    }

    /**
     * Get the quality of the stream.
     *
     * @return the quality label set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public String getQuality() {
        return quality;
    }

    /**
     * Get the codec of the stream.
     *
     * @return the codec set from the {@link ItagItem} passed in the constructor of the stream.
     */
    public String getCodec() {
        return codec;
    }

    /**
     * Get the id of the audio track.
     *
     * @return the id of the audio track
     */
    @Nullable
    public String getAudioTrackId() {
        return audioTrackId;
    }

    /**
     * Get the name of the audio track, which may be {@code null} if this information is not
     * provided by the service.
     *
     * @return the name of the audio track or {@code null}
     */
    @Nullable
    public String getAudioTrackName() {
        return audioTrackName;
    }

    /**
     * Get the {@link Locale} of the audio representing the language of the stream, which is
     * {@code null} if the audio language of this stream is not known.
     *
     * @return the {@link Locale} of the audio or {@code null}
     */
    @Nullable
    public Locale getAudioLocale() {
        return audioLocale;
    }

    /**
     * Get the {@link AudioTrackType} of the stream, which is {@code null} if the track type
     * is not known.
     *
     * @return the {@link AudioTrackType} of the stream or {@code null}
     */
    @Nullable
    public AudioTrackType getAudioTrackType() {
        return audioTrackType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ItagItem getItagItem() {
        return itagItem;
    }
}
