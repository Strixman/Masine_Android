package com.example.masine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.masine.databinding.ActivityMainBinding
import com.example.masine.scripts.Application

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding;
    private lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as Application;

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        app.onDestroy();
        super.onDestroy()
    }
}

