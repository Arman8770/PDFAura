package com.coderhymes.pdfaura.screens

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.coderhymes.pdfaura.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeScreen : AppCompatActivity() {
    private lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homescreen)
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // Get the NavHostFragment using the ID of the fragment
        // Get the NavHostFragment using the ID of the fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Access the NavController
        val navController = navHostFragment.navController

        // Set up the BottomNavigationView with the NavController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        // For changing the navigation bar color
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
//        }

// For changing the navigation bar icon color
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val flags = window.decorView.systemUiVisibility
//            window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//        }

        val button = findViewById<ImageView>(R.id.menuClose)
        button.setOnClickListener { v: View ->
            showMenu(v, R.menu.overflow_menu)
        }
    }

    private fun showMenu(v: View, overflowMenu: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(overflowMenu, popup.menu)

//        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
//            // Respond to menu item click.
//        }
//        popup.setOnDismissListener {
//            // Respond to popup being dismissed.
//        }
        // Show the popup menu.
        popup.show()
    }


}