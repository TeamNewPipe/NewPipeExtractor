package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nullable;

/**
 * An interface to provide poTokens to YouTube player requests.
 *
 * @implNote This interface is expected to be thread-safe, as it may be accessed by multiple threads.
 *
 * <p>
 * On some major clients, YouTube requires that the integrity of the device passes some checks to
 * allow playback.
 * </p>
 *
 * <p>
 * These checks involve running codes to verify the integrity and using their result to generate a
 * poToken (which likely stands for proof of origin token), using a visitor data ID for logged-out
 * users.
 * </p>
 *
 * <p>
 * These tokens may have a role in triggering the sign in requirement.
 * </p>
 */
public interface PoTokenProvider {

    /**
     * Get a {@link PoTokenResult} specific to the desktop website, a.k.a. the WEB InnerTube client.
     *
     * <p>
     * To be generated and valid, poTokens from this client must be generated using Google's
     * BotGuard machine, which requires a JavaScript engine with a good DOM implementation. They
     * must be added to adaptive/DASH streaming URLs with the {@code pot} parameter.
     * </p>
     *
     * @return a {@link PoTokenResult} specific to the WEB InnerTube client
     */
    @Nullable
    PoTokenResult getWebClientPoToken();

    @Nullable
    PoTokenResult getAndroidClientPoToken();
}
