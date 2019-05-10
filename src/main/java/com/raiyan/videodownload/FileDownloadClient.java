package com.raiyan.videodownload;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileDownloadClient {

    @GET
    @Streaming
    Call<ResponseBody> downloadFileStream(@Url String url);

}
