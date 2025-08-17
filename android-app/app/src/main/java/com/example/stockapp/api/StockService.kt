package com.example.stockapp.api

import retrofit2.http.GET
import retrofit2.http.Path

interface StockService {
    @GET("stock")
    suspend fun listTickers(): List<String>

    @GET("price/{ticker}")
    suspend fun getPrice(@Path("ticker") ticker: String): Double
}
