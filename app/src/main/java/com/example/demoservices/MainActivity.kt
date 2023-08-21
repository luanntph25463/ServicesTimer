package com.example.demoservices

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.background
import kotlinx.android.synthetic.main.activity_main.btnStart
import kotlinx.android.synthetic.main.activity_main.btnStop
import kotlinx.android.synthetic.main.activity_main.forgeground

class MainActivity : AppCompatActivity() {
    private lateinit var countTextView: TextView
    private lateinit var timerService: TimerService
    private  val TAG = "MainActivity"

    private var isBound = false
    private var foregournd = "foreground"
    private var Back_ground = "Back_ground"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        forgeground.setOnClickListener {
            forgeground.setBackgroundResource(R.drawable.button3)
            val intent = Intent(this, TimerService::class.java)
            intent.putExtra("dataKey", foregournd)
            startService(intent)
        }

        background.setOnClickListener {
            background.setBackgroundResource(R.drawable.button4)

            val intent = Intent(this, TimerService::class.java)
            intent.putExtra("dataKey", Back_ground)
            startService(intent)

        }


        countTextView = findViewById(R.id.tvElapsedTime)

        btnStart.setOnClickListener {
            startCounting()
        }

        btnStop.setOnClickListener {
            stopCounting()
        }
    }


    private val countReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            // xu ly khi nhan boardcast receiver
            val count = intent?.getIntExtra("count", 0)
            count?.let {
                updateCountTextView(it)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.CountingBinder
            timerService = binder.getService()

            isBound = true
            Log.d(TAG, "$isBound")
            Log.d(TAG, "$timerService")

        }

        // ghi đè ServiceConnection  disconnected
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }


    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        // bind auto_create connect if not create
        // khi connect service thanh cong se goi ham onServiceConnected
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d(TAG, "onconnect")

        // kh
        // đăng ký receiver
        registerReceiver(countReceiver, IntentFilter("COUNT_UPDATE"))
        Log.d("this", "$isBound")
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            // gọi unbindService của connection
            unbindService(connection)
            Log.d(TAG, "onStop")

            isBound = false
        }
        // hủy đăng ký từ boardcast receiver
        unregisterReceiver(countReceiver)
    }

    private fun startCounting() {
        Log.d(TAG, "$isBound")

        if (isBound) {
            timerService.startCounting()
            Log.d(TAG, "startCounting")

        }
    }

    private fun stopCounting() {
        if (isBound) {
            timerService.stopCounting()
            Log.d(TAG, "stopCounting")
        }
    }

    private fun updateCountTextView(count: Int) {
        countTextView.text = count.toString()
    }

}