package com.sgty.minecraft.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.sgty.minecraft.R

class GameView(context: Context) : View(context) {

    // 所有方块的图片
    private var sandBitmap: Bitmap? = null
    private var grassBitmap: Bitmap? = null
    private var hayBitmap: Bitmap? = null
    private var bedrockBitmap: Bitmap? = null
    private var leavesBitmap: Bitmap? = null
    private var planksBitmap: Bitmap? = null
    private var beetrootBitmap: Bitmap? = null
    private var pumpkinBitmap: Bitmap? = null

    private val paint = Paint().apply {
        isFilterBitmap = false
        isAntiAlias = false
    }

    private val blockSize = 80f
    private val offsetX = 50f
    private val offsetY = 150f // 整体往下一点，给上面留点空间

    // 1=沙子, 2=草, 3=干草, 4=基岩, 5=木板, 6=树叶, 7=甜菜根, 8=南瓜
    private val worldMap = arrayOf(
        intArrayOf(0, 0, 6, 0, 0, 0, 0, 0),
        intArrayOf(0, 6, 6, 6, 0, 0, 0, 0),
        intArrayOf(0, 0, 5, 0, 0, 0, 0, 0),
        intArrayOf(4, 4, 5, 2, 2, 5, 5, 5),
        intArrayOf(4, 4, 5, 2, 2, 5, 0, 5),
        intArrayOf(4, 4, 4, 4, 4, 4, 4, 4) 
    )

    // 当前手里选中的方块 (默认选中木板 5)
    private var currentBlockInHand = 5

    // 底部物品栏的方块列表：沙子、甜菜根、南瓜
    private val inventoryList = listOf(1, 7, 8) 

    init {
        sandBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sand)
        grassBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.grassblock)
        hayBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.haybale)
        bedrockBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bedrock)
        planksBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.oakplanks)
        leavesBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.oakleaves)
        // 确保你下载了这两个图片，并重命名为小写！
        beetrootBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.beetroot) 
        pumpkinBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pumpkin)
    }

    // 辅助函数：根据数字找图片
    private fun getBitmapByType(type: Int): Bitmap? {
        return when (type) {
            1 -> sandBitmap
            2 -> grassBitmap
            3 -> hayBitmap
            4 -> bedrockBitmap
            5 -> planksBitmap
            6 -> leavesBitmap
            7 -> beetrootBitmap
            8 -> pumpkinBitmap
            else -> null
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.DKGRAY) // 背景改成深灰，显得没那么空

        // 1. 画地图
        for (y in 0 until worldMap.size) {
            for (x in 0 until worldMap[y].size) {
                val blockType = worldMap[y][x]
                if (blockType == 0) continue

                val currentBitmap = getBitmapByType(blockType)
                if (currentBitmap != null) {
                    val left = offsetX + x * blockSize
                    val top = offsetY + y * blockSize
                    val destRect = RectF(left, top, left + blockSize, top + blockSize)
                    canvas.drawBitmap(currentBitmap, null, destRect, paint)
                }
            }
        }

        // 2. 画底部的物品栏 (画在最下面)
        val invStartY = 1200f // 物品栏的Y坐标
        for (i in inventoryList.indices) {
            val type = inventoryList[i]
            val bitmap = getBitmapByType(type)
            val left = 50f + i * 150f
            val top = invStartY
            val destRect = RectF(left, top, left + 100f, top + 100f)
            
            // 如果当前手里拿的是这个，就画个白色边框
            if (currentBlockInHand == type) {
                val borderPaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                }
                canvas.drawRect(destRect, borderPaint)
            }
            
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, null, destRect, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y

            // 首先判断：你有没有点到下方的物品栏？
            val invStartY = 1200f
            if (touchY > invStartY && touchY < invStartY + 100f) {
                // 算算点到了第几个格子 (每个格子宽 150)
                val slotIndex = ((touchX - 50f) / 150f).toInt()
                if (slotIndex in inventoryList.indices) {
                    currentBlockInHand = inventoryList[slotIndex]
                    invalidate() // 刷新画面，显示白色选中框
                }
                return true
            }

            // 其次判断：有没有点到地图？
            val gridX = ((touchX - offsetX) / blockSize).toInt()
            val gridY = ((touchY - offsetY) / blockSize).toInt()

            if (gridY >= 0 && gridY < worldMap.size && gridX >= 0 && gridX < worldMap[gridY].size) {
                
                if (worldMap[gridY][gridX] == 4) return true // 基岩挖不动
                
                if (worldMap[gridY][gridX] == 0) {
                    // 放置手里选中的方块
                    worldMap[gridY][gridX] = currentBlockInHand 
                } else {
                    // 破坏方块
                    worldMap[gridY][gridX] = 0 
                }
                invalidate() 
            }
        }
        return true
    }
}