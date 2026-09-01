package com.clmf.player.di

import com.clmf.player.BuildConfig
import com.clmf.player.data.remote.api.XtreamApi
import com.clmf.player.data.remote.interceptor.CredentialRedactingInterceptor
import com.clmf.player.utils.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(CredentialRedactingInterceptor())

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor { message ->
                AppLogger.debug(CredentialRedactingInterceptor.redactUrl(message), tag = "OkHttp")
            }.apply { level = HttpLoggingInterceptor.Level.BASIC }
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            // Base URL is a placeholder: every real call supplies a full @Url per connection.
            .baseUrl("https://clmf.local/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideXtreamApi(retrofit: Retrofit): XtreamApi = retrofit.create(XtreamApi::class.java)
}
