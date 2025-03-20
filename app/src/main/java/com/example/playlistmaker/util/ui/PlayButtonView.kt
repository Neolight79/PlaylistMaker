package com.example.playlistmaker.util.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.graphics.drawable.toBitmap
import com.example.playlistmaker.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): View(context, attrs, defStyleAttr, defStyleRes) {

    // region Переменные

    // Минимальный размер кнопки проигрывателя берем из ресурсов
    private val minViewSize = resources.getDimensionPixelSize(
        R.dimen.play_buttons_diameter
    )

    // Инициализируем прямоугольник для вывода изображения
    private val rect = RectF()

    // Переменная текущего статуса проигрывания
    private var isPlaying = false

    // endregion

    // region Картинки

    // Векторные исходные изображения кнопок состояний проигрывателя
    private var playImageDrawable: Drawable? = null
    private var pauseImageDrawable: Drawable? = null

    // Растровые данные изображений состояний проигрывателя
    private var playImageBitmap: Bitmap? = null
    private var pauseImageBitmap: Bitmap? = null

    // endregion

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                val backgroundTint = getColor(R.styleable.PlaybackButtonView_android_backgroundTint, Color.WHITE)
                playImageDrawable = getDrawable(R.styleable.PlaybackButtonView_playImageId)
                pauseImageDrawable = getDrawable(R.styleable.PlaybackButtonView_pauseImageId)
                playImageDrawable?.setTint(backgroundTint)
                pauseImageDrawable?.setTint(backgroundTint)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Расчёт ширины
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val contentWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            // Если нужно указать точное значение ширины —
            // тогда установим его, лишь в случае, если оно больше минимального
            MeasureSpec.EXACTLY -> max(widthSize, minViewSize)
            // Во всех остальных случаях устанавливаем наше минимальное значение
            else -> minViewSize
        }

        // Расчёт высоты
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val contentHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            // Если нужно указать точное значение высоты —
            // тогда установим его, лишь в случае, если оно больше минимального
            MeasureSpec.EXACTLY -> max(heightSize, minViewSize)
            // Во всех остальных случаях устанавливаем наше минимальное значение
            else -> minViewSize
        }

        // Если стороны различны, то берем минимальную
        val size = min(contentWidth, contentHeight)

        // Устанавливаем подсчитанные размеры
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        super.onSizeChanged(w, h, oldw, oldh)

        // Устанавливаем значения для прямоугольника вывода изображения
        rect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        // Рендерим векторные изображения для статусов проигрывателя
        playImageBitmap = playImageDrawable?.toBitmap(measuredWidth, measuredHeight)
        pauseImageBitmap = pauseImageDrawable?.toBitmap(measuredWidth, measuredHeight)

    }

    override fun onDraw(canvas: Canvas) {

        // Рисуем изображение
        when (isPlaying) {
            true -> pauseImageBitmap?.let {
                canvas.drawBitmap(it, null, rect, null)
            }
            false-> playImageBitmap?.let {
                canvas.drawBitmap(it, null, rect, null)
            }
        }
    }

    fun setIsPlaying(isPlayingNow: Boolean) {
        if (isPlayingNow != isPlaying) {
            // Если поменялось состояние проигрывания, то меняем статус и обновляем отображение
            isPlaying = isPlayingNow
            invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Проверяем, что нажатие оказалось в пределах круга кнопки
                // В event мы получаем координаты точки относительно начала координат нашей View,
                //    следовательно, надо скорректировать начала координат в центр круга и определить
                //    расстояние от этой точки до новой точки координат, если расстояние окажется меньше
                //    радиуса круга, значит нажатие попало куда следует
                val deltaX = abs((measuredWidth / 2) - event.x)
                val deltaY = abs((measuredHeight / 2) - event.y)
                val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
                if (distance > (measuredWidth / 2)) {
                    // Значит отпустили за пределами радиуса круга кнопки
                    return true
                } else {
                    // Обрабатываем нажатие
                    isPlaying = !isPlaying
                    invalidate()
                    callOnClick()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

}