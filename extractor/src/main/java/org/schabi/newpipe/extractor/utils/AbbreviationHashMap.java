package org.schabi.newpipe.extractor.utils;

import java.util.HashMap;


/**
 * Map matching abbreviations with their English equivalents
 *
 * @author B0pol
 * <p>
 * By using this map, you can replace the abbreviations used for numbers in the 80 languages supported by YouTube
 * With their English equivalent.
 * </p>
 * <p>
 * Some language use more abbreviations for numbers: east-asian languages have abbreviations for ten thousand,
 * and hundred million, indo-arabic languages have abbreviations for a hundred thousand and ten million,
 * then we replace ten thousand by {@link #tenThousandAbbreviation},
 * hundred thousand by {@link #hundredThousandAbbreviation},
 * ten million by {@link #tenMillionAbbreviation},
 * hundred million by {@link #hundredMillionAbbreviation}.
 * </p>
 * <p>
 * The languages using the abbreviation is commented with the language code at the left.
 * @see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">Wikipedia page of language codes</a>
 * </p>
 */
public class AbbreviationHashMap {

    //should be safe until someone has 1 billion subscribers on YouTube
    public static final HashMap<String, String> abbreviationSubscribersCount = new HashMap<>();
    public static final String englishMillionAbbreviation = "M";
    public static final String englishThousandAbbreviation = "K";

    public static final String tenThousandAbbreviation = "万";
    public static final String hundredThousandAbbreviation = "ল";
    public static final String tenMillionAbbreviation = "ক";
    public static final String hundredMillionAbbreviation = "億";

    static {
        abbreviationSubscribersCount.put(englishThousandAbbreviation, englishThousandAbbreviation); //az, iw, en
        abbreviationSubscribersCount.put(englishMillionAbbreviation, englishMillionAbbreviation); //iw, en
        abbreviationSubscribersCount.put(tenMillionAbbreviation, tenMillionAbbreviation);

        abbreviationSubscribersCount.put(" k", englishThousandAbbreviation); //af
        abbreviationSubscribersCount.put(" ሺ", englishThousandAbbreviation); //am
        abbreviationSubscribersCount.put(" ألف", englishThousandAbbreviation); //ar
        abbreviationSubscribersCount.put(" тыс", englishThousandAbbreviation); //be, ru
        abbreviationSubscribersCount.put(" хил", englishThousandAbbreviation); //bg
        abbreviationSubscribersCount.put(" হা", englishThousandAbbreviation); //bn
        abbreviationSubscribersCount.put(" hilj", englishThousandAbbreviation); //bs, sr
        abbreviationSubscribersCount.put("m", englishThousandAbbreviation); //ca
        abbreviationSubscribersCount.put(" tis", englishThousandAbbreviation); //cs, hr, sk, sl
        abbreviationSubscribersCount.put(" χιλ", englishThousandAbbreviation); //el
        abbreviationSubscribersCount.put(" tuh", englishThousandAbbreviation); //et
        abbreviationSubscribersCount.put(" هزار", englishThousandAbbreviation); //fa
        abbreviationSubscribersCount.put(" t", englishThousandAbbreviation); //fi
        abbreviationSubscribersCount.put(" હજાર", englishThousandAbbreviation); //gu
        abbreviationSubscribersCount.put(" हज़ार", englishThousandAbbreviation); //hi
        abbreviationSubscribersCount.put(" E", englishThousandAbbreviation); //hu
        abbreviationSubscribersCount.put(" հզր", englishThousandAbbreviation); //hy
        abbreviationSubscribersCount.put(" rb", englishThousandAbbreviation); //id
        abbreviationSubscribersCount.put(" þ", englishThousandAbbreviation); //is
        abbreviationSubscribersCount.put(" ათ", englishThousandAbbreviation); //ka
        abbreviationSubscribersCount.put(" мың", englishThousandAbbreviation); //kk
        abbreviationSubscribersCount.put("ពាន់", englishThousandAbbreviation); //km
        abbreviationSubscribersCount.put(" ពាន់", englishThousandAbbreviation); //km
        abbreviationSubscribersCount.put("ಸಾ", englishThousandAbbreviation); //kn
        abbreviationSubscribersCount.put("천", englishThousandAbbreviation); //ko
        abbreviationSubscribersCount.put(" миң", englishThousandAbbreviation); //ky
        abbreviationSubscribersCount.put(" ກີບ", englishThousandAbbreviation); //lo
        abbreviationSubscribersCount.put(" ພັນ", englishThousandAbbreviation); //lo
        abbreviationSubscribersCount.put(" tūkst", englishThousandAbbreviation); //lt, lv
        abbreviationSubscribersCount.put(" илј", englishThousandAbbreviation); //mk
        abbreviationSubscribersCount.put(" мянга", englishThousandAbbreviation); //mn
        abbreviationSubscribersCount.put(" ह", englishThousandAbbreviation); //mr
        abbreviationSubscribersCount.put("ထောင်", englishThousandAbbreviation); //my
        abbreviationSubscribersCount.put(" हजार", englishThousandAbbreviation); //ne
        abbreviationSubscribersCount.put("k", englishThousandAbbreviation); //no
        abbreviationSubscribersCount.put(" ਹਜ਼ਾਰ", englishThousandAbbreviation); //pa
        abbreviationSubscribersCount.put(" tys", englishThousandAbbreviation); //pl
        abbreviationSubscribersCount.put(" K", englishThousandAbbreviation); //ro
        abbreviationSubscribersCount.put("ද", englishThousandAbbreviation); //si
        abbreviationSubscribersCount.put(" mijë", englishThousandAbbreviation); //sq
        abbreviationSubscribersCount.put(" хиљ", englishThousandAbbreviation); //sr-Latn
        abbreviationSubscribersCount.put("elfu ", englishThousandAbbreviation); //sw
        abbreviationSubscribersCount.put("ஆ", englishThousandAbbreviation); //ta
        abbreviationSubscribersCount.put("వే", englishThousandAbbreviation); //te
        abbreviationSubscribersCount.put(" พัน", englishThousandAbbreviation); //th
        abbreviationSubscribersCount.put(" B", englishThousandAbbreviation); //tr
        abbreviationSubscribersCount.put(" тис", englishThousandAbbreviation); //uk
        abbreviationSubscribersCount.put(" ہزار", englishThousandAbbreviation); //ur
        abbreviationSubscribersCount.put(" ming", englishThousandAbbreviation); //uz
        abbreviationSubscribersCount.put(" N", englishThousandAbbreviation); //vi

        abbreviationSubscribersCount.put(" m", englishMillionAbbreviation); //af, is
        abbreviationSubscribersCount.put(" م", englishMillionAbbreviation); //an
        abbreviationSubscribersCount.put("ሜ", englishMillionAbbreviation); //am
        abbreviationSubscribersCount.put(" ሜትር", englishMillionAbbreviation); //am
        abbreviationSubscribersCount.put(" مليون", englishMillionAbbreviation); //ar
        abbreviationSubscribersCount.put(" mln", englishMillionAbbreviation); //az, et, lt, nl, pl, sq, uz
        abbreviationSubscribersCount.put(" млн", englishMillionAbbreviation); //be, bg, kk, ky, ru, uk
        abbreviationSubscribersCount.put(" mil", englishMillionAbbreviation); //bs, cs, hr, ro, sk, sr-Latn
        abbreviationSubscribersCount.put(" M", englishMillionAbbreviation); //ca, es, eu, and many more
        abbreviationSubscribersCount.put(" mio", englishMillionAbbreviation); //da, sl
        abbreviationSubscribersCount.put(" Mio", englishMillionAbbreviation); //de
        abbreviationSubscribersCount.put(" εκ", englishMillionAbbreviation); //el
        abbreviationSubscribersCount.put(" میلیون", englishMillionAbbreviation); //fa
        abbreviationSubscribersCount.put(" milj", englishMillionAbbreviation); //fi, lv
        abbreviationSubscribersCount.put(" մլն", englishMillionAbbreviation); //hy
        abbreviationSubscribersCount.put(" jt", englishMillionAbbreviation); //id
        abbreviationSubscribersCount.put(" Mln", englishMillionAbbreviation); //it
        abbreviationSubscribersCount.put(" მლნ", englishMillionAbbreviation); //ka
        abbreviationSubscribersCount.put(" លាន", englishMillionAbbreviation); //km
        abbreviationSubscribersCount.put("ಮಿ", englishMillionAbbreviation); //kn
        abbreviationSubscribersCount.put(" ລ້ານ", englishMillionAbbreviation); //lo
        abbreviationSubscribersCount.put(" М", englishMillionAbbreviation); //mk. It isn't a space but a
        // narrow non-breaking space, ie U+202F
        abbreviationSubscribersCount.put(" мил", englishMillionAbbreviation); //mk, sr
        abbreviationSubscribersCount.put(" сая", englishMillionAbbreviation); //mn
        abbreviationSubscribersCount.put("သန်း", englishMillionAbbreviation); //my
        abbreviationSubscribersCount.put(" mill", englishMillionAbbreviation); //no
        abbreviationSubscribersCount.put(" mi", englishMillionAbbreviation); //pt
        abbreviationSubscribersCount.put("මි", englishMillionAbbreviation); //si
        abbreviationSubscribersCount.put(" mn", englishMillionAbbreviation); //sv
        abbreviationSubscribersCount.put("మి", englishMillionAbbreviation); //te
        abbreviationSubscribersCount.put("மி", englishMillionAbbreviation); //ta
        abbreviationSubscribersCount.put(" ล้าน", englishMillionAbbreviation); //th
        abbreviationSubscribersCount.put(" Mn", englishMillionAbbreviation); //tr
        abbreviationSubscribersCount.put(" Tr", englishMillionAbbreviation); //vi

        abbreviationSubscribersCount.put("만", tenThousandAbbreviation); //ko
        abbreviationSubscribersCount.put("万", tenThousandAbbreviation); //ja, zh-CN
        abbreviationSubscribersCount.put("萬", tenThousandAbbreviation); //zh-TW

        abbreviationSubscribersCount.put(" লা", hundredThousandAbbreviation); //bn
        abbreviationSubscribersCount.put(" લાખ", hundredThousandAbbreviation); //gu
        abbreviationSubscribersCount.put(" लाख", hundredThousandAbbreviation); //hi, mr, ne
        abbreviationSubscribersCount.put(" ਲੱਖ", hundredThousandAbbreviation); //pa
        abbreviationSubscribersCount.put(" لاکھ", hundredThousandAbbreviation); //ur
        abbreviationSubscribersCount.put("သိန်း", hundredThousandAbbreviation); //my
        abbreviationSubscribersCount.put(" แสน", hundredThousandAbbreviation); //th

        abbreviationSubscribersCount.put(" কো", tenMillionAbbreviation); //bn
        abbreviationSubscribersCount.put(" કરોડ", tenMillionAbbreviation); //gu
        abbreviationSubscribersCount.put(" क॰", tenMillionAbbreviation); //hi
        abbreviationSubscribersCount.put(" कोटी", tenMillionAbbreviation); //mr
        abbreviationSubscribersCount.put("ကုဋေ", tenMillionAbbreviation); //my
        abbreviationSubscribersCount.put(" करोड", tenMillionAbbreviation); //ne
        abbreviationSubscribersCount.put(" ਕਰੋੜ", tenMillionAbbreviation); //pa
        abbreviationSubscribersCount.put(" کروڑ", tenMillionAbbreviation); //ur

        abbreviationSubscribersCount.put("億", hundredMillionAbbreviation); //ja, zh-TW
        abbreviationSubscribersCount.put("억", hundredMillionAbbreviation); //ko
        abbreviationSubscribersCount.put("亿", hundredMillionAbbreviation); //zh-CN
    }
}
