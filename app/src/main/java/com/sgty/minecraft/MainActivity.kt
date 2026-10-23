package com.sgty.minecraft 

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sgty.minecraft.ui.GameView 

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(GameView(this))
    }
}