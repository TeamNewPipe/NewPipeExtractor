package org.schabi.newpipe.extractor.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.schabi.newpipe.extractor.utils.io.SharpStream;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author kapodamy
 */
public class SubtitleConverter {
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final String NEW_LINE = "\r\n";

    public int dumpTTML(InputStream in, final SharpStream out, final boolean ignoreEmptyFrames, final boolean detectYoutubeDuplicateLines) {
        try {
            final int[] frame_index = {0};// ugly workaround
            final Charset charset = Charset.forName("utf-8");

            read_xml_based(in, new FrameWriter() {
                @Override
                public void yield(SubtitleFrame frame) throws IOException {
                    if (ignoreEmptyFrames && frame.isEmptyText()) {
                        return;
                    }
                    out.write(String.valueOf(frame_index[0]++).getBytes(charset));
                    out.write(NEW_LINE.getBytes(charset));
                    out.write(getTime(frame.start, true).getBytes(charset));
                    out.write(" --> ".getBytes(charset));
                    out.write(getTime(frame.end, true).getBytes(charset));
                    out.write(NEW_LINE.getBytes(charset));
                    out.write(frame.text.getBytes(charset));
                    out.write(NEW_LINE.getBytes(charset));
                    out.write(NEW_LINE.getBytes(charset));
                }
            }, detectYoutubeDuplicateLines, "tt", "xmlns", "http://www.w3.org/ns/ttml", new String[]{"tt", "body", "div", "p"}, "begin", "end", true);
        } catch (Exception err) {
            if (err instanceof IOException) {
                return 1;
            } else if (err instanceof ParseException) {
                return 2;
            } else if (err instanceof XmlPullParserException) {
                return 3;
            }
            return 4;
        }

        return 0;
    }

    private void read_xml_based(InputStream reader, FrameWriter callback, boolean detectYoutubeDuplicateLines,
                               String root, String formatAttr, String formatVersion, String[] framePath,
                               String timeAttr, String durationAttr, boolean hasTimestamp
    ) throws XmlPullParserException, IOException, ParseException {
        /*
         * XML based subtitles parser with BASIC support
         * multiple CUE is not supported
         * styling is not supported
         * tag timestamps (in auto-generated subtitles) are not supported, maybe in the future
         * also TimestampTagOption enum is not applicable
         * Language parsing is not supported
         */

        XmlDocument xml = new XmlDocument(reader, BUFFER_SIZE);
        String attr;

        // get the format version or namespace
        XmlNode node = xml.selectSingleNode(root);
        if (node == null) {
            throw new ParseException("Can't get the format version. ¿wrong namespace?", -1);
        }

        if (formatAttr.equals("xmlns")) {
            if (!node.getNameSpace().equals(formatVersion)) {
                throw new UnsupportedOperationException("Expected xml namespace: " + formatVersion);
            }
        } else {
            attr = node.getAttribute(formatAttr);
            if (attr == null) {
                throw new ParseException("Can't get the format attribute", -1);
            }
            if (!attr.equals(formatVersion)) {
                throw new ParseException("Invalid format version : " + attr, -1);
            }
        }

        XmlNodeList node_list;

        int line_break = 0;// Maximum characters per line if present (valid for TranScript v3)

        if (!hasTimestamp) {
            node_list = xml.selectNodes("timedtext", "head", "wp");

            if (node_list != null) {
                // if the subtitle has multiple CUEs, use the highest value
                while ((node = node_list.getNextNode()) != null) {
                    try {
                        int tmp = Integer.parseInt(node.getAttribute("ah"));
                        if (tmp > line_break) {
                            line_break = tmp;
                        }
                    } catch (NumberFormatException err) {
                    }
                }
            }
        }

        // parse every frame
        node_list = xml.selectNodes(framePath);

        if (node_list == null) {
            return;// no frames detected
        }

        int fs_ff = -1;// first timestamp of first frame
        boolean limit_lines = false;

        while ((node = node_list.getNextNode()) != null) {
            SubtitleFrame obj = new SubtitleFrame();
            obj.text = node.getInnerText();

            attr = node.getAttribute(timeAttr);// ¡this cant be null!
            obj.start = hasTimestamp ? parseTimestamp(attr) : Integer.parseInt(attr);

            attr = node.getAttribute(durationAttr);
            if (obj.text == null || attr == null) {
                continue;// normally is a blank line (on auto-generated subtitles) ignore
            }

            if (hasTimestamp) {
                obj.end = parseTimestamp(attr);

                if (detectYoutubeDuplicateLines) {
                    if (limit_lines) {
                        int swap = obj.end;
                        obj.end = fs_ff;
                        fs_ff = swap;
                    } else {
                        if (fs_ff < 0) {
                            fs_ff = obj.end;
                        } else {
                            if (fs_ff < obj.start) {
                                limit_lines = true;// the subtitles has duplicated lines
                            } else {
                                detectYoutubeDuplicateLines = false;
                            }
                        }
                    }
                }
            } else {
                obj.end = obj.start + Integer.parseInt(attr);
            }

            if (/*node.getAttribute("w").equals("1") &&*/line_break > 1 && obj.text.length() > line_break) {

                // implement auto line breaking (once)
                StringBuilder text = new StringBuilder(obj.text);
                obj.text = null;

                switch (text.charAt(line_break)) {
                    case ' ':
                    case '\t':
                        putBreakAt(line_break, text);
                        break;
                    default:// find the word start position
                        for (int j = line_break - 1; j > 0; j--) {
                            switch (text.charAt(j)) {
                                case ' ':
                                case '\t':
                                    putBreakAt(j, text);
                                    j = -1;
                                    break;
                                case '\r':
                                case '\n':
                                    j = -1;// long word, just ignore
                                    break;
                            }
                        }
                        break;
                }

                obj.text = text.toString();// set the processed text
            }

            callback.yield(obj);
        }
    }

    private static int parseTimestamp(String multiImpl) throws NumberFormatException, ParseException {
        if (multiImpl.length() < 1) {
            return 0;
        } else if (multiImpl.length() == 1) {
            return Integer.parseInt(multiImpl) * 1000;// ¡this must be a number in seconds!
        }

        // detect wallclock-time
        if (multiImpl.startsWith("wallclock(")) {
            throw new UnsupportedOperationException("Parsing wallclock timestamp is not implemented");
        }

        // detect offset-time
        if (multiImpl.indexOf(':') < 0) {
            int multiplier = 1000;
            char metric = multiImpl.charAt(multiImpl.length() - 1);
            switch (metric) {
                case 'h':
                    multiplier *= 3600000;
                    break;
                case 'm':
                    multiplier *= 60000;
                    break;
                case 's':
                    if (multiImpl.charAt(multiImpl.length() - 2) == 'm') {
                        multiplier = 1;// ms
                    }
                    break;
                default:
                    if (!Character.isDigit(metric)) {
                        throw new NumberFormatException("Invalid metric suffix found on : " + multiImpl);
                    }
                    metric = '\0';
                    break;
            }
            try {
                String offset_time = multiImpl;

                if (multiplier == 1) {
                    offset_time = offset_time.substring(0, offset_time.length() - 2);
                } else if (metric != '\0') {
                    offset_time = offset_time.substring(0, offset_time.length() - 1);
                }

                double time_metric_based = Double.parseDouble(offset_time);
                if (Math.abs(time_metric_based) <= Double.MAX_VALUE) {
                    return (int) (time_metric_based * multiplier);
                }
            } catch (Exception err) {
                throw new UnsupportedOperationException("Invalid or not implemented timestamp on: " + multiImpl);
            }
        }

        // detect clock-time
        int time = 0;
        String[] units = multiImpl.split(":");

        if (units.length < 3) {
            throw new ParseException("Invalid clock-time timestamp", -1);
        }

        time += Integer.parseInt(units[0]) * 3600000;// hours
        time += Integer.parseInt(units[1]) * 60000;//minutes
        time += Float.parseFloat(units[2]) * 1000f;// seconds and milliseconds (if present)

        // frames and sub-frames are ignored (not implemented)
        // time += units[3] * fps;
        return time;
    }

    private static void putBreakAt(int idx, StringBuilder str) {
        // this should be optimized at compile time

        if (NEW_LINE.length() > 1) {
            str.delete(idx, idx + 1);// remove after replace
            str.insert(idx, NEW_LINE);
        } else {
            str.setCharAt(idx, NEW_LINE.charAt(0));
        }
    }

    private static String getTime(int time, boolean comma) {
        // cast every value to integer to avoid auto-round in ToString("00").
        StringBuilder str = new StringBuilder(12);
        str.append(numberToString(time / 1000 / 3600, 2));// hours
        str.append(':');
        str.append(numberToString(time / 1000 / 60 % 60, 2));// minutes
        str.append(':');
        str.append(numberToString(time / 1000 % 60, 2));// seconds
        str.append(comma ? ',' : '.');
        str.append(numberToString(time % 1000, 3));// miliseconds

        return str.toString();
    }

    private static String numberToString(int nro, int pad) {
        return String.format(Locale.ENGLISH, "%0".concat(String.valueOf(pad)).concat("d"), nro);
    }

    /**
     * XmlPullParser wrapper
     * @param parser XmlPullParser instance
     * @param name node name
     * @param depth current tree deep
     * @return true if the node was reached, otherwise, false
     * @throws XmlPullParserException if cant read the next XML tag
     * @throws IOException I/O error
     */
    private static boolean getNextNode(XmlPullParser parser, String name, int depth) throws XmlPullParserException, IOException {
        int cursor = 0;
        int eventType = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    int tmp = parser.getDepth();

                    if (tmp < depth) {
                        return false;
                    }
                    if (tmp == depth && cursor == 0 && parser.getName().equals(name)) {
                        return true;
                    }
                    cursor++;
                    break;
                case XmlPullParser.END_TAG:
                    if (cursor > 0) {
                        cursor--;
                    }
            }
        }

        return false;
    }


    /******************
     * helper classes *
     ******************/

    private interface FrameWriter {

        void yield(SubtitleFrame frame) throws IOException;
    }

    private static class SubtitleFrame {
        //Java no support unsigned int

        public int end;
        public int start;
        public String text = "";

        private boolean isEmptyText() {
            if (text == null) {
                return true;
            }

            for (int i = 0; i < text.length(); i++) {
                switch (text.charAt(i)) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        break;
                    default:
                        return false;
                }
            }

            return true;
        }
    }

    private class XmlDocument {
        private BufferedInputStream src;
        private XmlPullParserFactory fac;

        XmlDocument(InputStream stream, int bufferSize) throws XmlPullParserException {
            // due how xml parsing works is necessary a wrapper
            src = new BufferedInputStream(stream, bufferSize);
            src.mark(0);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            fac = factory;
        }

        XmlNode selectSingleNode(String... path) throws XmlPullParserException, IOException {
            if (path.length < 1) {
                return null;
            }

            src.reset();// ¡this is very much important!

            XmlPullParser parser = fac.newPullParser();
            parser.setInput(src, null);

            for (int i = 0; i < path.length; i++) {
                if (!getNextNode(parser, path[i], i + 1)) {
                    return null;
                }
            }

            return new XmlNode(parser);
        }

        XmlNodeList selectNodes(String... path) throws XmlPullParserException, IOException {
            XmlNode node = selectSingleNode(path);
            if (node == null) {
                return null;
            }

            return new XmlNodeList(node.parser);
        }

    }

    private class XmlNode {
        XmlPullParser parser;

        XmlNode(XmlPullParser parser) {
            this.parser = parser;
        }

        private void init_attrs() {
            if (attrs != null) {
                return;
            }

            // backup attributes first
            attrs = new HashMap<String, String>(parser.getAttributeCount());
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                attrs.put(parser.getAttributeName(i), parser.getAttributeValue(i));
            }
        }

        String getText() throws IOException, XmlPullParserException {
            init_attrs();

            int eventType = 0;
            boolean crash = false;
            int deep = parser.getDepth();

            while (!crash && eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.next();

                switch (eventType) {
                    case XmlPullParser.TEXT:
                        if (parser.getDepth() != deep) {
                            continue;
                        }
                        return parser.getText();
                    case XmlPullParser.END_TAG:
                        if (parser.getDepth() > deep) {
                            continue;
                        }
                        return null;
                    case XmlPullParser.START_TAG:
                        if (parser.getDepth() < deep) {
                            crash = true;
                        }
                        break;
                }
            }

            throw new XmlPullParserException("cant read the text node, XmlPullParser crashed");
        }

        String getInnerText() throws IOException, XmlPullParserException {
            init_attrs();

            int eventType = 0;
            boolean crash = false;
            int deep = parser.getDepth();
            StringBuilder buffer = new StringBuilder(128);

            while (!crash && eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.next();

                switch (eventType) {
                    case XmlPullParser.TEXT:
                        String str = parser.getText();
                        if (str != null) {
                            buffer.append(str);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getDepth() > deep) {
                            continue;
                        }
                        return buffer.toString();
                    case XmlPullParser.START_TAG:
                        if (parser.getDepth() < deep) {
                            crash = true;
                        }
                        break;
                }
            }

            throw new XmlPullParserException("cant read the text node, XmlPullParser crashed");
        }

        String getAttribute(String name) {
            return attrs == null ? parser.getAttributeValue(null, name) : attrs.get(name);
        }

        String getNameSpace() {
            return parser.getNamespace();
        }

        private Map<String, String> attrs;
    }

    private class XmlNodeList {
        private XmlPullParser parser;
        boolean first = true;
        String node_name;
        int node_depth;

        XmlNodeList(XmlPullParser parser) {
            this.parser = parser;
            node_name = parser.getName();
            node_depth = parser.getDepth();
        }

        XmlNode getNextNode() throws XmlPullParserException, IOException {
            if (first) {
                first = false;
                return new XmlNode(parser);
            }

            if (!SubtitleConverter.getNextNode(parser, node_name, node_depth)) {
                parser = null;
            }

            return parser == null ? null : new XmlNode(parser);
        }
    }
}
