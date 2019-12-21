// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONException;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.Arrays;

public class BandcampExtractorHelper {

    /**
     * Get JSON behind <code>var $variable = </code> out of web page
     * <br/<br/>
     * Originally a part of bandcampDirect.
     *
     * @param html     The HTML where the JSON we're looking for is stored inside a
     *                 variable inside some JavaScript block
     * @param variable Name of the variable
     * @return The JsonObject stored in the variable with this name
     */
    public static JSONObject getJSONFromJavaScriptVariables(String html, String variable) throws JSONException, ParsingException {

        String[] part = html.split("var " + variable + " = ");

        String firstHalfGone = part[1];

        firstHalfGone = firstHalfGone.replaceAll("\" \\+ \"", "");

        int position = -1;
        int level = 0;
        for (char character : firstHalfGone.toCharArray()) {
            position++;

            switch (character) {
                case '{':
                    level++;
                    continue;
                case '}':
                    level--;
                    if (level == 0) {
                        return new JSONObject(firstHalfGone.substring(0, position + 1)
                                .replaceAll(" {4}//.+", "") // Remove comments in JSON
                        );
                    }
            }
        }

        throw new ParsingException("Unexpected HTML: JSON never ends");
    }

    /**
     * Concatenate all non-null and non-empty strings together while separating them using
     * the comma parameter
     */
    public static String smartConcatenate(String[] strings, String comma) {
        StringBuilder result = new StringBuilder();

        // Remove empty strings
        ArrayList<String> list = new ArrayList<>(Arrays.asList(strings));
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == null || list.get(i).isEmpty()) {
                list.remove(i);
            }
        }

        // Append remaining strings to result
        for (int i = 0; i < list.size(); i++) {
            String string = list.get(i);
            result.append(string);

            if (i != list.size() - 1) {
                // This is not the last iteration yet
                result.append(comma);
            }

        }

        return String.valueOf(result);
    }
}
