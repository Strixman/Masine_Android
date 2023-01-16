package com.example.masine

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.masine.databinding.ActivityMainBinding
import com.example.masine.scripts.Application
import com.example.masine.ui.main.MainFragment
import com.example.masine.ui.main.SettingsFragment
import com.example.masine.ui.main.SimulationFragment
import com.example.masine.ui.main.VehicleFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding;
    private lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as Application;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.statusBarColor = Color.BLACK;

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(MainFragment())

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> replaceFragment(MainFragment())
                R.id.navigation_vehicle -> replaceFragment(VehicleFragment())
                R.id.navigation_simulations -> replaceFragment(SimulationFragment())
                R.id.navigation_settings -> replaceFragment(SettingsFragment())
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
            true
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        app.onDestroy();
        super.onDestroy()
    }
}

