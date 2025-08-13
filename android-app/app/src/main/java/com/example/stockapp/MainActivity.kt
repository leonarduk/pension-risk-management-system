package com.example.stockapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.stockapp.api.RetrofitClient
import com.example.stockapp.api.StockService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private val service: StockService = RetrofitClient.instance.create(StockService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView)
        loadTickers()
    }

    private fun loadTickers() {
        service.listTickers().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val tickers = response.body() ?: emptyList()
                    fetchPrices(tickers)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // Handle error
            }
        })
    }

    private fun fetchPrices(tickers: List<String>) {
        val items = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
        for (ticker in tickers) {
            service.getPrice(ticker).enqueue(object : Callback<Double> {
                override fun onResponse(call: Call<Double>, response: Response<Double>) {
                    val price = response.body()
                    items.add("$ticker: ${'$'}price")
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<Double>, t: Throwable) {
                    items.add("$ticker: error")
                    adapter.notifyDataSetChanged()
                }
            })
        }
    }
}
