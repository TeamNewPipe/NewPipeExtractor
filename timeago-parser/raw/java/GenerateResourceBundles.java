import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.schabi.newpipe.extractor.timeago.TimeAgoPatternsManager.RESOURCE_BUNDLE_ARRAY_SEPARATOR;

public class GenerateResourceBundles {

    public static void main(String[] args) throws Exception {
        File outDir = new File("timeago-parser/outBundle");
        if (!outDir.isDirectory()) outDir.mkdir();

        JsonObject object = JsonParser.object().from(new FileInputStream(new File("timeago-parser/raw/unique_patterns.json")));

        for (Map.Entry<String, Object> langTimeEntry : new TreeMap<>(object).entrySet()) {
            final String langName = langTimeEntry.getKey();
            StringBuilder outString = new StringBuilder();

            final TreeMap<String, Object> sortedMap = new TreeMap<>(Utils.compareByUnitName());
            sortedMap.putAll((JsonObject) langTimeEntry.getValue());

            final Iterator<Map.Entry<String, Object>> unitEntriesIterator = sortedMap.entrySet().iterator();
            while (unitEntriesIterator.hasNext()) {
                final Map.Entry<String, Object> unitEntry = unitEntriesIterator.next();
                final String unitName = unitEntry.getKey();
                final List<Object> unitList = (JsonArray) unitEntry.getValue();

                outString.append(unitName).append("=\\\n");

                for (int i = 0; i < unitList.size(); i++) {
                    final String s = unitList.get(i).toString();
                    outString.append("  ").append(s);

                    if (i < unitList.size() - 1) {
                        outString.append(RESOURCE_BUNDLE_ARRAY_SEPARATOR).append("\\").append("\n");
                    }
                }

                if (unitEntriesIterator.hasNext()) outString.append("\n\n");
            }

            String fileName = "time_units_" + langName.replaceAll("-", "_") + ".properties";
            System.out.println("Writing " + fileName + "...");
            try (OutputStream out = new FileOutputStream(new File(outDir, fileName))) {
                out.write(outString.toString().getBytes("UTF-8"));
            }
        }

    }
}
