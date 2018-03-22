import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Map;

public class CheckAll {

    public static void main(String[] args) throws Exception {
        int SECONDS = 59, currentSeconds = 0;
        int MINUTES = 59, currentMinutes = 0;
        int HOURS = 23, currentHours = 0;
        int DAYS = 6, currentDays = 0;
        int WEEKS = 4, currentWeeks = 0;
        int MONTHS = 11, currentMonths = 0;
        int YEARS = 12, currentYears = 0;

        for (String name : Arrays.asList("seconds", "minutes", "hours", "days", "weeks", "months", "years")) {
            JsonObject object = JsonParser.object().from(new FileInputStream(new File("timeago-parser/raw/times/" + name + ".json")));

            for (Map.Entry<String, Object> entry : object.entrySet()) {
                JsonObject value = (JsonObject) entry.getValue();

                final int size = value.keySet().size();
                if (size >= 80) {
                    if (name.equals("seconds")) currentSeconds++;
                    if (name.equals("minutes")) currentMinutes++;
                    if (name.equals("hours")) currentHours++;
                    if (name.equals("days")) currentDays++;
                    if (name.equals("weeks")) currentWeeks++;
                    if (name.equals("months")) currentMonths++;
                    if (name.equals("years")) currentYears++;
                } else {
                    System.err.println("Missing some units in: " + name + " → " + entry.getKey() + " (current size = " + size + ")");
                }

                String number = entry.getKey().replaceAll("\\D", "");
                for (Map.Entry<String, Object> langsKeys : value.entrySet()) {
                    String lang = langsKeys.getKey();
                    String langValue = String.valueOf(langsKeys.getValue());

                    String langValueNumber = langValue.replaceAll("\\D", "");
                    if (!langValueNumber.equals(number)) {
                        final String msg = langValueNumber.isEmpty() ? "doesn't contain number" : "different number";
                        System.out.printf("%-20s[!]   %22s: %10s   = %s \n", entry.getKey(), msg, lang, langValue);
                    }
                }
            }
        }
        System.out.println("\n\nHow many:\n");

        if (currentSeconds == SECONDS) System.out.println("seconds: " + currentSeconds);
        else System.out.println("[!] missing seconds: " + currentSeconds);

        if (currentMinutes == MINUTES) System.out.println("minutes: " + currentMinutes);
        else System.out.println("[!] missing minutes: " + currentMinutes);

        if (currentHours == HOURS) System.out.println("hours: " + currentHours);
        else System.out.println("[!] missing hours: " + currentHours);

        if (currentDays == DAYS) System.out.println("days: " + currentDays);
        else System.out.println("[!] missing days: " + currentDays);

        if (currentWeeks == WEEKS) System.out.println("weeks: " + currentWeeks);
        else System.out.println("[!] missing weeks: " + currentWeeks);

        if (currentMonths == MONTHS) System.out.println("months: " + currentMonths);
        else System.out.println("[!] missing months: " + currentMonths);

        if (currentYears == YEARS) System.out.println("years: " + currentYears);
        else System.out.println("[!] missing years: " + currentYears);
    }
}
