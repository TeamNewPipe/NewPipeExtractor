import com.grack.nanojson.JsonAppendableWriter;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class GenerateOverview {

    public static void main(String[] args) throws Exception {
        Map<String, Map<String, Collection<String>>> outMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        for (String unitName : Arrays.asList("seconds", "minutes", "hours", "days", "weeks", "months", "years")) {
            JsonObject object = JsonParser.object().from(new FileInputStream(new File("timeago-parser/raw/times/" + unitName + ".json")));

            for (Map.Entry<String, Object> timeKeyValue : object.entrySet()) {
                JsonObject timeObject = (JsonObject) timeKeyValue.getValue();
                for (Map.Entry<String, Object> langsKeyValue : timeObject.entrySet()) {
                    String langKey = langsKeyValue.getKey();
                    String langValue = langsKeyValue.getValue().toString();

                    Map<String, Collection<String>> langUnitsMap;
                    if (outMap.containsKey(langKey)) {
                        langUnitsMap = outMap.get(langKey);
                    } else {
                        langUnitsMap = new TreeMap<>(Utils.compareByUnitName());
                        outMap.put(langKey, langUnitsMap);
                    }

                    Collection<String> langUnitListValues;
                    if (langUnitsMap.containsKey(unitName)) {
                        langUnitListValues = langUnitsMap.get(unitName);
                    } else {
                        langUnitListValues = new TreeSet<>(Utils.compareByNumber());
                        langUnitsMap.put(unitName, langUnitListValues);
                    }


                    langUnitListValues.add(langValue);
                }
            }
        }

        writeMapTo(outMap, JsonWriter.indent("  ").on(new FileOutputStream(new File("timeago-parser/raw/overview.json"))));
    }

    public static void writeMapTo(Map<String, Map<String, Collection<String>>> outMap, JsonAppendableWriter out) {
        out.object();
        for (Map.Entry<String, Map<String, Collection<String>>> langMapEntry : outMap.entrySet()) {
            final String langName = langMapEntry.getKey();
            out.object(langName);
            for (Map.Entry<String, Collection<String>> langValuesEntry : langMapEntry.getValue().entrySet()) {
                final String unitName = langValuesEntry.getKey();
                out.array(unitName);
                for (String timeValue : langValuesEntry.getValue()) out.value(timeValue);
                out.end();
            }
            out.end();
        }
        out.end().done();
    }

}
