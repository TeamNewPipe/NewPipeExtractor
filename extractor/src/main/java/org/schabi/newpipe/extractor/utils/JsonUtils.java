package org.schabi.newpipe.extractor.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

public class JsonUtils {

    private JsonUtils() {
    }
    
    @Nonnull
    public static <T> T getValue(@Nonnull JsonObject object, @Nonnull String path) throws ParsingException{

        List<String> keys = Arrays.asList(path.split("\\."));
        object = getObject(object, keys.subList(0, keys.size() - 1));
        if (null == object) throw new ParsingException("Unable to get " + path);
        T result = (T) object.get(keys.get(keys.size() - 1));
        if(null == result) throw new ParsingException("Unable to get " + path);
        return result;
    }
    

    @Nonnull
    public static <T> List<T> getValues(@Nonnull JsonArray array, @Nonnull String path) throws ParsingException {
        
        List<T> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.getObject(i);
            result.add((T)getValue(obj, path));
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

}
