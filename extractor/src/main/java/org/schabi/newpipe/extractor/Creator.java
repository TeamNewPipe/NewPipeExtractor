package org.schabi.newpipe.extractor;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * Class representing a creator of a piece of media that has been extracted.
 */
public final class Creator {

    /**
     * Constant representing that the amount of subscribers of a {@link Creator} is unknown.
     */
    public static final long UNKNOWN_SUBSCRIBER_COUNT = -1;

    @Nonnull
    private final String name;
    @Nonnull
    private final String url;
    @Nonnull
    private final List<Image> avatars;
    private final long subscriberCount;
    private final boolean isVerified;

    /**
     * Construct an {@link Creator} instance.
     *
     * @param name                     the name of the creator, which should be not null or empty
     * @param url                      the URL to the creator's page, which should be not null
     *                                 or empty
     * @param avatars                  the avatar of the creator, possibly in multiple resolutions
     * @param subscriberCount          the amount of subscribers/followers of the creator
     * @param isVerified               whether the creator has been verified by the platform
     */
    public Creator(@Nonnull final String name,
                 @Nonnull final String url,
                 @Nonnull final List<Image> avatars,
                 final long subscriberCount,
                 final boolean isVerified) {
        this.name = name;
        this.url = url;
        this.avatars = avatars;
        this.subscriberCount = subscriberCount;
        this.isVerified = isVerified;
    }

    /**
     * Get the name of the {@link Creator}.
     *
     * @return the {@link Creator}'s name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Get the URL of the {@link Creator}.
     *
     * @return the {@link Creator}'s URL.
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * Get the avatars of the {@link Creator}.
     *
     * @return the {@link Creator}'s avatars.
     */
    @Nonnull
    public List<Image> getAvatars() {
        return avatars;
    }

    /**
     * Get the amount of subscribers of this {@link Image}.
     *
     * <p>
     * If it is unknown, {@link #UNKNOWN_SUBSCRIBER_COUNT} is returned instead.
     * </p>
     *
     * @return the {@link Creator}'s amount of subscribers or {@link #UNKNOWN_SUBSCRIBER_COUNT}
     */
    public long getSubscriberCount() {
        return subscriberCount;
    }

    /**
     * Get whether the {@link Creator} has been verified by the platform.
     *
     * @return whether the {@link Creator} has been verified by the platform
     */
    public boolean isVerified() {
        return isVerified;
    }

    /**
     * Get a string representation of this {@link Creator} instance.
     *
     * @return a string representation of this {@link Creator} instance
     */
    @Nonnull
    @Override
    public String toString() {
        return "Creator {" + "name=" + name + ", url=" + url + ", avatars=" + avatars
                + ", subscriberCount=" + subscriberCount + ", isVerified=" + isVerified + "}";
    }
}
