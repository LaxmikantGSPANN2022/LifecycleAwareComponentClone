package com.example.lifecycleawarecomponentclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.lifecycleawarecomponents.NetworkMonitorUpdated

class MainActivity : AppCompatActivity() {

    private lateinit var networkMonitorUpdated: NetworkMonitorUpdated
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        networkMonitorUpdated = NetworkMonitorUpdated(this)

        findViewById<Button>(R.id.button).setOnClickListener(View.OnClickListener {
            Toast.makeText(this@MainActivity, "On Button Click!!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, NextActivity::class.java))
        })
    }

    override fun onStart() {
        super.onStart()
        networkMonitorUpdated.registerNetworkCallbacks()
    }

    override fun onStop() {
        super.onStop()
        networkMonitorUpdated.unRegisterNetworkCallbacks()
    }
}