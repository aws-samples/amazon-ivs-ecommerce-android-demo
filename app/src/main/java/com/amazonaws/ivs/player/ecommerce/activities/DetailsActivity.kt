package com.amazonaws.ivs.player.ecommerce.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.common.hideKeyboard
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        initUi()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_default, R.anim.anim_slide_down)
    }

    private fun initUi() {
        back_button.setOnClickListener {
            onBackPressed()
        }

        scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            search_view.clearFocus()
            hideKeyboard()
        })
    }
}
