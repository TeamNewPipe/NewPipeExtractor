package org.schabi.newpipe.extractor.services.youtube.retrofit.service;

import org.schabi.newpipe.extractor.services.youtube.retrofit.model.YoutubeMusicCheckBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface YoutubeMusicRetrofitService {
    String URL = "https://music.youtube.com/";
    String HARDCODED_KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30";
    String HARDCODED_CLIENT_VERSION = "1.20220808.01.00";

    @Headers({
            "X-YouTube-Client-Name: 67",
            "X-YouTube-Client-Version: " + HARDCODED_CLIENT_VERSION,
            "Origin: " + URL,
            "Referer: music.youtube.com"
    })
    @POST("/youtubei/v1/music/get_search_suggestions?alt=json&prettyPrint=false"
            + "&key=" + HARDCODED_KEY)
    Call<ResponseBody> checkHardcodedClientAndKeyValidity(@Body YoutubeMusicCheckBody body);

    @Headers({"Origin: " + URL, "Referer: " + URL})
    @GET("/sw.js")
    Call<ResponseBody> getSwJs();

    @GET("/ucbcb=1")
    Call<ResponseBody> getSearchPage(@Header("Cookie") String cookie);
}
