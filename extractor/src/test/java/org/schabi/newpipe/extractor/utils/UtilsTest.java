package org.schabi.newpipe.extractor.utils;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import org.junit.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class UtilsTest {
    @Test
    public void testMixedNumberWordToLong() throws JsonParserException, ParsingException {
        assertEquals(10, Utils.mixedNumberWordToLong("10"));
        assertEquals(10.5e3, Utils.mixedNumberWordToLong("10.5K"), 0.0);
        assertEquals(10.5e6, Utils.mixedNumberWordToLong("10.5M"), 0.0);
        assertEquals(10.5e6, Utils.mixedNumberWordToLong("10,5M"), 0.0);
        assertEquals(1.5e9, Utils.mixedNumberWordToLong("1,5B"), 0.0);
    }

    public static void createFile(String path, String content) throws IOException {
        String[] dirs = path.split("/");
        if (dirs.length > 1) {
            String pathWithoutFileName = path.replace(dirs[dirs.length - 1], "");
            if (!Files.exists(Paths.get(pathWithoutFileName))) { //create dirs if they don't exist
                new File(pathWithoutFileName).mkdirs();
            }
        }
        writeFile(path, content);
    }

    //lower lever createFile. Doesn't create directories and takes only a String
    public static void writeFile(String path, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public static String jsonObjToString(JsonObject object) {
        return JsonWriter.string(object);
    }

    public static void createFile(String path, JsonObject content) throws IOException {
        createFile(path, jsonObjToString(content));
    }
}