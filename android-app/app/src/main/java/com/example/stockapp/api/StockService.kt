package com.example.stockapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface StockService {
    @GET("stock")
    fun listTickers(): Call<List<String>>

    @GET("price/{ticker}")
    fun getPrice(@Path("ticker") ticker: String): Call<Double>
}
