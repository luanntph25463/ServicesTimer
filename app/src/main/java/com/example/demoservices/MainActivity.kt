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
import kotlinx.android.synthetic.main.activity_main.btnStart
import kotlinx.android.synthetic.main.activity_main.btnStop
import kotlinx.android.synthetic.main.activity_main.forgeground

class MainActivity : AppCompatActivity() {
    private lateinit var countTextView: TextView
    private lateinit var timerService: TimerService
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        forgeground.setOnClickListener{
            val serviceIntent = Intent(this, TimerService::class.java)
            startService( serviceIntent)
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
            Log.d("this", "$isBound")

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
        Log.d("this", "onconnect")

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
            Log.d("this", "onStop")

            isBound = false
        }
        // hủy đăng ký từ boardcast receiver
        unregisterReceiver(countReceiver)
    }

    private fun startCounting() {
        Log.d("this", "$isBound")

        if (isBound) {
            timerService.startCounting()
            Log.d("this", "startCounting")

        }
    }

    private fun stopCounting() {
        if (isBound) {
            timerService.stopCounting()
            Log.d("this", "stopCounting")
        }
    }

    private fun updateCountTextView(count: Int) {
        countTextView.text = count.toString()
    }

}