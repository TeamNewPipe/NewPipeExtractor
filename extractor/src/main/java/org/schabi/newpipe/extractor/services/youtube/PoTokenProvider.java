package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nullable;

/**
 * An interface to provide poTokens to YouTube player requests.
 *
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
 *
 * <p>
 * <b>Implementations of this interface are expected to be thread-safe, as they may be accessed by
 * multiple threads.</b>
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
     * <p>
     * Note that YouTube desktop website generates two poTokens:
     * - one for the player requests poTokens, using the videoId as the minter value;
     * - one for the streaming URLs, using a visitor data for logged-out users.
     * </p>
     *
     * @return a {@link PoTokenResult} specific to the WEB InnerTube client
     */
    @Nullable
    PoTokenResult getWebClientPoToken(String videoId);

    /**
     * Get a {@link PoTokenResult} specific to the web embeds, a.k.a. the WEB_EMBEDDED_PLAYER
     * InnerTube client.
     *
     * <p>
     * To be generated and valid, poTokens from this client must be generated using Google's
     * BotGuard machine, which requires a JavaScript engine with a good DOM implementation. They
     * should be added to adaptive/DASH streaming URLs with the {@code pot} parameter and do not
     * seem to be mandatory for now.
     * </p>
     *
     * <p>
     * As of writing, like the YouTube desktop website previously did, it generates only one
     * poToken, sent in player requests and streaming URLs, using a visitor data for logged-out
     * users.
     * </p>
     *
     * @return a {@link PoTokenResult} specific to the WEB_EMBEDDED_PLAYER InnerTube client
     */
    @Nullable
    PoTokenResult getWebEmbedClientPoToken(String videoId);

    /**
     * Get a {@link PoTokenResult} specific to the Android app, a.k.a. the ANDROID InnerTube client.
     *
     * <p>
     * Implementation details are not known, the app uses DroidGuard, a native virtual machine
     * ran by Google Play Services for which its code is updated pretty frequently.
     * </p>
     *
     * <p>
     * As of writing, DroidGuard seem to check for the Android app signature and package ID, as
     * unrooted YouTube patched with reVanced doesn't work without spoofing another InnerTube
     * client while the rooted version works without any client spoofing.
     * </p>
     *
     * <p>
     * There should be only poToken needed, for the player requests.
     * </p>
     *
     * @return a {@link PoTokenResult} specific to the ANDROID InnerTube client
     */
    @Nullable
    PoTokenResult getAndroidClientPoToken(String videoId);

    /**
     * Get a {@link PoTokenResult} specific to the Android app, a.k.a. the ANDROID InnerTube client.
     *
     * <p>
     * Implementation details are not really known, the app seem to use something called
     * iosGuard which should be something similar to Android's DroidGuard. It may rely on Apple's
     * attestation APIs.
     * </p>
     *
     * <p>
     * There should be only poToken needed, for the player requests.
     * </p>
     *
     * @return a {@link PoTokenResult} specific to the IOS InnerTube client
     */
    @Nullable
    PoTokenResult getIosClientPoToken(String videoId);
}
