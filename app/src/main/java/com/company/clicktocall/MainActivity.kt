package com.company.clicktocall

import android.app.Activity
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Create the main layout container
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            setBackgroundColor(Color.parseColor("#FFFFFF")) 
        }

        // 2. IP Address Text (Top)
        val ipAddress = getWifiIPAddress()
        val statusText = TextView(this).apply {
            text = "Click to Call is Running!\n\nChrome IP Address:\n\n$ipAddress\n\nYou can safely minimize this app."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#333333"))
        }

        // 3. Logo Image (Middle)
        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.logo) 
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 80, 0, 20) // Spacing above and below the logo
            }
            layoutParams = params
        }

        // 4. Tagline Text (Bottom)
        val poweredByText = TextView(this).apply {
            text = "This app is powered by\nNirdesh Technology PVT LTD"
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#555555"))
        }

        // Add them in the exact order requested
        mainLayout.addView(statusText)
        mainLayout.addView(logoImage)
        mainLayout.addView(poweredByText)

        setContentView(mainLayout)

        // Start the background engine so it works when minimized
        val serviceIntent = Intent(this, CallServerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun getWifiIPAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format("%d.%d.%d.%d", (ipAddress and 0xff), (ipAddress shr 8 and 0xff), (ipAddress shr 16 and 0xff), (ipAddress shr 24 and 0xff))
    }
}
