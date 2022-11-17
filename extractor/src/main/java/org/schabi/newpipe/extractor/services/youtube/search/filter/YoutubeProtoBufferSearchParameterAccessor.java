// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.youtube.search.filter;

import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.DateFilter;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.ExtraFeatures;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.Extras;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.Features;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.Filters;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.LengthFilter;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.SearchRequest;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.SortOrder;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.TypeFilter;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okio.ByteString;


/**
 * This class interacts with the auto generated proto buffer java files
 * created by the 'squareup.wire proto buffer' plugin.
 * <p>
 * Below proto buffer description file is used:
 * <a href="file:../main/proto/youtube-content-and-sort-filters.proto"
 * >youtube-content-and-sort-filters.proto</a>
 */
public final class YoutubeProtoBufferSearchParameterAccessor {

    /**
     * the base64 urlencoded sp string
     */
    private String searchParameter = "";

    private YoutubeProtoBufferSearchParameterAccessor() {
    }

    @SuppressWarnings("NewApi")
    public String encodeSp(@Nullable final SortOrder sort,
                           @Nullable final DateFilter date,
                           @Nullable final TypeFilter type,
                           @Nullable final LengthFilter length,
                           @Nullable final Features[] features,
                           @Nullable final ExtraFeatures[] extraFeatures) throws IOException {

        final Filters.Builder filtersBuilder = new Filters.Builder();
        if (null != date) {
            filtersBuilder.date((long) date.getValue());
        }

        if (null != type) {
            filtersBuilder.type((long) type.getValue());
        }

        if (null != length) {
            filtersBuilder.length((long) length.getValue());
        }

        if (null != features) {
            for (final Features feature : features) {
                setFeatureState(feature, true, filtersBuilder);
            }
        }

        final SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
        if (null != sort) {
            searchRequestBuilder.sorted((long) sort.getValue());
        }

        if (null != date || null != type || null != length || null != features
                // even though extraFeatures is evaluated later. But in the case that extraFeatures
                // will be the only activated 'feature' we still need to generate Filters here
                // as it is integral part of the SearchRequest object
                || null != extraFeatures) {
            final Filters filters = filtersBuilder.build();
            searchRequestBuilder.filter(filters);
        }

        if (null != extraFeatures && extraFeatures.length > 0) {
            final Extras.Builder extrasBuilder = new Extras.Builder();
            for (final ExtraFeatures extra : extraFeatures) {
                setExtraState(extra, true, extrasBuilder);
            }
            final Extras extras = extrasBuilder.build();
            searchRequestBuilder.extras(extras);
        }

        final SearchRequest searchRequest = searchRequestBuilder.build();

        final byte[] protoBufEncoded = searchRequest.encode();
        final ByteString bs = new ByteString(protoBufEncoded);
        final String protoBufEncodedBase64 = bs.base64();
        final String urlEncodedBase64EncodedSearchParameter
                = Utils.encodeUrlUtf8(protoBufEncodedBase64);

        this.searchParameter = urlEncodedBase64EncodedSearchParameter;

        return urlEncodedBase64EncodedSearchParameter;
    }

    /**
     * Decode a sp parameter back to a {@link SearchRequest} object
     *
     * @param urlEncodedBase64EncodedSearchParameter the parameter says it all
     * @return {@link SearchRequest} with decoded search parameter
     * @throws IOException
     */
    public SearchRequest decodeSp(@Nonnull final String urlEncodedBase64EncodedSearchParameter)
            throws IOException {
        final String urlDecodedBase64EncodedSearchParameter =
                Utils.decodeUrlUtf8(urlEncodedBase64EncodedSearchParameter);
        final byte[] decodedSearchParameter = Objects.requireNonNull(
                        ByteString.decodeBase64(urlDecodedBase64EncodedSearchParameter))
                .toByteArray();

        return new SearchRequest.Builder().build().adapter().decode(decodedSearchParameter);
    }

    public String getSp() {
        return this.searchParameter;
    }

    private void setExtraState(@Nonnull final ExtraFeatures extra,
                               final boolean enable,
                               @Nonnull final Extras.Builder extrasBuilder) {
        switch (extra) {
            case verbatim:
                extrasBuilder.verbatim(enable);
                break;
        }
    }

    private void setFeatureState(@Nonnull final Features feature,
                                 final boolean enable,
                                 @Nonnull final Filters.Builder filtersBuilder) {
        switch (feature) {
            case live:
                filtersBuilder.live(enable);
                break;
            case is_4k:
                filtersBuilder.is_4k(enable);
                break;
            case is_hd:
                filtersBuilder.is_hd(enable);
                break;
            case subtitles:
                filtersBuilder.subtitles(enable);
                break;
            case ccommons:
                filtersBuilder.ccommons(enable);
                break;
            case is_360:
                filtersBuilder.is_360(enable);
                break;
            case is_vr180:
                filtersBuilder.is_vr180(enable);
                break;
            case is_3d:
                filtersBuilder.is_3d(enable);
                break;
            case is_hdr:
                filtersBuilder.is_hdr(enable);
                break;
            case location:
                filtersBuilder.location(enable);
                break;
            case purchased:
                filtersBuilder.purchased(enable);
                break;
        }
    }

    /**
     * Build a {@link YoutubeProtoBufferSearchParameterAccessor} Object
     */
    public static class Builder {
        private final ArrayList<Features> featureList = new ArrayList<>();
        private final ArrayList<ExtraFeatures> extraFeatureList = new ArrayList<>();
        private final YoutubeProtoBufferSearchParameterAccessor searchParamGenerator =
                new YoutubeProtoBufferSearchParameterAccessor();
        private SortOrder sort = null;
        private DateFilter date = null;
        private TypeFilter type = null;
        private LengthFilter length = null;

        public Builder setSortOrder(@Nullable final SortOrder sortOrder) {
            this.sort = sortOrder;
            return this;
        }

        public Builder setDateFilter(@Nullable final DateFilter dateFilter) {
            this.date = dateFilter;
            return this;
        }

        public Builder setTypeFilter(@Nullable final TypeFilter typeFilter) {
            this.type = typeFilter;
            return this;
        }

        public Builder setLengthFilter(@Nullable final LengthFilter lengthFilter) {
            this.length = lengthFilter;
            return this;
        }

        public Builder addFeature(@Nullable final Features feature) {
            this.featureList.add(feature);
            return this;
        }

        public Builder addExtraFeature(@Nullable final ExtraFeatures extra) {
            this.extraFeatureList.add(extra);
            return this;
        }

        public YoutubeProtoBufferSearchParameterAccessor build() throws IOException {
            final Features[] features = this.featureList.toArray(new Features[0]);
            final ExtraFeatures[] extraFeat = this.extraFeatureList.toArray(new ExtraFeatures[0]);

            this.searchParamGenerator
                    .encodeSp(this.sort, this.date, this.type, this.length, features, extraFeat);
            return this.searchParamGenerator;
        }
    }
}
