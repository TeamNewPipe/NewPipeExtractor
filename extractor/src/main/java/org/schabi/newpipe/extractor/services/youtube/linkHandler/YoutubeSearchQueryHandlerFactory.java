package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Base64;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String CHARSET_UTF_8 = "UTF-8";

    public enum FilterType {
        Content((byte)0x10),
        Time((byte)0x08),
        Duration((byte)0x18);

        private final byte value;

        FilterType(byte value) {
            this.value = value;
        }
    }

    public enum Filter {
        All(FilterType.Content,(byte)0),
        Video(FilterType.Content,(byte)0x01),
        Channel(FilterType.Content,(byte)0x02),
        Playlist(FilterType.Content,(byte)0x03),
        Movie(FilterType.Content,(byte)0x04),
        Show(FilterType.Content,(byte)0x05),

        Hour(FilterType.Time,(byte)0x01),
        Today(FilterType.Time,(byte)0x02),
        Week(FilterType.Time,(byte)0x03),
        Month(FilterType.Time,(byte)0x04),
        Year(FilterType.Time,(byte)0x05),

        Short(FilterType.Duration, (byte)0x01),
        Long(FilterType.Duration, (byte)0x02);

        private final FilterType type;
        private final byte value;

        Filter(FilterType type, byte value) {
            this.type = type;
            this.value = value;
        }
    }

    public enum SorterType {
        Default((byte)0x08);

        private final byte value;

        SorterType(byte value) {
            this.value = value;
        }
    }

    public enum Sorter {
        Relevance(SorterType.Default, (byte)0x00),
        Rating(SorterType.Default, (byte)0x01),
        Upload_Date(SorterType.Default, (byte)0x02),
        View_Count(SorterType.Default, (byte)0x03);

        private final SorterType type;
        private final byte value;

        Sorter(SorterType type, byte value) {
            this.type = type;
            this.value = value;
        }
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
        for(Filter contentFilter : Filter.values()) {
            contentFiltersList.add(contentFilter.name());
        }
        String[] contentFiltersArray = new String[contentFiltersList.size()];
        contentFiltersArray = contentFiltersList.toArray(contentFiltersArray);
        return contentFiltersArray;
    }

    @Override
    public String[] getAvailableSortFilter() {
        List<String> sortFiltersList = new ArrayList<>();
        for(Sorter sortFilter : Sorter.values()) {
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
        return URLEncoder.encode(Base64.encodeToString(convert(returnList), Base64.URL_SAFE));
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
        Filter contentFilter;
        try {
            contentFilter = Filter.valueOf(filter);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            System.err.println("Unknown content filter type provided = " + filter +", none will be applied");
            return Collections.emptyList();
        }
        switch (contentFilter) {
            case All:
                return Collections.emptyList();
            default:
                return Arrays.asList(contentFilter.type.value, contentFilter.value);
        }
    }

    private List<Byte> getSortFiltersQueryParam(String filter) {
        if(filter == null || filter.isEmpty()) {
            return Collections.emptyList();
        }
        Sorter sorter;
        try {
            sorter = Sorter.valueOf(filter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Unknown sort filter = " + filter + " provided, none applied.");
            return Collections.emptyList();
        }
        return Arrays.asList(sorter.type.value, sorter.value);
    }

    private byte[] convert(@Nonnull List<Byte> bigByteList) {
        byte[] returnArray = new byte[bigByteList.size()];
        for (int i = 0; i < bigByteList.size(); i++) {
            returnArray[i] = bigByteList.get(i);
        }
        return returnArray;
    }
}
