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

        // 启动时横屏提示（现在的它是独立的，不会嵌套了）
        showLandscapeTip()
    }

    // ⚠️ 这个是独立的函数！
    private fun showLandscapeTip() {
        AlertDialog.Builder(this)
            .setTitle("💡 温馨提示")
            .setMessage("横屏使用体验更佳哦！\n\n" +
                    "【防伪声明】\n" +
                    "本游戏完全免费开源，仅在 GitHub 发布。\n" +
                    "如果你是在任何应用商店下载的，那绝对是被篡改的盗版！\n\n" +
                    "官方仓库：github.com/ShanGuang0421/LauncherCraft")
            .setPositiveButton("我知道了") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    // ⚠️ 这个是独立的设置函数！
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

        // 组装设置界面
        layout.addView(textView)
        layout.addView(seekBar)

        // 新增防伪小字
        val copyrightView = TextView(this).apply {
            text = "\n本游戏未上架任何应用商店。\n官方下载地址：github.com/ShanGuang0421/LauncherCraft"
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
            setPadding(0, 40, 0, 0)
        }
        layout.addView(copyrightView)

        // 弹出设置对话框（你刚刚缺失的代码在这里！）
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