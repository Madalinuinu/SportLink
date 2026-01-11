package com.example.sportlink.di

import com.example.sportlink.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies.
 * Provides Retrofit instance and OkHttpClient for API calls.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * Provides OkHttpClient with logging interceptor for debugging.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Interceptor to disable cache for lobby requests to ensure fresh data
        val cacheControlInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            val newRequest = request.newBuilder()
                // Add cache control headers to prevent stale data
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build()
            chain.proceed(newRequest)
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(cacheControlInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)  // Optimized: reduced from 30s for faster error detection
            .readTimeout(15, TimeUnit.SECONDS)     // Optimized: reduced from 30s for faster response
            .writeTimeout(15, TimeUnit.SECONDS)    // Optimized: reduced from 30s for faster uploads
            .build()
    }
    
    /**
     * Provides GsonConverterFactory for JSON parsing.
     */
    @Provides
    @Singleton
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }
    
    /**
     * Provides Retrofit instance configured with base URL and converters.
     * This is the main Retrofit instance used for all API calls.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }
}

