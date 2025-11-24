package org.schabi.newpipe.timeago_generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

public class GeneratePatternClasses {
    public static void main(String[] args) throws FileNotFoundException, JsonParserException {
        final InputStream resourceAsStream =
                new FileInputStream("timeago-parser/raw/unique_patterns.json");

        final JsonObject from = JsonParser.object().from(resourceAsStream);
        final TreeMap<String, Object> map = new TreeMap<>(from);

        final StringBuilder patternMapEntries = new StringBuilder();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final String languageCode = entry.getKey();
            final String formattedCode = languageCode.replace('-', '_');
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
            specialCasesConstruct(ChronoUnit.SECONDS, seconds, specialCasesString);
            specialCasesConstruct(ChronoUnit.MINUTES, minutes, specialCasesString);
            specialCasesConstruct(ChronoUnit.HOURS, hours, specialCasesString);
            specialCasesConstruct(ChronoUnit.DAYS, days, specialCasesString);
            specialCasesConstruct(ChronoUnit.WEEKS, weeks, specialCasesString);
            specialCasesConstruct(ChronoUnit.MONTHS, months, specialCasesString);
            specialCasesConstruct(ChronoUnit.YEARS, years, specialCasesString);

            System.out.println("Generating \"" + languageCode + "\" pattern class...");


            try (final FileWriter fileOut = new FileWriter(
                    "timeago-parser/src/main/java/org/schabi/newpipe/extractor/timeago/patterns/" +
                            formattedCode + ".java")) {
                final String test = INFO_CLASS_GENERATED + "\n" +
                        "\n" +
                        "package org.schabi.newpipe.extractor.timeago.patterns;\n\n" +
                        "import org.schabi.newpipe.extractor.timeago.PatternsHolder;\n" +
                        (specialCasesString.length() > 0 ? "\nimport java.time.temporal.ChronoUnit;\n" : "") +
                        "\n" +
                        "public class " + formattedCode + " extends PatternsHolder {\n" +
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
                        "    private static final " + formattedCode + " INSTANCE = new " + formattedCode + "();\n" +
                        "\n" +
                        "    public static " + formattedCode + " getInstance() {\n" +
                        "        return INSTANCE;\n" +
                        "    }\n" +
                        "\n" +
                        "    private " + formattedCode + "() {\n" +
                        "        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);\n" +
                        specialCasesString +
                        "    }\n" +
                        "}";
                fileOut.write(test);
            } catch (IOException e) {
                e.printStackTrace();
            }

            patternMapEntries.append("        patternMap.put(\"")
                    .append(languageCode).append("\", ")
                    .append(formattedCode).append(".getInstance());\n");
        }

        try (final FileWriter fileOut = new FileWriter(
                "timeago-parser/src/main/java/org/schabi/newpipe/extractor/timeago/PatternMap.java")) {
            final String patternMapClass = INFO_CLASS_GENERATED + "\n" +
                    "\n" +
                    "package org.schabi.newpipe.extractor.timeago;\n\n" +
                    "import org.schabi.newpipe.extractor.timeago.patterns.*;\n" +
                    "import java.util.HashMap;\n" +
                    "import java.util.Map;\n\n" +
                    "public class PatternMap {\n" +
                    "    private static final Map<String, PatternsHolder> patternMap = new HashMap<>();\n" +
                    "\n" +
                    "    static {\n" +
                    patternMapEntries +
                    "    }\n" +
                    "\n" +
                    "    public static PatternsHolder getPattern(final String languageCode) {\n" +
                    "        return patternMap.get(languageCode);\n" +
                    "    }\n" +
                    "}";
            fileOut.write(patternMapClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void specialCasesConstruct(ChronoUnit unit, JsonArray array, StringBuilder stringBuilder) {
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
                            .append("putSpecialCase(ChronoUnit.").append(unit.name())
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
