package com.example.stockapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var retryButton: Button
    private val viewModel: StockViewModel by viewModels()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView)
        retryButton = findViewById(R.id.retryButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        retryButton.setOnClickListener { viewModel.retry() }

        viewModel.items.observe(this) { items ->
            adapter.clear()
            adapter.addAll(items)
            adapter.notifyDataSetChanged()
        }

        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.loadData()
    }
}
