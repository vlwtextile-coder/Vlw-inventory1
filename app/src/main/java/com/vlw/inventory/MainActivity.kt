package com.vlw.inventory

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "VLW TEXTILE"
        tv.textSize = 28f
        tv.setPadding(48, 96, 48, 48)
        setContentView(tv)
    }
}
