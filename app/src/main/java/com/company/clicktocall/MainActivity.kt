package com.company.clicktocall

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private var serverSocket: ServerSocket? = null
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        statusTextView = TextView(this).apply {
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }
        setContentView(statusTextView)

        val ipAddress = getWifiIPAddress()
        statusTextView.text = "Click to Call Status: RUNNING\n\nEnter this IP address in Chrome Extension settings:\n\n$ipAddress\n\n(Keep your phone connected to the same Wi-Fi network)"

        startHttpServer()
    }

    private fun startHttpServer() {
        thread {
            try {
                serverSocket = ServerSocket(8080)
                while (true) {
                    val socket = serverSocket?.accept() ?: break
                    handleClientSocket(socket)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClientSocket(socket: Socket) {
        thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                val firstLine = reader.readLine() ?: ""
                
                if (firstLine.contains("/dial?number=")) {
                    val number = firstLine.substringAfter("/dial?number=").substringBefore(" ")
                    
                    runOnUiThread {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$number")
                        }
                        startActivity(intent)
                    }
                }

                val output = socket.getOutputStream()
                output.write("HTTP/1.1 200 OK\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: 2\r\n\r\nOK".toByteArray())
                output.flush()
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getWifiIPAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format("%d.%d.%d.%d", (ipAddress and 0xff), (ipAddress shr 8 and 0xff), (ipAddress shr 16 and 0xff), (ipAddress shr 24 and 0xff))
    }

    override fun onDestroy() {
        super.onDestroy()
        try { serverSocket?.close() } catch (e: Exception) {}
    }
}