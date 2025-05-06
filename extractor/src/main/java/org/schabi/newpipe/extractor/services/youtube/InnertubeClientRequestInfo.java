package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.DESKTOP_CLIENT_PLATFORM;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.EMBED_CLIENT_SCREEN;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_DEVICE_MODEL;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_OS_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.MOBILE_CLIENT_PLATFORM;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WATCH_CLIENT_SCREEN;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_HARDCODED_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_REMIX_HARDCODED_CLIENT_VERSION;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO: add docs

public final class InnertubeClientRequestInfo {

    @Nonnull
    public ClientInfo clientInfo;
    @Nonnull
    public DeviceInfo deviceInfo;

    public static final class ClientInfo {

        @Nonnull
        public String clientName;
        @Nonnull
        public String clientVersion;
        @Nonnull
        public String clientScreen;
        @Nullable
        public String clientId;
        @Nullable
        public String visitorData;

        private ClientInfo(@Nonnull final String clientName,
                           @Nonnull final String clientVersion,
                           @Nonnull final String clientScreen,
                           @Nullable final String clientId,
                           @Nullable final String visitorData) {
            this.clientName = clientName;
            this.clientVersion = clientVersion;
            this.clientScreen = clientScreen;
            this.clientId = clientId;
            this.visitorData = visitorData;
        }
    }

    public static final class DeviceInfo {

        @Nonnull
        public String platform;
        @Nullable
        public String deviceMake;
        @Nullable
        public String deviceModel;
        @Nullable
        public String osName;
        @Nullable
        public String osVersion;
        public int androidSdkVersion;

        private DeviceInfo(@Nonnull final String platform,
                           @Nullable final String deviceMake,
                           @Nullable final String deviceModel,
                           @Nullable final String osName,
                           @Nullable final String osVersion,
                           final int androidSdkVersion) {
            this.platform = platform;
            this.deviceMake = deviceMake;
            this.deviceModel = deviceModel;
            this.osName = osName;
            this.osVersion = osVersion;
            this.androidSdkVersion = androidSdkVersion;
        }
    }

    private InnertubeClientRequestInfo(@Nonnull final ClientInfo clientInfo,
                                       @Nonnull final DeviceInfo deviceInfo) {
        this.clientInfo = clientInfo;
        this.deviceInfo = deviceInfo;
    }

    @Nonnull
    public static InnertubeClientRequestInfo ofWebClient() {
        return new InnertubeClientRequestInfo(
                new InnertubeClientRequestInfo.ClientInfo(
                        WEB_CLIENT_NAME, WEB_HARDCODED_CLIENT_VERSION, WATCH_CLIENT_SCREEN,
                        WEB_CLIENT_ID, null),
                new InnertubeClientRequestInfo.DeviceInfo(DESKTOP_CLIENT_PLATFORM, null, null,
                        null, null, -1));
    }

    @Nonnull
    public static InnertubeClientRequestInfo ofWebEmbeddedPlayerClient() {
        return new InnertubeClientRequestInfo(
                new InnertubeClientRequestInfo.ClientInfo(WEB_EMBEDDED_CLIENT_NAME,
                        WEB_REMIX_HARDCODED_CLIENT_VERSION, EMBED_CLIENT_SCREEN,
                        WEB_EMBEDDED_CLIENT_ID, null),
                new InnertubeClientRequestInfo.DeviceInfo(DESKTOP_CLIENT_PLATFORM, null, null,
                        null, null, -1));
    }

    @Nonnull
    public static InnertubeClientRequestInfo ofAndroidClient() {
        return new InnertubeClientRequestInfo(
                new InnertubeClientRequestInfo.ClientInfo(ANDROID_CLIENT_NAME,
                        ANDROID_CLIENT_VERSION, WATCH_CLIENT_SCREEN, ANDROID_CLIENT_ID, null),
                new InnertubeClientRequestInfo.DeviceInfo(MOBILE_CLIENT_PLATFORM, null, null,
                        "Android", "15", 35));
    }

    @Nonnull
    public static InnertubeClientRequestInfo ofIosClient() {
        return new InnertubeClientRequestInfo(
                new InnertubeClientRequestInfo.ClientInfo(IOS_CLIENT_NAME, IOS_CLIENT_VERSION,
                        WATCH_CLIENT_SCREEN, IOS_CLIENT_ID, null),
                new InnertubeClientRequestInfo.DeviceInfo(MOBILE_CLIENT_PLATFORM, "Apple",
                        IOS_DEVICE_MODEL, "iOS", IOS_OS_VERSION, -1));
    }
}
