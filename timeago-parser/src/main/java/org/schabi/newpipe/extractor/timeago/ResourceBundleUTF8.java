package org.schabi.newpipe.extractor.timeago;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Handy class to get normal UTF-8 strings from the resource bundles.
 */
public class ResourceBundleUTF8 {
    private static final UTF8Control utf8Control = new UTF8Control();

    /**
     * @see ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getBundle(String baseName) {
        return ResourceBundle.getBundle(baseName, utf8Control);
    }

    /**
     * @see ResourceBundle#getBundle(String, Locale)
     */
    public static ResourceBundle getBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName, locale, utf8Control);
    }

    /**
     * Class copied from the java source modified to work with UTF-8 strings.
     */
    public static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            // COPY FROM SOURCE

            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends ResourceBundle> bundleClass
                            = (Class<? extends ResourceBundle>) loader.loadClass(bundleName);

                    // If the class isn't a ResourceBundle subclass, throw a
                    // ClassCastException.
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = bundleClass.newInstance();
                    } else {
                        throw new ClassCastException(bundleClass.getName()
                                + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                }
            } else if (format.equals("java.properties")) {
                final String resourceName = bundleName.contains("://") ? null : toResourceName(bundleName, "properties");
                if (resourceName == null) {
                    return bundle;
                }
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream = null;
                try {
                    stream = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<InputStream>() {
                                public InputStream run() throws IOException {
                                    InputStream is = null;
                                    if (reloadFlag) {
                                        URL url = classLoader.getResource(resourceName);
                                        if (url != null) {
                                            URLConnection connection = url.openConnection();
                                            if (connection != null) {
                                                // Disable caches to get fresh data for
                                                // reloading.
                                                connection.setUseCaches(false);
                                                is = connection.getInputStream();
                                            }
                                        }
                                    } else {
                                        is = classLoader.getResourceAsStream(resourceName);
                                    }
                                    return is;
                                }
                            });
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                if (stream != null) {
                    try {
                        ////// Line changed to support UTF-8
                        bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                    } finally {
                        stream.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("unknown format: " + format);
            }
            return bundle;
        }
    }
}
