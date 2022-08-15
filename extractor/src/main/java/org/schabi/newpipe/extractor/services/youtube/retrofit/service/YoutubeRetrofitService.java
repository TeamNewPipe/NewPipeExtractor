package org.schabi.newpipe.extractor.services.youtube.retrofit.service;

import org.schabi.newpipe.extractor.services.youtube.retrofit.model.ValidityCheckBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface YoutubeRetrofitService {
    String YOUTUBE_URL = "https://www.youtube.com";

    /**
     * The InnerTube API key which should be used by YouTube's desktop website, used as a fallback
     * if the extraction of the real one failed.
     */
    String HARDCODED_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";

    /**
     * The client version for InnerTube requests with the {@code WEB} client, used as the last
     * fallback if the extraction of the real one failed.
     */
    String HARDCODED_CLIENT_VERSION = "2.20220809.02.00";

    @Headers({"X-YouTube-Client-Name: 1", "X-YouTube-Client-Version: " + HARDCODED_CLIENT_VERSION})
    @POST("/guide?key=" + HARDCODED_KEY + "&prettyPrint=false")
    Call<String> checkHardcodedClientAndKeyValidity(@Body ValidityCheckBody body);

    @Headers({"Origin: " + YOUTUBE_URL, "Referer: " + YOUTUBE_URL})
    @GET("/sw.js")
    Call<ResponseBody> getSwJs();
}
