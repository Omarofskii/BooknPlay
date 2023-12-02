package com.example.project

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (getSupportActionBar() != null) {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.greenish_button)))

        }

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Customization for bottom navigation view
        with(bottomNavView) {
            // Change the background color if needed
            setBackgroundColor(ContextCompat.getColor(context, R.color.bottom_nav_background))

            // Customize the text appearance
            itemTextAppearanceActive = R.style.BottomNavigationView_Active
            itemTextAppearanceInactive = R.style.BottomNavigationView_Inactive

            // Set the listener for item selection
            setOnNavigationItemSelectedListener { item ->
                var selectedFragment: Fragment? = null
                when (item.itemId) {
                    R.id.navigation_play -> selectedFragment = PlayFragment()
                    R.id.navigation_profile -> selectedFragment = ProfileFragment()
                }
                selectedFragment?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, it)
                        .commit()
                }
                true
            }

            // Set default selection
            selectedItemId = R.id.navigation_play
        }
    }
}