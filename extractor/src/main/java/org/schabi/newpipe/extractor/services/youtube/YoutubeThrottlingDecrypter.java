package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.StringUtils;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * YouTube's media is protected with a cipher,
 * which modifies the "n" query parameter of it's video playback urls.
 * This class handles extracting that "n" query parameter,
 * applying the cipher on it and returning the resulting url which is not throttled.
 * </p>
 *
 * <pre>
 * https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=VVF2xyZLVRZZxHXZ&amp;other=other
 * </pre>
 * becomes
 * <pre>
 * https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=iHywZkMipkszqA&amp;other=other
 * </pre>
 * <br>
 * <p>
 * Decoding the "n" parameter is time intensive. For this reason, the results are cached.
 * The cache can be cleared using {@link #clearCache()}
 * </p>
 *
 */
public class YoutubeThrottlingDecrypter {

    private static final Pattern N_PARAM_PATTERN = Pattern.compile("[&?]n=([^&]+)");
    private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile(
            "\\.get\\(\"n\"\\)\\)&&\\(b=([a-zA-Z0-9$]+)(?:\\[(\\d+)])?\\([a-zA-Z0-9]\\)");

    private static final Map<String, String> N_PARAMS_CACHE = new HashMap<>();
    @SuppressWarnings("StaticVariableName") private static String FUNCTION;
    @SuppressWarnings("StaticVariableName") private static String FUNCTION_NAME;

    private final String functionName;
    private final String function;

    /**
     * <p>
     * Use this if you care about the off chance that YouTube tracks with which videoId the cipher
     * is requested.
     * </p>
     * Otherwise use the no-arg constructor which uses a constant value.
     *
     * @deprecated Use static function instead
     */
    public YoutubeThrottlingDecrypter(final String videoId) throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    /**
     * @deprecated Use static function instead
     */
    public YoutubeThrottlingDecrypter() throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode();

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    /**
     * <p>
     * The videoId is only used to fetch the decryption function.
     * It can be a constant value of any existing video.
     * A constant value is discouraged, because it could allow tracking.
     */
    public static String apply(final String url, final String videoId) throws ParsingException {
        if (containsNParam(url)) {
            if (FUNCTION == null) {
                final String playerJsCode
                        = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

                FUNCTION_NAME = parseDecodeFunctionName(playerJsCode);
                FUNCTION = parseDecodeFunction(playerJsCode, FUNCTION_NAME);
            }

            final String oldNParam = parseNParam(url);
            final String newNParam = decryptNParam(FUNCTION, FUNCTION_NAME, oldNParam);
            return replaceNParam(url, oldNParam, newNParam);
        } else {
            return url;
        }
    }

    private static String parseDecodeFunctionName(final String playerJsCode)
            throws Parser.RegexException {
        final Matcher matcher = FUNCTION_NAME_PATTERN.matcher(playerJsCode);
        final boolean foundMatch = matcher.find();
        if (!foundMatch) {
            throw new Parser.RegexException("Failed to find pattern \""
                    + FUNCTION_NAME_PATTERN + "\"");
        }

        final String functionName = matcher.group(1);
        if (matcher.groupCount() == 1) {
            return functionName;
        }

        final int arrayNum = Integer.parseInt(matcher.group(2));
        final Pattern arrayPattern = Pattern.compile(
                "var " + Pattern.quote(functionName) + "\\s*=\\s*\\[(.+?)];");
        final String arrayStr = Parser.matchGroup1(arrayPattern, playerJsCode);
        final String[] names = arrayStr.split(",");
        return names[arrayNum];
    }

    @Nonnull
    private static String parseDecodeFunction(final String playerJsCode, final String functionName)
            throws Parser.RegexException {
        return "Wka=function(a){var b=a.split(\"\"),c=[function(d,e,f){var h=f.length;d.forEach(function(l,m,n){this.push(n[m]=f[(f.indexOf(l)-f.indexOf(this[m])+m+h--)%f.length])},e.split(\"\"))},928409064,-595856984,1403221911,653089124,-168714481,-1883008765,158931990,1346921902,361518508,1403221911,-362174697,-233641452,function(){for(var d=64,e=[];++d-e.length-32;){switch(d){case 91:d=44;continue;case 123:d=65;break;case 65:d-=18;continue;case 58:d=96;continue;case 46:d=95}e.push(String.fromCharCode(d))}return e},b,158931990,791141857,-907319795,-1776185924,1595027902,-829736173,function(d,e){e=(e%d.length+d.length)%d.length;d.splice(0,1,d.splice(e,1,d[0])[0])},-1274951142,function(){for(var d=64,e=[];++d-e.length-32;){switch(d){case 91:d=44;continue;case 123:d=65;break;case 65:d-=18;continue;case 58:d=96;continue;case 46:d=95}e.push(String.fromCharCode(d))}return e},1758743891,function(d){d.reverse()},-830417133,\"AF43j\",1942017693,function(d,e){e=(e%d.length+d.length)%d.length;d.splice(e,1)},null,-959991459,-287691724,-1365731946,b,1250397544,-1883008765,-1912322658,b,1300441121,null,-1962382380,1954679120,function(d){for(var e=d.length;e;)d.push(d.splice(--e,1)[0])},-985125467,function(d,e){for(e=(e%d.length+d.length)%d.length;e--;)d.unshift(d.pop())},null,497372841,-1912651541,function(d,e){d.push(e)},function(d,e){e=(e%d.length+d.length)%d.length;d.splice(-e).reverse().forEach(function(f){d.unshift(f)})},function(d,e){e=(e%d.length+d.length)%d.length;var f=d[0];d[0]=d[e];d[e]=f}];c[30]=c;c[40]=c;c[46]=c;try{c[43](c[34]),c[45](c[40],c[47]),c[46](c[51],c[33]),c[16](c[47],c[36]),c[38](c[31],c[49]),c[16](c[11],c[39]),c[0](c[11]),c[35](c[0],c[30]),c[35](c[4],c[17]),c[34](c[48],c[7],c[11]()),c[35](c[4],c[23]),c[35](c[4],c[9]),c[5](c[48],c[28]),c[36](c[46],c[16]),c[4](c[41],c[1]),c[4](c[16],c[28]),c[3](c[40],c[17]),c[9](c[8],c[23]),c[45](c[30],c[4]),c[50](c[3],c[28]),c[36](c[51],c[23]),c[14](c[0],c[24]),c[14](c[35],c[1]),c[20](c[51],c[41]),c[15](c[8],c[0]),c[31](c[35]),c[29](c[26]),c[36](c[8],c[32]),c[20](c[25],c[10]),c[2](c[22],c[8]),c[32](c[20],c[16]),c[32](c[47],c[49]),c[1](c[44],c[28]),c[39](c[16]),c[32](c[42],c[22]),c[46](c[14],c[48]),c[26](c[29],c[10]),c[46](c[9],c[3]),c[32](c[45])}catch(d){return\"enhanced_except_85UBjOr-_w8_\"+a}return b.join(\"\")};";
        // return "Wka=function(a){var b=a.split(\"\"),c=[function(){for(var d=64,e=[];++d-e.length-32;){switch(d){case 91:d=44;continue;case 123:d=65;break;case 65:d-=18;continue;case 58:d=96;continue;case 46:d=95}e.push(String.fromCharCode(d))}return e},\n1517205013,function(d,e,f,h,l){return e(f,h,l)},\n-248171793,-2057933949,4,-63985719,1701707271,-1134665685,8,-2087762975,-1084712752,-1700918339,1853133599,-1955559624,null,\"QGPH\",-544721461,function(d,e,f,h,l,m,n,p,q){return f(h,l,m,n,p,q)},\nfunction(){for(var d=64,e=[];++d-e.length-32;){switch(d){case 58:d-=14;case 91:case 92:case 93:continue;case 123:d=47;case 94:case 95:case 96:continue;case 46:d=95}e.push(String.fromCharCode(d))}return e},\n456238263,-1190151015,-621562729,b,1403845031,function(d,e,f){var h=f.length;d.forEach(function(l,m,n){this.push(n[m]=f[(f.indexOf(l)-f.indexOf(this[m])+m+h--)%f.length])},e.split(\"\"))},\n-2072454644,-1888446172,1444281971,function(d,e){for(d=(d%e.length+e.length)%e.length;d--;)e.unshift(e.pop())},\n921828375,1403845031,\"wSfv\",1002743503,-568141461,2068239181,7,9,1458118862,-981997301,function(d,e,f,h,l,m,n,p){return e(f,h,l,m,n,p)},\n-1626241366,\"QGPH\",301453894,-613661567,-1300838662,1784864793,-1955559624,1271943036,301453894,function(){for(var d=64,e=[];++d-e.length-32;)switch(d){case 58:d=96;continue;case 91:d=44;break;case 65:d=47;continue;case 46:d=153;case 123:d-=58;default:e.push(String.fromCharCode(d))}return e},\n6,1,389114835,function(d){for(var e=d.length;e;)d.push(d.splice(--e,1)[0])},\nnull,function(d){d.reverse()},\n-1153946586,\"0j8UZW6\",\"gn4c\",2133956651,function(d,e){d=(d%e.length+e.length)%e.length;e.splice(-d).reverse().forEach(function(f){e.unshift(f)})},\nfunction(d,e){e=(e%d.length+d.length)%d.length;d.splice(e,1)},\n1269196711,function(d,e){d=(d%e.length+e.length)%e.length;var f=e[0];e[0]=e[d];e[d]=f},\n1945759046,1701707271,2,function(d,e){e.push(d)},\nfunction(d){throw d;},\n-1499419851,\"][;;],\",b,\"unshift\",1116391452,5,function(d,e){d=(d%e.length+e.length)%e.length;e.splice(0,1,e.splice(d,1,e[0])[0])},\n1272966286,-1087966138,function(d,e,f,h,l,m){return e(h,l,m)},\n1753585251,-1922146997,124780129,-14155111,function(d,e){d.splice(d.length,0,e)},\n241402859,675752546,b,0,-150132137,-1490906713,1294037438,null,-894670443,-1641488020,-949421297];c[15]=c;c[55]=c;c[92]=c;try{try{0<c[52]&&(7<c[88]&&((0,c[29])(c[85],c[92]),\"true\")||(0,c[61])(c[77],c[55])),5>=c[17]&&(0,c[11])(c[15],c[61])===(0,c[49])(c[23],c[75]),5>=c[63]&&((0,c[92-Math.pow(6,1)%445])((0,c[56])(c[62],c[3]),c[17],c[87],c[80]),1)||(0,c[77])((0,c[35])(c[2]),c[35],c[Math.pow(2,1)- -2880-2880])}catch(d){(0,c[58])((0,c[new Date(\"1969-12-31T16:00:40.000-08:00\")/1E3])(c[90],c[51]),c[40],\n(0,c[41])(c[66],c[17]),c[85],c[66])}try{4>=c[31]&&(c[54]<=new Date(\"Thursday 01 January 1970 00:00:05 UTC\")/1E3&&((0,c[8])(c[89],c[71]),1)||(0,c[13])(c[11],c[46],(0,c[7])())),(0,c[57])(),(0,c[86])((0,c[50])(c[80],c[69]),c[Math.pow(4,1)%411+9],c[75],c[20],(0,c[84])())}catch(d){3<c[25]&&(1>=c[55]||((0,c[86])((0,c[49])(c[79],c[75]),c[64],c[48],c[43]),void 0))&&(0,c[86])((0,c[49])(c[12],c[Math.pow(8,4)+112+-4197]),c[17],c[9-Math.pow(7,1)+35],c[43]),(3<c[89]||((0,c[67])((0,c[86])((0,c[17])(c[62],c[3]),\nc[50],c[43],c[77]),c[50],(0,c[50])(c[60],c[68]),c[80],c[9]),0))&&(0,c[6])((0,c[64])(c[27],c[Math.pow(7,2)+34251+-34240]),(0,c[64])(c[15],c[43]),c[86],(0,c[52])(c[36],c[75]),c[42],c[11]),6>=c[40]&&(4<c[89]&&((0,c[67])((0,c[42])(c[60]),c[52],(0,c[50])(c[11],c[70]),c[85],c[3]),/(})/)||(0,c[67])((0,c[52])(c[18],c[43]),c[49],(0,c[44])(c[80]),c[66],c[new Date(\"1969-12-31T20:00:53.000-04:00\")/1E3])),9>=c[81]&&(5<c[80]?(0,c[53])(c[54],c[25]):(0,c[55])(c[30],c[76])),2<c[new Date(\"01 January 1970 00:01:21 UTC\")/\n1E3]&&(0,c[38])((0,c[77])((0,c[41])(c[17],c[62]),c[38],(0,c[61])(c[94]),c[56],(0,c[19])((0,c[56])(c[64],c[5]),c[70],c[27],c[19]),c[68],c[22]),c[18],(0,c[15])((0,c[18])(c[74],c[-390- -56*Math.pow(7,1)]),c[33],(0,c[32])(c[22],c[16]),c[16-Math.pow(3,5)%14],c[39]),c[51],c[41])}finally{2<c[60]&&(8>=c[29]||((0,c[34])(c[24],c[6]),\"\"))&&(0,c[35])(c[79],c[41])}try{5<c[29]&&((0,c[79])((0,c[26])(c[65]),c[615-68*Math.pow(3,2)],c[11],c[91]),c[92])(c[73],c[6]),(0,c[new Date(\"December 31 1969 13:45:41 -1015\")/1E3])(c[31],\nc[79],(0,c[66])()),(0,c[78])(c[31],c[26]),(0,c[78])(c[88],c[62])}catch(d){}try{2<c[67]&&(0,c[34])((0,c[17])((0,c[70])(c[6]),c[70],c[25110+Math.pow(new Date(\"01/01/1970 00:00:07 GMT\")/1E3,4)-27505]),(0,c[17])((0,c[80])(c[74],c[71]),c[78],c[6],c[50]),c[17],(0,c[80])(c[44],c[88]),c[-1036+Math.pow(8,1)+1100],c[88]),(2<c[new Date(\"December 31 1969 20:00:53 EDT\")/1E3]||((0,c[17])((0,c[72])(c[6]),c[78],c[new Date(\"1969-12-31T19:01:28.000-05:00\")/1E3],c[38]),0))&&(0,c[17])((0,c[41])(c[39],c[89],(0,c[1])()),\nc[72],c[6]),(0,c[85])(c[11]),(0,c[41])(c[39],c[28],(0,c[66])()),(0,c[78])(c[Math.pow(6,2)+122+-119],c[new Date(\"Wednesday December 31 1969 19:00:14 CDT\")/1E3])}catch(d){(0<c[0]||(((0,c[80])(c[60],c[88]),c[80])(c[21],c[88]),0))&&(0,c[17])((0,c[70])(c[6]),c[78],c[88],c[12])}try{2<c[91]&&(1<c[53]?(0,c[17])((0,c[80])(c[82],c[6]),c[78],c[39],c[5]):((0,c[45])(c[49],c[88]),c[78])(c[88],c[86])),(0,c[17])((0,c[85])(c[31]),c[41],c[88],c[75],(0,c[35])())}catch(d){}}catch(d){return\"enhanced_except_lpYB6en-_w8_\"+\na}return b.join(\"\")};";

        /*
        try {
            return parseWithLexer(playerJsCode, functionName);
        } catch (final Exception e) {
            return parseWithRegex(playerJsCode, functionName);
        }*/
    }

    @Nonnull
    private static String parseWithParenthesisMatching(final String playerJsCode,
                                                       final String functionName) {
        final String functionBase = functionName + "=function";
        return functionBase + StringUtils.matchToClosingParenthesis(playerJsCode, functionBase)
                + ";";
    }

    @Nonnull
    private static String parseWithRegex(final String playerJsCode, final String functionName)
            throws Parser.RegexException {
        final Pattern functionPattern = Pattern.compile(functionName + "=function(.*?}};)\n",
                Pattern.DOTALL);
        return "function " + functionName + Parser.matchGroup1(functionPattern, playerJsCode);
    }

    @Nonnull
    private static String parseWithLexer(final String playerJsCode, final String functionName) throws ParsingException, IOException {
        final String functionBase = functionName + "=function";
        return functionBase + JavaScriptExtractor.extractFunction(playerJsCode, functionBase)
                + ";";
    }

    @Deprecated
    public String apply(final String url) throws ParsingException {
        if (containsNParam(url)) {
            final String oldNParam = parseNParam(url);
            final String newNParam = decryptNParam(function, functionName, oldNParam);
            return replaceNParam(url, oldNParam, newNParam);
        } else {
            return url;
        }
    }

    private static boolean containsNParam(final String url) {
        return Parser.isMatch(N_PARAM_PATTERN, url);
    }

    private static String parseNParam(final String url) throws Parser.RegexException {
        return Parser.matchGroup1(N_PARAM_PATTERN, url);
    }

    private static String decryptNParam(final String function,
                                        final String functionName,
                                        final String nParam) {
        if (N_PARAMS_CACHE.containsKey(nParam)) {
            return N_PARAMS_CACHE.get(nParam);
        }
        final String decryptedNParam = JavaScript.run(function, functionName, nParam);
        N_PARAMS_CACHE.put(nParam, decryptedNParam);
        return decryptedNParam;
    }

    @Nonnull
    private static String replaceNParam(@Nonnull final String url,
                                        final String oldValue,
                                        final String newValue) {
        return url.replace(oldValue, newValue);
    }

    /**
     * @return the number of the cached "n" query parameters.
     */
    public static int getCacheSize() {
        return N_PARAMS_CACHE.size();
    }

    /**
     * Clears all stored "n" query parameters.
     */
    public static void clearCache() {
        N_PARAMS_CACHE.clear();
    }
}
