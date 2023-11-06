package io.lumstudio.yohub.common.net.factory

import com.google.gson.GsonBuilder
import io.lumstudio.yohub.common.net.converter.JsonConverterFactory
import io.lumstudio.yohub.common.net.interceptor.BusinessErrorInterceptor
import io.lumstudio.yohub.common.utils.DateDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * �ӿ����󹤳�
 */
object PrivacyApiFactory {

    // OkHttpClient�ͻ���
    private val mClient: OkHttpClient by lazy { newClient() }

    /**
     * ����API Service�ӿ�ʵ��
     */
    fun <T> createService(baseUrl: String, clazz: Class<T>): T =
        Retrofit.Builder().baseUrl(baseUrl).client(mClient)
            .addConverterFactory(
                JsonConverterFactory(
                    GsonBuilder()
                        .registerTypeAdapter(Date::class.java, DateDeserializer())
                        .create()
                )
            )
            .build().create(clazz)

    /**
     * OkHttpClient�ͻ���
     */
    private fun newClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)// ����ʱ�䣺30s��ʱ
        readTimeout(10, TimeUnit.SECONDS)// ��ȡʱ�䣺10s��ʱ
        writeTimeout(10, TimeUnit.SECONDS)// д��ʱ�䣺10s��ʱ
        addInterceptor(BusinessErrorInterceptor())
    }.build()
}