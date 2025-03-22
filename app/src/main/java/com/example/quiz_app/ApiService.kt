package com.example.quiz_app

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("/api/trivia")
    suspend fun getTriviaQuestion(): TriviaQuestion

    companion object {
        private const val BASE_URL = "http://192.168.0.102:5000"  // Replace with your machine's IP

        fun create(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}

data class TriviaQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)