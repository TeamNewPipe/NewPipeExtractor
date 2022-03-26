package org.schabi.newpipe.extractor.utils;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class JsonUtils {
    private JsonUtils() {
    }

    @Nonnull
    public static Object getValue(@Nonnull final JsonObject object,
                                  @Nonnull final String path) throws ParsingException {

        final List<String> keys = Arrays.asList(path.split("\\."));
        final JsonObject parentObject = getObject(object, keys.subList(0, keys.size() - 1));
        if (parentObject == null) {
            throw new ParsingException("Unable to get " + path);
        }

        final Object result = parentObject.get(keys.get(keys.size() - 1));
        if (result == null) {
            throw new ParsingException("Unable to get " + path);
        }
        return result;
    }

    private static <T> T getInstanceOf(@Nonnull final JsonObject object,
                                       @Nonnull final String path,
                                       @Nonnull final Class<T> klass) throws ParsingException {
        final Object value = getValue(object, path);
        if (klass.isInstance(value)) {
            return klass.cast(value);
        } else {
            throw new ParsingException("Wrong data type at path " + path);
        }
    }

    @Nonnull
    public static String getString(@Nonnull final JsonObject object, @Nonnull final String path)
            throws ParsingException {
        return getInstanceOf(object, path, String.class);
    }

    @Nonnull
    public static Boolean getBoolean(@Nonnull final JsonObject object,
                                     @Nonnull final String path) throws ParsingException {
        return getInstanceOf(object, path, Boolean.class);
    }

    @Nonnull
    public static Number getNumber(@Nonnull final JsonObject object,
                                   @Nonnull final String path)
            throws ParsingException {
        return getInstanceOf(object, path, Number.class);
    }

    @Nonnull
    public static JsonObject getObject(@Nonnull final JsonObject object,
                                       @Nonnull final String path) throws ParsingException {
        return getInstanceOf(object, path, JsonObject.class);
    }

    @Nonnull
    public static JsonArray getArray(@Nonnull final JsonObject object, @Nonnull final String path)
            throws ParsingException {
        return getInstanceOf(object, path, JsonArray.class);
    }

    @Nonnull
    public static List<Object> getValues(@Nonnull final JsonArray array, @Nonnull final String path)
            throws ParsingException {

        final List<Object> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            final JsonObject obj = array.getObject(i);
            result.add(getValue(obj, path));
        }
        return result;
    }

    @Nullable
    private static JsonObject getObject(@Nonnull final JsonObject object,
                                        @Nonnull final List<String> keys) {
        JsonObject result = object;
        for (final String key : keys) {
            result = result.getObject(key);
            if (result == null) {
                break;
            }
        }
        return result;
    }

    public static JsonArray toJsonArray(final String responseBody) throws ParsingException {
        try {
            return JsonParser.array().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }
    }

    public static JsonObject toJsonObject(final String responseBody) throws ParsingException {
        try {
            return JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }
    }

    /**
     * <p>Get an attribute of a web page as JSON
     *
     * <p>Originally a part of bandcampDirect.</p>
     * <p>Example HTML:</p>
     * <pre>
     * {@code
     * <p data-town="{&quot;name&quot;:&quot;Mycenae&quot;,&quot;country&quot;:&quot;Greece&quot;}">
     * This is Sparta!</p>
     * }
     * </pre>
     * <p>Calling this function to get the attribute <code>data-town</code> returns the JsonObject
     * for</p>
     * <pre>
     * {@code
     *   {
     *     "name": "Mycenae",
     *     "country": "Greece"
     *   }
     * }
     * </pre>
     *
     * @param html     The HTML where the JSON we're looking for is stored inside a
     *                 variable inside some JavaScript block
     * @param variable Name of the variable
     * @return The JsonObject stored in the variable with this name
     */
    public static JsonObject getJsonData(final String html, final String variable)
            throws JsonParserException, ArrayIndexOutOfBoundsException {
        final Document document = Jsoup.parse(html);
        final String json = document.getElementsByAttribute(variable).attr(variable);
        return JsonParser.object().from(json);
    }

    public static List<String> getStringListFromJsonArray(@Nonnull final JsonArray array) {
        return array.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());
    }
}
