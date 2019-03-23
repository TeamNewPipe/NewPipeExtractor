package org.schabi.newpipe.extractor.utils;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

public class Base64Utils {
    public static String encodeBase64String(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes), Charsets.UTF_8);
    }
}
