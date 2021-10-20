package com.twitter.test3d.network

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GlbApi {
    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String): Single<Response<ResponseBody>>
}