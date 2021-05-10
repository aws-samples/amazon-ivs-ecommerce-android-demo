package com.amazonaws.ivs.player.ecommerce.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.common.Configuration
import com.amazonaws.ivs.player.ecommerce.common.Configuration.EXTRA_LINK
import com.amazonaws.ivs.player.ecommerce.common.changeUtilityAppearance
import com.amazonaws.ivs.player.ecommerce.common.hideKeyboard
import com.amazonaws.ivs.player.ecommerce.common.setCounter
import com.amazonaws.ivs.player.ecommerce.databinding.ActivityMainBinding
import com.amazonaws.ivs.player.ecommerce.databinding.StreamItemBinding
import com.amazonaws.ivs.player.ecommerce.models.LiveStreamItem
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeUtilityAppearance(R.color.white_color, true)

        addStreamItems(Configuration.streamList)
        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            binding.searchView.clearFocus()
            hideKeyboard()
        })

        timer = object : CountDownTimer(getCountdownTime(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val dateFormat = SimpleDateFormat("HH mm ss", Locale.US)
                binding.countdownView.setCounter(dateFormat.format(millisUntilFinished))
            }

            override fun onFinish() {
                timer.start()
            }
        }
        timer.start()
    }

    private fun getCountdownTime(): Long {
        val millisDay = Configuration.MILLIS_DAY
        val currentTime = System.currentTimeMillis()
        val millisTillNow = currentTime % millisDay
        return millisDay - millisTillNow
    }

    private fun addStreamItems(items: List<LiveStreamItem>) {
        items.forEachIndexed { index, streamItem ->
            val streamBinding = StreamItemBinding.inflate(layoutInflater)
            streamBinding.apply {
                data = streamItem
                first = index == 0
                last = index == items.size - 1
                liveStreamItem.isEnabled = index == 0
                liveStreamItem.setOnClickListener {
                    startWatchActivity(streamItem.link)
                }
            }
            binding.streamHolder.addView(streamBinding.root)
        }
    }

    private fun startWatchActivity(streamLink: String) {
        startActivity(Intent(this, PlayerActivity::class.java).putExtra(EXTRA_LINK, streamLink))
    }
}
