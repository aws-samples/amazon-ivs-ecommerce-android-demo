package com.amazonaws.ivs.player.ecommerce.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.activities.adapters.LiveStreamAdapter
import com.amazonaws.ivs.player.ecommerce.common.Configuration
import com.amazonaws.ivs.player.ecommerce.common.Configuration.EXTRA_LINK
import com.amazonaws.ivs.player.ecommerce.common.hideKeyboard
import com.amazonaws.ivs.player.ecommerce.common.setCounter
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val liveStreamAdapter by lazy {
        LiveStreamAdapter {
            val intent = Intent(this, WatchActivity::class.java)
            intent.putExtra(EXTRA_LINK, it)
            startActivity(intent)
            overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_default)
        }
    }

    lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
    }

    private fun initUi() {
        initAdapter()
        initTimer()

        scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            search_view.clearFocus()
            hideKeyboard()
        })
    }

    private fun initTimer() {
         timer = object : CountDownTimer(getCountdownTime(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val dateFormat = SimpleDateFormat("HH mm ss", Locale.US)
                countdown_view.setCounter(dateFormat.format(millisUntilFinished))
            }

            override fun onFinish() {
                timer.start()
            }
        }
        timer.start()
    }

    private fun initAdapter() {
        rv_streams.apply {
            adapter = liveStreamAdapter
        }
        liveStreamAdapter.items = Configuration.streamList
    }

    fun getCountdownTime(): Long {
        val millisDay = Configuration.MILLIS_DAY
        val currentTime = System.currentTimeMillis()
        val millisTillNow = currentTime % millisDay
        return millisDay - millisTillNow
    }
}
