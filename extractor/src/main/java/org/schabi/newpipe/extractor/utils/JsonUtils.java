package org.schabi.newpipe.extractor.utils;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * JsonUtils.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class JsonUtils {
    public static final JsonObject EMPTY_OBJECT = new JsonObject();
    public static final JsonArray EMPTY_ARRAY = new JsonArray();
    public static final String EMPTY_STRING = "";

    private JsonUtils() {
    }

    @Nonnull
    public static Object getValue(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException {

        List<String> keys = Arrays.asList(path.split("\\."));
        object = getObject(object, keys.subList(0, keys.size() - 1));
        if (null == object) throw new ParsingException("Unable to get " + path);
        Object result = object.get(keys.get(keys.size() - 1));
        if (null == result) throw new ParsingException("Unable to get " + path);
        return result;
    }

    @Nonnull
    public static String getString(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException {
        Object value = getValue(object, path);
        if (value instanceof String) {
            return (String) value;
        } else {
            throw new ParsingException("Unable to get " + path);
        }
    }

    @Nonnull
    public static Boolean getBoolean(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException {
        Object value = getValue(object, path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            throw new ParsingException("Unable to get " + path);
        }
    }

    @Nonnull
    public static Number getNumber(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException {
        Object value = getValue(object, path);
        if (value instanceof Number) {
            return (Number) value;
        } else {
            throw new ParsingException("Unable to get " + path);
        }
    }

    @Nonnull
    public static JsonObject getObject(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException {
        Object value = getValue(object, path);
        if (value instanceof JsonObject) {
            return (JsonObject) value;
        } else {
            throw new ParsingException("Unable to get " + path);
        }
    }

    @Nonnull
    public static JsonArray getArray(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException {
        Object value = getValue(object, path);
        if (value instanceof JsonArray) {
            return (JsonArray) value;
        } else {
            throw new ParsingException("Unable to get " + path);
        }
    }

    @Nonnull
    public static List<Object> getValues(@Nonnull JsonArray array, @Nonnull String path) throws ParsingException {

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.getObject(i);
            result.add(getValue(obj, path));
        }
        return result;
    }

    @Nullable
    private static JsonObject getObject(@Nonnull JsonObject object, @Nonnull List<String> keys) {
        JsonObject result = object;
        for (String key : keys) {
            result = result.getObject(key);
            if (null == result) break;
        }
        return result;
    }

    public static List<String> getListStringFromJsonArray(@Nonnull JsonArray array) {
        List<String> tagsList = new ArrayList<>(array.size());
        for (Object tag : array) {
            if (tag instanceof String) {
                tagsList.add((String) tag);
            }
        }
        return tagsList;
    }

}
