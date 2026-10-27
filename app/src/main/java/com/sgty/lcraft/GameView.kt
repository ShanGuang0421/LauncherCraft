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

    // 先创建一个全为 0（空气）的地图，具体地形在 init 里动态生成
    private val worldMap = Array(mapHeight) { IntArray(mapWidth) { 0 } }

    private var currentBlockInHand = 11 
    private val inventoryList = listOf(1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)

    private var slotSize = 80f
    private val padding = 15f
    private var maxPerRow = 7
    private var invStartX = 0f

    var onSettingsRequested: (() -> Unit)? = null

    init {
        // 加载设置
        val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        slotSize = prefs.getFloat("slot_size", 80f)
        maxPerRow = prefs.getInt("max_per_row", 7)

        // 加载贴图
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

        // ============ 动态地形生成开始 ============
        val seed = (1000..9999).random() // 随机种子

        for (x in 0 until mapWidth) {
            // 用两个正弦波叠加，算出每一列地表的 Y 坐标
            val wave1 = Math.sin((x + seed) * 0.2) * 3.0
            val wave2 = Math.sin((x + seed) * 0.5) * 1.5
            // 地表 Y 坐标（防止超出地图范围，限制在 3 到 16 之间）
            val surfaceY = (mapHeight / 2 + wave1 + wave2).toInt().coerceIn(3, 16)

            for (y in 0 until mapHeight) {
                if (y < surfaceY) {
                    worldMap[y][x] = 0 // 天空
                } else if (y == surfaceY) {
                    worldMap[y][x] = 2 // 草方块
                } else if (y < surfaceY + 4) {
                    worldMap[y][x] = 9 // 泥土（草地往下4格）
                } else if (y < mapHeight - 1) {
                    worldMap[y][x] = 10 // 石头
                } else {
                    worldMap[y][x] = 4 // 最底层基岩
                }
            }
        }

        // 随机种树
        for (i in 0 until 6) { // 种 6 棵树
            val treeX = (2 until mapWidth - 2).random() // 避开边缘
            
            // 从上往下找这一列的草地
            var surfaceY = -1
            for (y in 0 until mapHeight) {
                if (worldMap[y][treeX] == 2) {
                    surfaceY = y
                    break
                }
            }

            // 如果找到草地，并且上方空间足够种下 4 格高的树
            if (surfaceY != -1 && surfaceY > 4) {
                worldMap[surfaceY - 1][treeX] = 13 // 树干
                worldMap[surfaceY - 2][treeX] = 13
                worldMap[surfaceY - 3][treeX] = 6  // 树叶
                worldMap[surfaceY - 3][treeX - 1] = 6
                worldMap[surfaceY - 3][treeX + 1] = 6
                worldMap[surfaceY - 4][treeX] = 6
            }
        }
        // ============ 动态地形生成结束 ============
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

        // 渲染地图
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

        // 右上角设置按钮
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

            // 点击设置
            if (touchX > width - 160f && touchY < 150f) {
                onSettingsRequested?.invoke()
                return true
            }

            // 点击物品栏
            val invStartY = height - (slotSize * 2) - (padding * 2) - 40f
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
                
                if (worldMap[gridY][gridX] == 4) return true // 基岩保护
                
                if (worldMap[gridY][gridX] == 0) {
                    // 放置方块
                    if (currentBlockInHand == 11 || currentBlockInHand == 12) {
                        if (gridY > 0 && worldMap[gridY - 1][gridX] != 4) {
                            worldMap[gridY][gridX] = currentBlockInHand
                            worldMap[gridY - 1][gridX] = 0
                        }
                    } else {
                        worldMap[gridY][gridX] = currentBlockInHand 
                    }
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