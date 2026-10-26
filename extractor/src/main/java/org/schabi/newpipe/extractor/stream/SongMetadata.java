
package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.localization.DateWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing the information on a song or single.
 */
public final class SongMetadata implements Serializable {
    /**
     * Constant representing that the track number of a {@link SongMetadata} is unknown.
     */
    public static final int TRACK_UNKNOWN = -1;

    @Nonnull
    public final String title;
    @Nonnull
    public final String artist;
    @Nonnull
    public final List<String> performer;
    @Nullable
    public final String composer;
    @Nullable
    public final String genre;
    @Nullable
    public final String album;
    public final int track;
    @Nullable
    public final Duration duration;
    @Nullable
    public final DateWrapper releaseDate;
    @Nullable
    public final String label;
    @Nullable
    public final String copyright;
    @Nullable
    public final String location;

    public SongMetadata(@Nonnull final String title, @Nonnull final String artist,
                        @Nonnull final List<String> performer, @Nullable final String composer,
                        @Nullable final String genre, @Nullable final String album,
                        final int track, @Nullable final Duration duration,
                        @Nullable final DateWrapper releaseDate, @Nullable final String label,
                        @Nullable final String copyright, @Nullable final String location) {
        this.title = title;
        this.artist = artist;
        this.performer = performer;
        this.composer = composer;
        this.genre = genre;
        this.album = album;
        this.track = track;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.label = label;
        this.copyright = copyright;
        this.location = location;
    }

    public static final class Builder {
        @Nonnull
        private final String mTitle;
        @Nonnull
        private final String mArtist;
        @Nonnull
        private List<String> mPerformer = new ArrayList<>();
        @Nullable
        private String mComposer;
        @Nullable
        private String mGenre;
        @Nullable
        private String mAlbum;
        private int mTrack = TRACK_UNKNOWN;
        @Nullable
        private Duration mDuration;
        @Nullable
        private DateWrapper mReleaseDate;
        @Nullable
        private String mLabel;
        @Nullable
        private String mCopyright;
        @Nullable
        private String mLocation;

        public Builder(@Nonnull final String title, @Nonnull final String artist) {
            this.mTitle = title;
            this.mArtist = artist;
        }

        public Builder setPerformer(@Nonnull final List<String> performer) {
            this.mPerformer = performer;
            return this;
        }

        public Builder addPerformer(@Nonnull final String performer) {
            this.mPerformer.add(performer);
            return this;
        }


        public Builder setComposer(@Nullable final String composer) {
            this.mComposer = composer;
            return this;
        }

        public Builder setGenre(@Nullable final String genre) {
            this.mGenre = genre;
            return this;
        }

        public Builder setAlbum(@Nullable final String album) {
            this.mAlbum = album;
            return this;
        }

        public Builder setTrack(final int track) {
            this.mTrack = track;
            return this;
        }

        public Builder setDuration(@Nullable final Duration duration) {
            this.mDuration = duration;
            return this;
        }

        public Builder setReleaseDate(@Nullable final DateWrapper releaseDate) {
            this.mReleaseDate = releaseDate;
            return this;
        }

        public Builder setLabel(@Nullable final String label) {
            this.mLabel = label;
            return this;
        }

        public Builder setCopyright(@Nullable final String copyright) {
            this.mCopyright = copyright;
            return this;
        }

        public Builder setLocation(@Nullable final String location) {
            this.mLocation = location;
            return this;
        }

        public SongMetadata build() {
            return new SongMetadata(
                    mTitle, mArtist, Collections.unmodifiableList(mPerformer), mComposer, mGenre,
                    mAlbum, mTrack, mDuration, mReleaseDate, mLabel, mCopyright, mLocation);
        }
    }

}
