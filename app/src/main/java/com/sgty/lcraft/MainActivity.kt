package com.sgty.lcraft

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 开启全屏沉浸
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val gameView = GameView(this)
        setContentView(gameView)

        gameView.onSettingsRequested = {
            showSettingsDialog(gameView)
        }
        // 启动时横屏提示
        showLandscapeTip()
    }

    private fun showLandscapeTip() {
        AlertDialog.Builder(this)
            .setTitle("💡 温馨提示")
            .setMessage("横屏使用体验更佳哦！\n\n由于物品栏和地图比较宽，横屏能让你看到更完整的沙盒世界。\n提示：此游戏遵循GPL v3.0开源，唯一下载地址GitHub：\nhttps://github.com/ShanGuang0421/LauncherCraft")
            .setPositiveButton("我知道了") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showSettingsDialog(gameView: GameView) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val prefs = getSharedPreferences("game_settings", MODE_PRIVATE)
        val currentSize = prefs.getFloat("slot_size", 80f).toInt()

        val textView = TextView(this).apply {
            text = "调整物品栏格子大小: $currentSize"
            textSize = 18f
        }

        val seekBar = SeekBar(this).apply {
            max = 50 
            progress = currentSize - 50 
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "调整物品栏格子大小: ${50 + progress}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        layout.addView(textView)
        layout.addView(seekBar)

        AlertDialog.Builder(this)
            .setTitle("⚙️ 游戏设置")
            .setView(layout)
            .setPositiveButton("确定") { _, _ ->
                val newSlotSize = (50 + seekBar.progress).toFloat()
                gameView.updateSettings(newSlotSize, 7)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}