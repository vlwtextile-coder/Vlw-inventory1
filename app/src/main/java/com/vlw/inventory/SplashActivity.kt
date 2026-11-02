package com.vlw.inventory

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GoldProgressView(context: Context) : View(context) {
    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#232C38")
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#B8C0CC")
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val r = h / 2f
        rect.set(1f, 1f, w - 1f, h - 1f)
        canvas.drawRoundRect(rect, r, r, trackPaint)
        val fw = (w - 2f) * progress
        if (fw > 0f) {
            fillPaint.shader = LinearGradient(
                0f, 0f, w, 0f,
                intArrayOf(
                    Color.parseColor("#B8860B"),
                    Color.parseColor("#FFD76A"),
                    Color.parseColor("#E0A526")
                ),
                null, Shader.TileMode.CLAMP
            )
            val right = minOf(1f + maxOf(fw, h - 2f), w - 1f)
            rect.set(1f, 1f, right, h - 1f)
            canvas.drawRoundRect(rect, r, r, fillPaint)
        }
        rect.set(1f, 1f, w - 1f, h - 1f)
        canvas.drawRoundRect(rect, r, r, strokePaint)
    }
}

class SplashActivity : AppCompatActivity() {
    private var shimmer: ValueAnimator? = null
    private var loader: ValueAnimator? = null
    private var opened = false

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor("#020B1F"))

        val image = ImageView(this)
        image.setImageResource(R.drawable.vlw_splash)
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        root.addView(
            image,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(root)

        root.post {
            val w = root.width
            val h = root.height
            if (w == 0 || h == 0) {
                openMain()
                return@post
            }

            val bandW = (w * 0.35f).toInt()
            val band = View(this)
            band.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(0x00FFFFFF, 0x38FFFFFF, 0x00FFFFFF)
            )
            val bandLp = FrameLayout.LayoutParams(bandW, (h * 0.07f).toInt())
            bandLp.topMargin = (h * 0.275f).toInt()
            root.addView(band, bandLp)
            band.translationX = -bandW.toFloat()

            val status = TextView(this)
            status.text = "Initializing Application..."
            status.setTextColor(Color.WHITE)
            status.textSize = 15f
            status.gravity = Gravity.CENTER
            val statusLp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            statusLp.topMargin = (h * 0.808f).toInt()
            root.addView(status, statusLp)

            val side = (w * 0.17f).toInt()
            val bar = GoldProgressView(this)
            val barLp = FrameLayout.LayoutParams(w - 2 * side, dp(9))
            barLp.leftMargin = side
            barLp.topMargin = (h * 0.8625f).toInt() - dp(4)
            root.addView(bar, barLp)

            val pct = TextView(this)
            pct.text = "0%"
            pct.setTextColor(Color.parseColor("#F2C14E"))
            pct.textSize = 13f
            pct.gravity = Gravity.CENTER
            val pctLp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            pctLp.topMargin = (h * 0.885f).toInt()
            root.addView(pct, pctLp)

            shimmer = ValueAnimator.ofFloat(-bandW.toFloat(), w.toFloat()).apply {
                duration = 1600
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener { band.translationX = it.animatedValue as Float }
                start()
            }

            loader = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2800
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    val v = it.animatedValue as Float
                    bar.progress = v
                    pct.text = "${(v * 100).toInt()}%"
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        openMain()
                    }
                })
                start()
            }
        }
    }

    private fun openMain() {
        if (opened) return
        opened = true
        shimmer?.cancel()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        loader?.removeAllListeners()
        loader?.cancel()
        shimmer?.cancel()
        super.onDestroy()
    }
}
