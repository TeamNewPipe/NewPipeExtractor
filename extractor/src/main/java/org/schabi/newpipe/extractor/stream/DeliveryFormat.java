package org.schabi.newpipe.extractor.stream;

import java.io.Serializable;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A class that is used to represent the way that a streaming service deliver their streams.
 */
public abstract class DeliveryFormat implements Serializable {

    public static Direct direct(@NonNull String url) {
        return new Direct(url);
    }

    public static HLS hls(@NonNull String url) {
        return new HLS(url);
    }

    public static ManualDASH manualDASH(@NonNull String baseUrl,
                                        @NonNull String manualDashManifest) {
        return new ManualDASH(baseUrl, manualDashManifest);
    }

    /**
     * Used when a service offer a direct link to their streams.
     */
    public static class Direct extends DeliveryFormat {
        private final String url;

        private Direct(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Direct direct = (Direct) o;

            //noinspection EqualsReplaceableByObjectsCall
            return url != null ? url.equals(direct.url) : direct.url == null;
        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }
    }

    /**
     * Used when a service uses HLS playlists for delivering their content.
     */
    public static class HLS extends DeliveryFormat {
        private final String url;

        private HLS(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HLS hls = (HLS) o;

            //noinspection EqualsReplaceableByObjectsCall
            return url != null ? url.equals(hls.url) : hls.url == null;
        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }
    }

    /**
     * Used when a service uses DASH Manifests to deliver their streams.
     * <p>
     * This is useful for extracting a specific stream from the entire manifest.
     */
    public static class ManualDASH extends DeliveryFormat {
        private final String baseUrl;
        private final String manualDashManifest;

        private ManualDASH(String baseUrl, String manualDashManifest) {
            this.baseUrl = baseUrl;
            this.manualDashManifest = manualDashManifest;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getManualDashManifest() {
            return manualDashManifest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ManualDASH that = (ManualDASH) o;

            //noinspection EqualsReplaceableByObjectsCall
            if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) {
                return false;
            }

            //noinspection EqualsReplaceableByObjectsCall
            return manualDashManifest != null
                    ? manualDashManifest.equals(that.manualDashManifest)
                    : that.manualDashManifest == null;
        }

        @Override
        public int hashCode() {
            int result = baseUrl != null ? baseUrl.hashCode() : 0;
            result = 31 * result + (manualDashManifest != null ? manualDashManifest.hashCode() : 0);
            return result;
        }
    }
}
