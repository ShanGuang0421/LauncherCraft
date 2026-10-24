package com.sgty.lcraft

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.sgty.lcraft.R

class GameView(context: Context) : View(context) {

    private var sandBitmap: Bitmap? = null
    private var grassBitmap: Bitmap? = null
    private var hayBitmap: Bitmap? = null
    private var bedrockBitmap: Bitmap? = null
    private var leavesBitmap: Bitmap? = null
    private var planksBitmap: Bitmap? = null
    private var beetrootBitmap: Bitmap? = null
    private var pumpkinBitmap: Bitmap? = null
    private var dirtBitmap: Bitmap? = null
    private var stoneBitmap: Bitmap? = null
    private var odBitmap: Bitmap? = null
    private var dodBitmap: Bitmap? = null
    private var oakBitmap: Bitmap? = null
    private var waterBitmap: Bitmap? = null

    private val paint = Paint().apply {
        isFilterBitmap = false
        isAntiAlias = false
    }

    private val blockSize = 45f
    private var offsetX = 0f
    private var offsetY = 0f

    private val mapWidth = 30
    private val mapHeight = 20

    // 纯净的地形生成
    private val worldMap = Array(mapHeight) { y ->
        IntArray(mapWidth) { x ->
            when {
                y < 6 -> 0 
                y == 6 -> 2 
                y < 15 -> 9 
                y < 19 -> 10 
                else -> 4 
            }
        }
    }

    private var currentBlockInHand = 11 
    private val inventoryList = listOf(1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)

    private var slotSize = 80f
    private val padding = 15f
    private var maxPerRow = 7
    private var invStartX = 0f

    var onSettingsRequested: (() -> Unit)? = null

    init {
        val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        slotSize = prefs.getFloat("slot_size", 80f)
        maxPerRow = prefs.getInt("max_per_row", 7)

        sandBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sand)
        grassBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.grassblock)
        hayBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.haybale)
        bedrockBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bedrock)
        leavesBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.oakleaves)
        planksBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.oakplanks)
        beetrootBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.beetroot)
        pumpkinBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pumpkin)
        dirtBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dirt)
        stoneBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.stone)
        odBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.od)
        dodBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dod)
        oakBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.oak)
        waterBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.water)

        // 生成房子、树、水池 (无彩蛋)
        for (x in 3..7) {
            worldMap[4][x] = 5
            worldMap[5][x] = 5
        }
        worldMap[5][5] = 11

        for (i in 0 until 5) {
            val treeX = (10..15).random()
            val treeY = 6
            if (worldMap[treeY][treeX] == 2) {
                worldMap[treeY - 1][treeX] = 13
                worldMap[treeY - 2][treeX] = 13 
                worldMap[treeY - 3][treeX] = 6 
                worldMap[treeY - 3][treeX - 1] = 6
                worldMap[treeY - 3][treeX + 1] = 6
            }
        }
        for (x in 20..25) {
            worldMap[6][x] = 14
            worldMap[7][x] = 14
        }
    }

    fun updateSettings(newSlotSize: Float, newMaxPerRow: Int) {
        this.slotSize = newSlotSize
        this.maxPerRow = newMaxPerRow
        
        val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("slot_size", newSlotSize)
            putInt("max_per_row", newMaxPerRow)
            apply()
        }
        
        onSizeChanged(width, height, width, height)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val mapPixelWidth = mapWidth * blockSize
        val mapPixelHeight = mapHeight * blockSize
        offsetX = (w - mapPixelWidth) / 2f
        offsetY = (h - mapPixelHeight) / 2f - 100f

        val totalInvWidth = maxPerRow * slotSize + (maxPerRow - 1) * padding
        invStartX = (w - totalInvWidth) / 2f
    }

    private fun getBitmapByType(type: Int): Bitmap? {
        return when (type) {
            1 -> sandBitmap; 2 -> grassBitmap; 3 -> hayBitmap; 4 -> bedrockBitmap; 5 -> planksBitmap
            6 -> leavesBitmap; 7 -> beetrootBitmap; 8 -> pumpkinBitmap; 9 -> dirtBitmap; 10 -> stoneBitmap
            11 -> odBitmap; 12 -> dodBitmap; 13 -> oakBitmap; 14 -> waterBitmap; else -> null
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#87CEEB"))

        for (y in 0 until worldMap.size) {
            for (x in 0 until worldMap[y].size) {
                val blockType = worldMap[y][x]
                if (blockType == 0) continue
                val currentBitmap = getBitmapByType(blockType)
                if (currentBitmap != null) {
                    val left = offsetX + x * blockSize
                    val top = offsetY + y * blockSize
                    val right = left + blockSize
                    val isDoor = (blockType == 11 || blockType == 12)
                    val renderHeight = if (isDoor) blockSize * 2f else blockSize
                    val actualTop = if (isDoor) top - blockSize else top
                    val actualBottom = actualTop + renderHeight
                    val destRect = RectF(left, actualTop, right, actualBottom)
                    canvas.drawBitmap(currentBitmap, null, destRect, paint)
                }
            }
        }

        // 物品栏背景板
        val invStartY = height - (slotSize * 2) - (padding * 2) - 40f 
        val bgPaint = Paint().apply { color = Color.parseColor("#99000000") }
        canvas.drawRect(0f, invStartY - 20f, width.toFloat(), height.toFloat(), bgPaint)

        // 渲染物品栏
        for (i in inventoryList.indices) {
            val type = inventoryList[i]
            val bitmap = getBitmapByType(type)
            val row = i / maxPerRow
            val col = i % maxPerRow
            val left = invStartX + col * (slotSize + padding)
            val top = invStartY + row * (slotSize + padding)
            val destRect = RectF(left, top, left + slotSize, top + slotSize)
            
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

        // 右上角设置按钮 (纯净版，不搞点击变色了)
        val btnBgPaint = Paint().apply { 
            color = Color.parseColor("#88000000")
            isAntiAlias = true 
        }
        val btnRect = RectF(width - 160f, 50f, width - 60f, 150f)
        canvas.drawRoundRect(btnRect, 20f, 20f, btnBgPaint)
        
        // 三个点图标
        val dotPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true }
        canvas.drawCircle(width - 110f, 80f, 6f, dotPaint)
        canvas.drawCircle(width - 110f, 100f, 6f, dotPaint)
        canvas.drawCircle(width - 110f, 120f, 6f, dotPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y

            // 点击设置按钮
            if (touchX > width - 160f && touchY < 150f) {
                onSettingsRequested?.invoke()
                return true
            }

            val invStartY = height - (slotSize * 2) - (padding * 2) - 40f

            // 点击物品栏
            if (touchY > invStartY) {
                val col = ((touchX - invStartX) / (slotSize + padding)).toInt()
                val row = ((touchY - invStartY) / (slotSize + padding)).toInt()
                val exactLeft = invStartX + col * (slotSize + padding)
                val exactTop = invStartY + row * (slotSize + padding)
                
                if (touchX < exactLeft + slotSize && touchY < exactTop + slotSize) {
                    val slotIndex = row * maxPerRow + col
                    if (slotIndex in inventoryList.indices && col < maxPerRow) {
                        currentBlockInHand = inventoryList[slotIndex]
                        invalidate()
                    }
                }
                return true
            }

            // 点击地图
            val gridX = ((touchX - offsetX) / blockSize).toInt()
            val gridY = ((touchY - offsetY) / blockSize).toInt()

            if (gridY >= 0 && gridY < worldMap.size && gridX >= 0 && gridX < worldMap[gridY].size) {
                if (worldMap[gridY][gridX] == 4) return true
                
                if (worldMap[gridY][gridX] == 0) {
                    if (currentBlockInHand == 11 || currentBlockInHand == 12) {
                        if (gridY > 0 && worldMap[gridY - 1][gridX] != 4) {
                            worldMap[gridY][gridX] = currentBlockInHand
                            worldMap[gridY - 1][gridX] = 0
                        }
                    } else {
                        worldMap[gridY][gridX] = currentBlockInHand 
                    }
                } else {
                    worldMap[gridY][gridX] = 0 
                }
                invalidate() 
            }
        }
        return true
    }
}