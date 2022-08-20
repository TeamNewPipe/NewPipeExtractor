package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

import java.util.List;

public class HtmlInitialData {
    private ResponseContext responseContext;

    public ResponseContext getResponseContext() {
        return responseContext;
    }

    public void setResponseContext(final ResponseContext responseContext) {
        this.responseContext = responseContext;
    }

    @Override
    public String toString() {
        return "HtmlInitialData{responseContext=" + responseContext + '}';
    }

    public static class ResponseContext {
        private List<ServiceTrackingParam> serviceTrackingParams;

        public List<ServiceTrackingParam> getServiceTrackingParams() {
            return serviceTrackingParams;
        }

        public void setServiceTrackingParams(final List<ServiceTrackingParam> params) {
            this.serviceTrackingParams = params;
        }

        @Override
        public String toString() {
            return "ResponseContext{serviceTrackingParams=" + serviceTrackingParams + '}';
        }
    }

    public static class ServiceTrackingParam {
        private String service;
        private List<Param> params;

        public String getService() {
            return service;
        }

        public void setService(final String service) {
            this.service = service;
        }

        public List<Param> getParams() {
            return params;
        }

        public void setParams(final List<Param> params) {
            this.params = params;
        }

        @Override
        public String toString() {
            return "ServiceTrackingParam{service='" + service + "', params=" + params + '}';
        }
    }

    public static class Param {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Param{key='" + key + "', value='" + value + "'}";
        }
    }
}
