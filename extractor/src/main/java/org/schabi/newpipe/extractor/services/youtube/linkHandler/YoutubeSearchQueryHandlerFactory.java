package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.apache.commons.codec.binary.Base64;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Base64Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String CHARSET_UTF_8 = "UTF-8";

    public enum FilterType {
        Content(new byte[]{0x10}),
        Time(new byte[]{0x08}),
        Duration(new byte[]{0x18}),
        Feature(new byte[]{});

        private final byte[] values;

        FilterType(byte[] values) {
            this.values = values;
        }
    }

    public enum Filter {
        All("All", FilterType.Content, new byte[]{0}),
        Video("Video", FilterType.Content, new byte[]{0x01}),
        Channel("Channel", FilterType.Content, new byte[]{0x02}),
        Playlist("Playlist", FilterType.Content, new byte[]{0x03}),
        Film("Film", FilterType.Content, new byte[]{0x04}),
        Programme("Programme", FilterType.Content, new byte[]{0x05}),

        Hour("Last hour", FilterType.Time, new byte[]{0x01}),
        Today("Today", FilterType.Time, new byte[]{0x02}),
        Week("This week", FilterType.Time, new byte[]{0x03}),
        Month("This month", FilterType.Time, new byte[]{0x04}),
        Year("This year", FilterType.Time, new byte[]{0x05}),

        Short("Short", FilterType.Duration, new byte[]{0x01}),
        Long("Long", FilterType.Duration, new byte[]{0x02}),

        HD("HD", FilterType.Feature, new byte[]{0x20, 0x01}),
        Subtitles("Subtitles/CC", FilterType.Feature, new byte[]{0x28, 0x01}),
        CreativeCommons("Creative Commons", FilterType.Feature, new byte[]{0x30, 0x01}),
        ThreeDimensional("3D", FilterType.Feature, new byte[]{0x38, 0x01}),
        Live("Live", FilterType.Feature, new byte[]{0x40, 0x01}),
        Purchased("Purchased", FilterType.Feature, new byte[]{0x48, 0x01}),
        FourK("4K", FilterType.Feature, new byte[]{0x70, 0x01}),
        ThreeSixty("360", FilterType.Feature, new byte[]{0x78, 0x01}),
        Location("Location", FilterType.Feature, new byte[]{(byte) 0xb8, 0x01, 0x01}),
        HDR("HDR", FilterType.Feature, new byte[]{(byte) 0xc8, 0x01, 0x01});

        private final String title;
        private final FilterType type;
        private final byte[] values;

        Filter(String title, FilterType type, byte[] values) {
            this.title = title;
            this.type = type;
            this.values = values;
        }

        public String getTitle() {
            return title;
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
        Upload_Date("Upload date", SorterType.Default, (byte) 0x02),
        View_Count("View count", SorterType.Default, (byte) 0x03);

        private final String title;
        private final SorterType type;
        private final byte value;

        Sorter(String title, SorterType type, byte value) {
            this.title = title;
            this.type = type;
            this.value = value;
        }

        public String getTitle() {
            return title;
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
        } catch (IllegalArgumentException | IOException e) {
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
            throws IllegalArgumentException, IOException {
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
        return URLEncoder.encode(Base64Utils.encodeBase64String(convert(returnList)), "UTF-8");
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
                List<Byte> returnList = new ArrayList<>();
                returnList.addAll(convert(contentFilter.type.values));
                returnList.addAll(convert(contentFilter.values));
                return returnList;
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

    private List<Byte> convert(@Nonnull byte[] byteArray) {
        List<Byte> returnList = new ArrayList<>(byteArray.length);
        for (int i = 0; i < byteArray.length; i++) {
            returnList.add(i, byteArray[i]);
        }
        return returnList;
    }
}
