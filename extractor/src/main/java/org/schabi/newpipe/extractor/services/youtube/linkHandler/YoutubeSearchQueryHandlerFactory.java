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
        Content((byte) 0x10),
        Time((byte) 0x08),
        Duration((byte) 0x18),
        Feature((byte) 0);

        private final byte value;

        FilterType(byte value) {
            this.value = value;
        }
    }

    public enum Filter {
        All("All", FilterType.Content, (byte) 0),
        Video("Video", FilterType.Content, (byte) 0x01),
        Channel("Channel", FilterType.Content, (byte) 0x02),
        Playlist("Playlist", FilterType.Content, (byte) 0x03),
        Movie("Movie", FilterType.Content, (byte) 0x04),
        Show("Show", FilterType.Content, (byte) 0x05),

        Hour("Hour", FilterType.Time, (byte) 0x01),
        Today("Today", FilterType.Time, (byte) 0x02),
        Week("Week", FilterType.Time, (byte) 0x03),
        Month("Month", FilterType.Time, (byte) 0x04),
        Year("Year", FilterType.Time, (byte) 0x05),

        Short("Short", FilterType.Duration, (byte) 0x01),
        Long("Long", FilterType.Duration, (byte) 0x02),

        HD("HD", FilterType.Feature, (byte) 0x2001),
        Subtitles("Subtitles", FilterType.Feature, (byte) 0x2801),
        CreativeCommons("Creative Commons", FilterType.Feature, (byte) 0x3001),
        ThreeDimensional("3D", FilterType.Feature, (byte) 0x3801),
        Live("Live", FilterType.Feature, (byte) 0x4001),
        Purchased("Purchased", FilterType.Feature, (byte) 0x4801),
        FourK("4k", FilterType.Feature, (byte) 0x7001),
        ThreeSixty("360", FilterType.Feature, (byte) 0x7801),
        Location("Location", FilterType.Feature, (byte) 0xb80101),
        HDR("HDR", FilterType.Feature, (byte) 0xc80101);

        private final String title;
        private final FilterType type;
        private final byte value;

        Filter(String title, FilterType type, byte value) {
            this.title = title;
            this.type = type;
            this.value = value;
        }
    }

    public enum SorterType {
        Default((byte) 0x08);

        private final byte value;

        SorterType(byte value) {
            this.value = value;
        }
    }

    public enum Sorter {
        Relevance("Relevance", SorterType.Default, (byte) 0x00),
        Rating("Rating", SorterType.Default, (byte) 0x01),
        Upload_Date("Upload_Date", SorterType.Default, (byte) 0x02),
        View_Count("View_Count", SorterType.Default, (byte) 0x03);

        private final String title;
        private final SorterType type;
        private final byte value;

        Sorter(String title, SorterType type, byte value) {
            this.title = title;
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
            if (filterQueryParams != null) {
                returnURL = returnURL + "&sp=" + filterQueryParams;
            }
            return returnURL;
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Failed to get search results", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        List<String> contentFiltersList = new ArrayList<>();
        for (Filter contentFilter : Filter.values()) {
            contentFiltersList.add(contentFilter.title);
        }
        String[] contentFiltersArray = new String[contentFiltersList.size()];
        contentFiltersArray = contentFiltersList.toArray(contentFiltersArray);
        return contentFiltersArray;
    }

    @Override
    public String[] getAvailableSortFilter() {
        List<String> sortFiltersList = new ArrayList<>();
        for (Sorter sortFilter : Sorter.values()) {
            sortFiltersList.add(sortFilter.title);
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
            throws IllegalArgumentException {
        List<Byte> returnList = new ArrayList<>();
        List<Byte> sortFilterParams = getSortFiltersQueryParam(sortFilter);
        if (!sortFilterParams.isEmpty()) {
            returnList.addAll(sortFilterParams);
        }
        List<Byte> contentFilterParams = getContentFiltersQueryParams(contentFilters);
        if (!contentFilterParams.isEmpty()) {
            returnList.add((byte) 0x12);
            returnList.add((byte) contentFilterParams.size());
            returnList.addAll(contentFilterParams);
        }

        if (returnList.isEmpty()) {
            return null;
        }
        return URLEncoder.encode(Base64.encodeToString(convert(returnList), Base64.URL_SAFE));
    }

    private List<Byte> getContentFiltersQueryParams(List<String> contentFilter) throws IllegalArgumentException {
        if (contentFilter == null || contentFilter.isEmpty()) {
            return Collections.emptyList();
        }
        List<Byte> returnList = new ArrayList<>();
        for (String filter : contentFilter) {
            List<Byte> byteList = getContentFilterQueryParams(filter);
            if (!byteList.isEmpty()) {
                returnList.addAll(byteList);
            }
        }
        return returnList;
    }

    private List<Byte> getContentFilterQueryParams(String filter) throws IllegalArgumentException {
        Filter contentFilter;
        try {
            contentFilter = Filter.valueOf(filter);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            throw new IllegalArgumentException("Unknown content filter type provided = " + filter + ", none will be applied");
        }
        switch (contentFilter) {
            case All:
                return Collections.emptyList();
            default:
                return Arrays.asList(contentFilter.type.value, contentFilter.value);
        }
    }

    private List<Byte> getSortFiltersQueryParam(String filter) throws IllegalArgumentException {
        if (filter == null || filter.isEmpty()) {
            return Collections.emptyList();
        }
        Sorter sorter;
        try {
            sorter = Sorter.valueOf(filter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unknown sort filter = " + filter + " provided, none applied.");
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
