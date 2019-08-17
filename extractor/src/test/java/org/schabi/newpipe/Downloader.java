package org.schabi.newpipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.schabi.newpipe.extractor.DownloadRequest;
import org.schabi.newpipe.extractor.DownloadResponse;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.Localization;

/*
 * Created by Christian Schabesberger on 28.01.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * Downloader.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Downloader implements org.schabi.newpipe.extractor.Downloader {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
    private static String mCookies = "";

    private static Downloader instance = null;

    private Downloader() {
    }

    public static Downloader getInstance() {
        if (instance == null) {
            synchronized (Downloader.class) {
                if (instance == null) {
                    instance = new Downloader();
                }
            }
        }
        return instance;
    }

    public static synchronized void setCookies(String cookies) {
        Downloader.mCookies = cookies;
    }

    public static synchronized String getCookies() {
        return Downloader.mCookies;
    }

    /**
     * Download the text file at the supplied URL as in download(String), but set
     * the HTTP header field "Accept-Language" to the supplied string.
     *
     * @param siteUrl  the URL of the text file to return the contents of

     * @param localization the language and country (usually a 2-character code for both values)
     * @return the contents of the specified text file
     */
    public String download(String siteUrl, Localization localization) throws IOException, ReCaptchaException {
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Accept-Language", localization.getLanguage());
        return download(siteUrl, requestProperties);
    }

    /**
     * Download the text file at the supplied URL as in download(String), but set
     * the HTTP header field "Accept-Language" to the supplied string.
     *
     * @param siteUrl          the URL of the text file to return the contents of
     * @param customProperties set request header properties
     * @return the contents of the specified text file
     * @throws IOException
     */
    public String download(String siteUrl, Map<String, String> customProperties)
            throws IOException, ReCaptchaException {
        URL url = new URL(siteUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        for (Map.Entry<String, String> pair : customProperties.entrySet()) {
            con.setRequestProperty(pair.getKey(), pair.getValue());
        }
        return dl(con);
    }

    /**
     * Common functionality between download(String url) and download(String url,
     * String language)
     */
    private static String dl(HttpsURLConnection con) throws IOException, ReCaptchaException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = null;

        try {

            con.setRequestMethod("GET");
            setDefaults(con);

            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (UnknownHostException uhe) {// thrown when there's no internet
                                            // connection
            throw new IOException("unknown host or no network", uhe);
            // Toast.makeText(getActivity(), uhe.getMessage(),
            // Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            /*
             * HTTP 429 == Too Many Request Receive from Youtube.com = ReCaptcha challenge
             * request See : https://github.com/rg3/youtube-dl/issues/5138
             */
            if (con.getResponseCode() == 429) {
                throw new ReCaptchaException("reCaptcha Challenge requested", con.getURL().toString());
            }

            throw new IOException(con.getResponseCode() + " " + con.getResponseMessage(), e);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return response.toString();
    }

    private static void setDefaults(HttpsURLConnection con) {

        con.setConnectTimeout(30 * 1000);// 30s
        con.setReadTimeout(30 * 1000);// 30s

        // set default user agent
        if (null == con.getRequestProperty("User-Agent")) {
            con.setRequestProperty("User-Agent", USER_AGENT);
        }

        // add default cookies
        if (getCookies().length() > 0) {
            con.addRequestProperty("Cookie", getCookies());
        }
    }

    /**
     * Download (via HTTP) the text file located at the supplied URL, and return its
     * contents. Primarily intended for downloading web pages.
     *
     * @param siteUrl the URL of the text file to download
     * @return the contents of the specified text file
     */
    public String download(String siteUrl) throws IOException, ReCaptchaException {
        URL url = new URL(siteUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        // HttpsURLConnection con = NetCipher.getHttpsURLConnection(url);
        return dl(con);
    }

    @Override
    public DownloadResponse get(String siteUrl, DownloadRequest request)
            throws IOException, ReCaptchaException {
        URL url = new URL(siteUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        for (Map.Entry<String, List<String>> pair : request.getRequestHeaders().entrySet()) {
            for(String value: pair.getValue()) {
                con.addRequestProperty(pair.getKey(), value);
            }
        }
        String responseBody = dl(con);
        return new DownloadResponse(responseBody, con.getHeaderFields());
    }

    @Override
    public DownloadResponse get(String siteUrl) throws IOException, ReCaptchaException {
        return get(siteUrl, DownloadRequest.emptyRequest);
    }

    @Override
    public DownloadResponse post(String siteUrl, DownloadRequest request)
            throws IOException, ReCaptchaException {
        URL url = new URL(siteUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        for (Map.Entry<String, List<String>> pair : request.getRequestHeaders().entrySet()) {
            for(String value: pair.getValue()) {
                con.addRequestProperty(pair.getKey(), value);
            }
        }
        // set fields to default if not set already
        setDefaults(con);

        if(null != request.getRequestBody()) {
            byte[] postDataBytes = request.getRequestBody().getBytes("UTF-8");
            con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            con.setDoOutput(true);
            con.getOutputStream().write(postDataBytes);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
        }
        return new DownloadResponse(sb.toString(), con.getHeaderFields());
    }
}
