package com.example.stockapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockapp.api.RetrofitClient
import com.example.stockapp.api.StockService
import kotlinx.coroutines.launch

class StockViewModel : ViewModel() {
    private val service: StockService = RetrofitClient.instance.create(StockService::class.java)

    private val _items = MutableLiveData<List<String>>()
    val items: LiveData<List<String>> = _items

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadData() {
        viewModelScope.launch {
            try {
                val tickers = service.listTickers()
                val results = mutableListOf<String>()
                for (ticker in tickers) {
                    try {
                        val price = service.getPrice(ticker)
                        results.add("$ticker: $price")
                    } catch (e: Exception) {
                        results.add("$ticker: error")
                    }
                }
                _items.value = results
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load tickers. Please try again."
            }
        }
    }

    fun retry() {
        loadData()
    }
}
