package com.company.clicktocall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class CallServerService : Service() {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        
        // Create a notification to keep the service alive in the background
        val channelId = "ClickToCallChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Click to Call Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Click to Call is Active")
                .setContentText("Listening for numbers from Chrome...")
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Click to Call is Active")
                .setContentText("Listening for numbers from Chrome...")
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .build()
        }
            
        startForeground(1, notification)
        startServer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startServer() {
        isRunning = true
        thread {
            try {
                serverSocket = ServerSocket(8080)
                while (isRunning) {
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
                    
                    // Open the dialer even if the app is in the background
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$number")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(dialIntent)
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

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try { serverSocket?.close() } catch (e: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
