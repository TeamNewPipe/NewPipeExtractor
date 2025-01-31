package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

        public ClientInfo(@Nonnull final String clientName,
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

        public DeviceInfo(@Nonnull final String platform,
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

    public InnertubeClientRequestInfo(@Nonnull final ClientInfo clientInfo,
                                      @Nonnull final DeviceInfo deviceInfo) {
        this.clientInfo = clientInfo;
        this.deviceInfo = deviceInfo;
    }
}
