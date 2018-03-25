import java.util.*;

public class Utils {

    static Comparator<String> compareByNumber() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }

            private int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        };
    }

    static Comparator<Object> compareByUnitName() {
        return new Comparator<Object>() {
            private final List<String> ORDER = Arrays.asList("seconds", "minutes", "hours", "days", "weeks", "months", "years");

            @Override
            public int compare(Object o1, Object o2) {
                return Integer.compare(ORDER.indexOf(o1.toString()), ORDER.indexOf(o2.toString()));
            }
        };
    }
}
