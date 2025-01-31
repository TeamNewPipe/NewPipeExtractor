package org.schabi.newpipe.extractor.services.youtube;

final class ClientsConstants {
    private ClientsConstants() {
    }

    // Common client fields

    static final String DESKTOP_CLIENT_PLATFORM = "DESKTOP";
    static final String MOBILE_CLIENT_PLATFORM = "MOBILE";
    static final String WATCH_CLIENT_SCREEN = "WATCH";
    static final String EMBED_CLIENT_SCREEN = "EMBED";

    // WEB (YouTube desktop) client fields

    static final String WEB_CLIENT_ID = "1";
    static final String WEB_CLIENT_NAME = "WEB";
    /**
     * The client version for InnerTube requests with the {@code WEB} client, used as the last
     * fallback if the extraction of the real one failed.
     */
    static final String WEB_HARDCODED_CLIENT_VERSION = "2.20250122.04.00";

    // WEB_REMIX (YouTube Music) client fields

    static final String WEB_REMIX_CLIENT_ID = "67";
    static final String WEB_REMIX_CLIENT_NAME = "WEB_REMIX";
    static final String WEB_REMIX_HARDCODED_CLIENT_VERSION = "1.20250122.01.00";

    // TVHTML5 (YouTube on TVs and consoles using HTML5) client fields
    static final String TVHTML5_CLIENT_ID = "7";
    static final String TVHTML5_CLIENT_NAME = "TVHTML5";
    static final String TVHTML5_CLIENT_VERSION = "7.20250122.15.00";
    static final String TVHTML5_CLIENT_PLATFORM = "GAME_CONSOLE";
    static final String TVHTML5_DEVICE_MAKE = "Sony";
    static final String TVHTML5_DEVICE_MODEL_AND_OS_NAME = "PlayStation 4";
    // CHECKSTYLE:OFF
    static final String TVHTML5_USER_AGENT =
            "Mozilla/5.0 (PlayStation; PlayStation 4/12.00) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Safari/605.1.15";
    // CHECKSTYLE:ON

    // WEB_EMBEDDED_PLAYER (YouTube embeds)

    static final String WEB_EMBEDDED_CLIENT_ID = "56";
    static final String WEB_EMBEDDED_CLIENT_NAME = "WEB_EMBEDDED_PLAYER";
    static final String WEB_EMBEDDED_CLIENT_VERSION = "1.20250121.00.00";

    // IOS (iOS YouTube app) client fields

    static final String IOS_CLIENT_ID = "5";
    static final String IOS_CLIENT_NAME = "IOS";

    /**
     * The hardcoded client version of the iOS app used for InnerTube requests with this client.
     *
     * <p>
     * It can be extracted by getting the latest release version of the app on
     * <a href="https://apps.apple.com/us/app/youtube-watch-listen-stream/id544007664/">the App
     * Store page of the YouTube app</a>, in the {@code What’s New} section.
     * </p>
     */
    static final String IOS_CLIENT_VERSION = "20.03.02";

    /**
     * The device machine id for the iPhone 15 Pro Max, used to get 60fps with the {@code iOS}
     * client.
     *
     * <p>
     * See <a href="https://gist.github.com/adamawolf/3048717">this GitHub Gist</a> for more
     * information.
     * </p>
     */
    static final String IOS_DEVICE_MODEL = "iPhone16,2";

    /**
     * The iOS version to be used in JSON POST requests, the one of an iPhone 15 Pro Max running
     * iOS 18.2.1 with the hardcoded version of the iOS app (for the {@code "osVersion"} field).
     *
     * <p>
     * The value of this field seems to use the following structure:
     * "iOS major version.minor version.patch version.build version", where
     * "patch version" is equal to 0 if it isn't set
     * The build version corresponding to the iOS version used can be found on
     * <a href="https://theapplewiki.com/wiki/Firmware/iPhone/18.x#iPhone_15_Pro_Max">
     *     https://theapplewiki.com/wiki/Firmware/iPhone/18.x#iPhone_15_Pro_Max</a>
     * </p>
     *
     * @see #IOS_USER_AGENT_VERSION
     */
    static final String IOS_OS_VERSION = "18.2.1.22C161";

    /**
     * The iOS version to be used in the HTTP user agent for requests.
     *
     * <p>
     * This should be the same of as {@link #IOS_OS_VERSION}.
     * </p>
     *
     * @see #IOS_OS_VERSION
     */
    static final String IOS_USER_AGENT_VERSION = "18_2_1";

    // ANDROID (Android YouTube app) client fields

    static final String ANDROID_CLIENT_ID = "3";
    static final String ANDROID_CLIENT_NAME = "ANDROID";

    /**
     * The hardcoded client version of the Android app used for InnerTube requests with this
     * client.
     *
     * <p>
     * It can be extracted by getting the latest release version of the app in an APK repository
     * such as <a href="https://www.apkmirror.com/apk/google-inc/youtube/">APKMirror</a>.
     * </p>
     */
    static final String ANDROID_CLIENT_VERSION = "19.28.35";
}
