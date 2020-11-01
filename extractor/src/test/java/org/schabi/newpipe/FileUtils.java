package org.schabi.newpipe;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Util class to write file to disk
 * <p>
 * Can be used to debug and test, for example writing a service's JSON response
 * (especially useful if the response provided by the service is not documented)
 */
public class FileUtils {

    public static void createFile(String path, JsonObject content) throws IOException {
        createFile(path, jsonObjToString(content));
    }

    public static void createFile(String path, JsonArray array) throws IOException {
        createFile(path, jsonArrayToString(array));
    }

    /**
     * Create a file given a path and its content. Create subdirectories if needed
     *
     * @param path    the path to write the file, including the filename (and its extension)
     * @param content the content to write
     * @throws IOException
     */
    public static void createFile(final String path, final String content) throws IOException {
        final String[] dirs = path.split("/");
        if (dirs.length > 1) {
            String pathWithoutFileName = path.replace(dirs[dirs.length - 1], "");
            if (!Files.exists(Paths.get(pathWithoutFileName))) { //create dirs if they don't exist
                if (!new File(pathWithoutFileName).mkdirs()) {
                    throw new IOException("An error occurred while creating directories");
                }
            }
        }
        writeFile(path, content);
    }

    /**
     * Write a file to disk
     *
     * @param filename the file name (and its extension if wanted)
     * @param content  the content to write
     * @throws IOException
     */
    private static void writeFile(final String filename, final String content) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(content);
        writer.flush();
        writer.close();
    }

    /**
     * Resolves the test resource file based on its filename. Looks in
     * {@code extractor/src/test/resources/} and {@code src/test/resources/}
     * @param filename the resource filename
     * @return the resource file
     */
    public static File resolveTestResource(final String filename) {
        final File file = new File("extractor/src/test/resources/" + filename);
        if (file.exists()) {
            return file;
        } else {
            return new File("src/test/resources/" + filename);
        }
    }

    /**
     * Convert a JSON object to String
     * toString() does not produce a valid JSON string
     */
    public static String jsonObjToString(JsonObject object) {
        return JsonWriter.string(object);
    }

    /**
     * Convert a JSON array to String
     * toString() does not produce a valid JSON string
     */
    public static String jsonArrayToString(JsonArray array) {
        return JsonWriter.string(array);
    }
}
