package com.twitter.test3d

import android.util.Log
import com.twitter.test3d.network.GlbApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.Subject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class ModelViewModel(
    private val glbRelay: Subject<List<ByteBuffer>>,
    names: List<String>
) {

    private val glbApi: GlbApi = Retrofit.Builder()
        .baseUrl("https://models.babylonjs.com/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS).build()
        )
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create()).build()
        .create(GlbApi::class.java)

    private var compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            Observable.fromIterable(names)
                .flatMapSingle { name -> loadGlb(name) }
                .toList()
                .onErrorReturn {
                    Log.i("jared", it.stackTraceToString())
                    emptyList()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { modelBytesList ->
                    glbRelay.onNext(modelBytesList)
                }
        )
    }

    private fun loadGlb(name: String): Single<ByteBuffer> = glbApi.downloadFile(name)
        .map { it.body() ?: throw java.lang.IllegalArgumentException("Null body") }
        .map { response ->
            ByteBuffer.wrap(
                response?.bytes() ?: throw IllegalArgumentException("Null bytes repsonse")
            )
        }
        .subscribeOn(Schedulers.io())

    fun clear() {
        compositeDisposable.clear()
    }
}