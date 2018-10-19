package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String CHARSET_UTF_8 = "UTF-8";

    private static final SortFilter DEFAULT_SORT_FILTER = SortFilter.relevance;
    private static final ContentFilter DEFAULT_CONTENT_FILTER = ContentFilter.all;

    public enum ContentFilter {
        all,
        videos,
        channels,
        playlists,
        movie,
        show
    }

    public enum SortFilter {
        relevance,
        rating,
        upload_date,
        date,
        view_count,
        views
    }

    public static YoutubeSearchQueryHandlerFactory getInstance() {
        return new YoutubeSearchQueryHandlerFactory();
    }

    @Override
    public String getUrl(String searchString, List<String> contentFilters, String sortFilter) throws ParsingException {
        try {
            String returnURL = getSearchBaseUrl(searchString);
            String filterQueryParams = getFilterQueryParams(contentFilters, sortFilter);
            if(filterQueryParams != null) {
                returnURL = returnURL + "&sp=" + filterQueryParams;
            }
            return returnURL;
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        List<String> contentFiltersList = new ArrayList<>();
        for(ContentFilter contentFilter : ContentFilter.values()) {
            contentFiltersList.add(contentFilter.name());
        }
        String[] contentFiltersArray = new String[contentFiltersList.size()];
        contentFiltersArray = contentFiltersList.toArray(contentFiltersArray);
        return contentFiltersArray;
    }

    @Override
    public String[] getAvailableSortFilter() {
        List<String> sortFiltersList = new ArrayList<>();
        for(SortFilter sortFilter : SortFilter.values()) {
            sortFiltersList.add(sortFilter.name());
        }
        String[] sortFiltersArray = new String[sortFiltersList.size()];
        sortFiltersArray = sortFiltersList.toArray(sortFiltersArray);
        return sortFiltersArray;
    }

    private String getSearchBaseUrl(String searchQuery)
            throws UnsupportedEncodingException {
        return "https://www.youtube.com/results" +
                "?q=" +
                URLEncoder.encode(searchQuery, CHARSET_UTF_8);
    }

    @Nullable
    private String getFilterQueryParams(List<String> contentFilters, String sortFilter)
            throws UnsupportedEncodingException {
        List<Byte> returnList = new ArrayList<>();
        List<Byte> sortFilterParams = getSortFiltersQueryParam(sortFilter);
        if(!sortFilterParams.isEmpty()) {
            returnList.addAll(sortFilterParams);
        }
        List<Byte> contentFilterParams = getContentFiltersQueryParams(contentFilters);
        if(!contentFilterParams.isEmpty()) {
            returnList.add((byte)0x12);
            returnList.add((byte)contentFilterParams.size());
            returnList.addAll(contentFilterParams);
        }

        if(returnList.isEmpty()) {
            return null;
        }
        return URLEncoder.encode(DatatypeConverter.printBase64Binary(convert(returnList)), CHARSET_UTF_8);
    }

    private List<Byte> getContentFiltersQueryParams(List<String> contentFilter) {
        if(contentFilter == null || contentFilter.isEmpty()) {
            return Collections.emptyList();
        }
        List<Byte> returnList = new ArrayList<>();
        for(String filter : contentFilter) {
            List<Byte> byteList = getContentFilterQueryParams(filter);
            if(!byteList.isEmpty()) {
                returnList.addAll(byteList);
            }
        }
        return returnList;
    }

    private List<Byte> getContentFilterQueryParams(String filter) {
        ContentFilter contentFilter;
        try {
            contentFilter = ContentFilter.valueOf(filter);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            System.err.println("Unknown content filter type provided = " + filter +", none will be applied");
            return Collections.emptyList();
        }
        switch (contentFilter) {
            case all:
                return Collections.emptyList();
            case videos:
                return Arrays.asList((byte)0x10, (byte)0x01);
            case channels:
                return Arrays.asList((byte)0x10, (byte)0x02);
            case playlists:
                return Arrays.asList((byte)0x10, (byte)0x03);
            case movie:
                return Arrays.asList((byte)0x10, (byte)0x04);
            case show:
                return Arrays.asList((byte)0x10, (byte)0x05);
            default:
                throw new IllegalArgumentException("Unexpected content filter found = " + contentFilter);
        }
    }

    private List<Byte> getSortFiltersQueryParam(String filter) {
        if(filter == null || filter.isEmpty()) {
            return Collections.emptyList();
        }
        SortFilter sortFilter;
        try {
            sortFilter = SortFilter.valueOf(filter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Unknown sort filter = " + filter + ", provided, none applied.");
            return Collections.emptyList();
        }
        switch (sortFilter) {
            case relevance:
                return new ArrayList<>(Arrays.asList((byte)0x08, (byte)0x00));
            case rating:
                return new ArrayList<>(Arrays.asList((byte)0x08, (byte)0x01));
            case upload_date:
            case date:
                return new ArrayList<>(Arrays.asList((byte)0x08, (byte)0x02));
            case view_count:
            case views:
                return new ArrayList<>(Arrays.asList((byte)0x08, (byte)0x03));
            default:
                throw new IllegalArgumentException("Unexpected sort filter = " + sortFilter);
        }
    }

    private byte[] convert(@Nonnull List<Byte> bigByteList) {
        byte[] returnArray = new byte[bigByteList.size()];
        for (int i = 0; i < bigByteList.size(); i++) {
            returnArray[i] = bigByteList.get(i);
        }
        return returnArray;
    }
}
