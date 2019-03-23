package org.schabi.newpipe.extractor.utils;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnull;

public class Base64Utils {
    /*
    Using Base64.encodeBase64String() throws a NoSuchMethodError when
    using it on Android. The reason behind this seems to be that
    Android already includes an older version of Apache Commons Codec
    which does not have this method, as per below SO thread:

    https://stackoverflow.com/questions/2047706/apache-commons-codec-with-android-could-not-find-method

    Hence, encodeBase64 method has been used which is an older method.
    Creating a String out of it is exactly what was being done in the
    library too, so that has been pulled in here.
     */
    public static String encodeBase64String(@Nonnull byte[] bytes) {
        return new String(Base64.encodeBase64(bytes), Charsets.UTF_8);
    }
}
