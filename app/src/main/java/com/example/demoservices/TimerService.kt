package com.example.demoservices

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Timer
import kotlin.concurrent.schedule

class TimerService : Service() {
    companion object {
        private const val TAG = "ForegroundService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private var count: Int = 0
        private var checked = true
        private var timer: Timer? = null
        private lateinit var handler: Handler
        private var serviceLooper: Looper? = null
        private var serviceHandler: serviceHandler? = null
        private var ischeck = ""
    }

    private val binder: IBinder = CountingBinder()

    private inner class serviceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            // Increase the count
            count++

            if(checked == true){
                if (ischeck == "foreground") {
                    updateNotification(count)
                    if (count >= 100) {
                        checked = false
                        stopSelf(msg.arg1)
                    } else {
                        sendEmptyMessageDelayed(0, 1000)
                        Log.d(TAG, "$count")
                    }
                }
                if (ischeck == "Back_ground") {
                    if (count == 100) {

                        updateNotification(count)
                        Toast.makeText(
                            applicationContext,
                            "Complete Count = $count",
                            Toast.LENGTH_SHORT
                        ).show()
                        stopSelf(msg.arg1)
                    } else {
                        sendEmptyMessageDelayed(0, 1000)
                        Log.d(TAG, "$count")

                    }
                }
            }

            // Update the notification with the new count

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
        }
    }

    inner class CountingBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun startCounting() {
        timer = Timer()
        // đặt lịch lặp lại 1s
        timer?.schedule(0, 1000) {
            count++
            // gửi  count
            sendCountBroadcast(count)

            if (count >= 100) {
                timer?.cancel()
            }
        }
    }

    fun stopCounting() {
        timer?.cancel()
        // stop foreground
        stopForeground(false)
        // use removeMessages to remove messages
        serviceHandler?.removeMessages(0)
        Log.d(TAG, "stopCounting")
        stopSelf()
    }

    private fun sendCountBroadcast(count: Int) {
        val intent = Intent("COUNT_UPDATE")
        intent.putExtra("count", count)
        sendBroadcast(intent)
    }

    override fun onDestroy() {

        super.onDestroy()
        checked = false
        Log.d(TAG, "services destroyed")

    }


    override fun onCreate() {
        super.onCreate()
//        createNotificationChannel()

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = serviceHandler(looper)
        }
//        Log.d(TAG, "ForegroundService onCreate")
//
//        val overlayIntent = Intent(applicationContext, OverlayService::class.java)
//        startService(overlayIntent)
//        Log.d(TAG, "11 onCreate")

    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        ischeck = intent?.getStringExtra("dataKey").toString()
        if (ischeck != null) {
            Log.d(TAG, ischeck)
        }
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }
        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    private fun updateNotification(count: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hello Service")
            .setContentText("Count: $count")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        // create notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hello Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}