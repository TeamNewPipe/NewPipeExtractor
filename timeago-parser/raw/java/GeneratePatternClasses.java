import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.timeago.PatternsHolder;
import org.schabi.newpipe.extractor.timeago.TimeAgoUnit;

import java.io.*;
import java.util.*;

public class GeneratePatternClasses {
    public static void main(String[] args) throws FileNotFoundException, JsonParserException {
        final InputStream resourceAsStream =
                new FileInputStream("timeago-parser/raw/unique_patterns.json");

        final JsonObject from = JsonParser.object().from(resourceAsStream);
        final TreeMap<String, Object> map = new TreeMap<>(from);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final String languageCode = entry.getKey().replace('-', '_');
            final Map<String, Object> unitsList = (Map<String, Object>) entry.getValue();

            final String wordSeparator = (String) unitsList.get("word_separator");

            final JsonArray seconds = (JsonArray) unitsList.get("seconds");
            final JsonArray minutes = (JsonArray) unitsList.get("minutes");
            final JsonArray hours = (JsonArray) unitsList.get("hours");
            final JsonArray days = (JsonArray) unitsList.get("days");
            final JsonArray weeks = (JsonArray) unitsList.get("weeks");
            final JsonArray months = (JsonArray) unitsList.get("months");
            final JsonArray years = (JsonArray) unitsList.get("years");

            final StringBuilder specialCasesString = new StringBuilder();
            specialCasesConstruct(TimeAgoUnit.SECONDS, seconds, specialCasesString);
            specialCasesConstruct(TimeAgoUnit.MINUTES, minutes, specialCasesString);
            specialCasesConstruct(TimeAgoUnit.HOURS, hours, specialCasesString);
            specialCasesConstruct(TimeAgoUnit.DAYS, days, specialCasesString);
            specialCasesConstruct(TimeAgoUnit.WEEKS, weeks, specialCasesString);
            specialCasesConstruct(TimeAgoUnit.MONTHS, months, specialCasesString);
            specialCasesConstruct(TimeAgoUnit.YEARS, years, specialCasesString);

            System.out.println("Generating \"" + languageCode + "\" pattern class...");

            try (final FileWriter fileOut = new FileWriter(
                    "timeago-parser/src/main/java/org/schabi/newpipe/extractor/timeago/patterns/" +
                            languageCode + ".java")) {
                final String test = INFO_CLASS_GENERATED + "\n" +
                        "\n" +
                        "package org.schabi.newpipe.extractor.timeago.patterns;\n\n" +
                        "import org.schabi.newpipe.extractor.timeago.PatternsHolder;\n" +
                        (specialCasesString.length() > 0 ? "import org.schabi.newpipe.extractor.timeago.TimeAgoUnit;\n" : "") +
                        "\n" +
                        "public class " + languageCode + " extends PatternsHolder {\n" +
                        "    private static final String WORD_SEPARATOR = \"" + wordSeparator + "\";\n" +
                        "    private static final String[]\n" +
                        "            SECONDS  /**/ = {" + join(seconds) + "},\n" +
                        "            MINUTES  /**/ = {" + join(minutes) + "},\n" +
                        "            HOURS    /**/ = {" + join(hours) + "},\n" +
                        "            DAYS     /**/ = {" + join(days) + "},\n" +
                        "            WEEKS    /**/ = {" + join(weeks) + "},\n" +
                        "            MONTHS   /**/ = {" + join(months) + "},\n" +
                        "            YEARS    /**/ = {" + join(years) + "};\n" +
                        "\n" +
                        "    private static final " + languageCode + " INSTANCE = new " + languageCode + "();\n" +
                        "\n" +
                        "    public static " + languageCode + " getInstance() {\n" +
                        "        return INSTANCE;\n" +
                        "    }\n" +
                        "\n" +
                        "    private " + languageCode + "() {\n" +
                        "        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);\n" +
                        specialCasesString.toString() +
                        "    }\n" +
                        "}";
                fileOut.write(test);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void specialCasesConstruct(TimeAgoUnit unit, JsonArray array, StringBuilder stringBuilder) {
        final Iterator<Object> iterator = array.iterator();
        while (iterator.hasNext()) {
            final Object o = iterator.next();
            if (o instanceof JsonObject) {
                final JsonObject caseObject = (JsonObject) o;
                for (Map.Entry<String, Object> caseEntry : caseObject.entrySet()) {
                    final int caseAmount = Integer.parseInt(caseEntry.getKey());
                    final String caseText = (String) caseEntry.getValue();
                    iterator.remove();

                    stringBuilder.append("        ")
                            .append("putSpecialCase(TimeAgoUnit.").append(unit.name())
                            .append(", \"").append(caseText).append("\"")
                            .append(", ").append(caseAmount).append(");").append("\n");
                }
            }
        }
    }

    private static final String INFO_CLASS_GENERATED = "/**/// DO NOT MODIFY THIS FILE MANUALLY\n" +
            "/**/// This class was automatically generated by \"GeneratePatternClasses.java\",\n" +
            "/**/// modify the \"unique_patterns.json\" and re-generate instead.";

    private static String join(List<Object> list) {
        final StringBuilder toReturn = new StringBuilder();

        for (Object o : list) {
            toReturn.append('"').append(o).append('"').append(", ");
        }
        toReturn.setLength(Math.max(toReturn.length() - 2, 0));

        return toReturn.toString();
    }
}