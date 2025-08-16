package com.example.stockapp.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StockServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: StockService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(StockService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun listTickers_returnsTickers() {
        val body = "[\"AAPL\",\"GOOG\"]"
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val response = service.listTickers().execute()
        assertTrue(response.isSuccessful)
        assertEquals(listOf("AAPL", "GOOG"), response.body())
    }

    @Test
    fun getPrice_returnsPrice() {
        server.enqueue(MockResponse().setBody("123.45").setResponseCode(200))

        val response = service.getPrice("AAPL").execute()
        assertTrue(response.isSuccessful)
        assertEquals(123.45, response.body(), 0.0)
    }
}
