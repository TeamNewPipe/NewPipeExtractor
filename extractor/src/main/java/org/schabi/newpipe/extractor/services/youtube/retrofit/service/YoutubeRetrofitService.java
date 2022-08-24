package org.schabi.newpipe.extractor.services.youtube.retrofit.service;

import org.schabi.newpipe.extractor.services.youtube.retrofit.model.YoutubeCheckBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface YoutubeRetrofitService {
    String URL = "https://www.youtube.com/";

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
    @POST("/youtubei/v1/guide?key=" + HARDCODED_KEY + "&prettyPrint=false")
    Call<ResponseBody> checkHardcodedClientAndKeyValidity(@Header("Accept-Language") String lang,
                                                          @Body YoutubeCheckBody body);

    @Headers({"Origin: " + URL, "Referer: " + URL})
    @GET("/sw.js")
    Call<ResponseBody> getSwJs(@Header("Accept-Language") String language);

    @GET("/results?search_query=&ucbcb=1")
    Call<ResponseBody> getSearchPage(@Header("Cookie") String cookie);
}
