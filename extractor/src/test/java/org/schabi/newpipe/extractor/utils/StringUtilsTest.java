package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.utils.StringUtils.matchToClosingParenthesis;

public class StringUtilsTest {

    @Test
    public void actualDecodeFunction__success() {
        String preNoise = "if(\"function\"===typeof b&&\"function\"===typeof c||\"function\"===typeof c&&\"function\"===typeof d)throw Error(\"It looks like you are passing several store enhancers to createStore(). This is not supported. Instead, compose them together to a single function.\");\"function\"===typeof b&&\"undefined\"===typeof c&&(c=b,b=void 0);if(\"undefined\"!==typeof c){if(\"function\"!==typeof c)throw Error(\"Expected the enhancer to be a function.\");return c(Dr)(a,b)}if(\"function\"!==typeof a)throw Error(\"Expected the reducer to be a function.\");\n" +
                "var l=a,m=b,n=[],p=n,q=!1;h({type:Cr});a={};var t=(a.dispatch=h,a.subscribe=f,a.getState=e,a.replaceReducer=function(u){if(\"function\"!==typeof u)throw Error(\"Expected the nextReducer to be a function.\");l=u;h({type:hha});return t},a[Er]=function(){var u={};\n" +
                "return u.subscribe=function(x){function y(){x.next&&x.next(e())}\n" +
                "if(\"object\"!==typeof x||null===x)throw new TypeError(\"Expected the observer to be an object.\");y();return{unsubscribe:f(y)}},u[Er]=function(){return this},u},a);\n" +
                "return t};\n" +
                "Fr=function(a){De.call(this,a,-1,iha)};\n" +
                "Gr=function(a){De.call(this,a)};\n" +
                "jha=function(a,b){for(;Jd(b);)switch(b.C){case 10:var c=Od(b);Ge(a,1,c);break;case 18:c=Od(b);Ge(a,2,c);break;case 26:c=Od(b);Ge(a,3,c);break;case 34:c=Od(b);Ge(a,4,c);break;case 40:c=Hd(b.i);Ge(a,5,c);break;default:if(!we(b))return a}return a};";
        String signature = "kha=function(a)";
        String body = "{var b=a.split(\"\"),c=[-1186681497,-1653318181,372630254,function(d,e){for(var f=64,h=[];++f-h.length-32;){switch(f){case 58:f-=14;case 91:case 92:case 93:continue;case 123:f=47;case 94:case 95:case 96:continue;case 46:f=95}h.push(String.fromCharCode(f))}d.forEach(function(l,m,n){this.push(n[m]=h[(h.indexOf(l)-h.indexOf(this[m])+m-32+f--)%h.length])},e.split(\"\"))},\n" +
                "-467738125,1158037010,function(d,e){e=(e%d.length+d.length)%d.length;var f=d[0];d[0]=d[e];d[e]=f},\n" +
                "\"continue\",158531598,-172776392,function(d,e){e=(e%d.length+d.length)%d.length;d.splice(-e).reverse().forEach(function(f){d.unshift(f)})},\n" +
                "-1753359936,function(d){for(var e=d.length;e;)d.push(d.splice(--e,1)[0])},\n" +
                "1533713399,-1736576025,-1274201783,function(d){d.reverse()},\n" +
                "169126570,1077517431,function(d,e){d.push(e)},\n" +
                "-1807932259,-150219E3,480561184,-3495188,-1856307605,1416497372,b,-1034568435,-501230371,1979778585,null,b,-1049521459,function(d,e){e=(e%d.length+d.length)%d.length;d.splice(0,1,d.splice(e,1,d[0])[0])},\n" +
                "1119056651,function(d,e){for(e=(e%d.length+d.length)%d.length;e--;)d.unshift(d.pop())},\n" +
                "b,1460920438,135616752,-1807932259,-815823682,-387465417,1979778585,113585E4,function(d,e){d.push(e)},\n" +
                "-1753359936,-241651400,-386043301,-144139513,null,null,function(d,e){e=(e%d.length+d.length)%d.length;d.splice(e,1)}];\n" +
                "c[30]=c;c[49]=c;c[50]=c;try{c[51](c[26],c[25]),c[10](c[30],c[17]),c[5](c[28],c[9]),c[18](c[51]),c[14](c[19],c[21]),c[8](c[40],c[22]),c[50](c[35],c[28]),c[24](c[29],c[3]),c[0](c[31],c[19]),c[27](c[26],c[33]),c[29](c[36],c[40]),c[50](c[26]),c[27](c[32],c[9]),c[8](c[10],c[14]),c[35](c[44],c[28]),c[22](c[44],c[1]),c[8](c[11],c[3]),c[29](c[44]),c[21](c[41],c[45]),c[16](c[32],c[4]),c[17](c[14],c[26]),c[36](c[20],c[45]),c[43](c[35],c[39]),c[43](c[20],c[23]),c[43](c[10],c[51]),c[43](c[34],c[32]),c[29](c[34],\n" +
                "c[49]),c[43](c[20],c[44]),c[49](c[20]),c[19](c[15],c[8]),c[36](c[15],c[46]),c[17](c[20],c[37]),c[18](c[10]),c[17](c[34],c[31]),c[19](c[10],c[30]),c[19](c[20],c[2]),c[36](c[20],c[21]),c[43](c[35],c[16]),c[19](c[35],c[5]),c[18](c[46],c[34])}catch(d){return\"enhanced_except_lJMB6-z-_w8_\"+a}return b.join(\"\")}";
        String postNoise = "Hr=function(a){this.i=a}";

        String substring = matchToClosingParenthesis(preNoise + '\n' + signature + body + ";" + postNoise, signature);

        assertEquals(body, substring);
    }

    @Test
    public void moreClosing__success() {
        String expected = "{{{}}}";
        String string = "a" + expected + "}}";

        String substring = matchToClosingParenthesis(string, "a");

        assertEquals(expected, substring);
    }

    @Disabled("Functionality currently not needed")
    @Test
    public void lessClosing__success() {
        String expected = "{{{}}}";
        String string = "a{{" + expected;

        String substring = matchToClosingParenthesis(string, "a");

        assertEquals(expected, substring);
    }

    @Test
    void find_closing_with_quotes() {
        final String expected = "{return \",}\\\"/\"}";
        final String string = "function(d){return \",}\\\"/\"}";

        final String substring = matchToClosingParenthesis(string, "function(d)");

        assertEquals(expected, substring);
    }
}