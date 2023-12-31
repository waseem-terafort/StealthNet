package com.xilli.stealthnet.ui.activity

import android.animation.Animator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.ActivitySplashBinding
import com.xilli.stealthnet.helper.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import top.oneconnectapi.app.api.OneConnect
import java.io.IOException


class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    Log.d("MyApp", "Coroutine started")
                    val oneConnect = OneConnect()
                    oneConnect.initialize(this@SplashActivity, "2w0QvmDcWoNfZWTmEukdXSF8JAu4zLka2Br7u1iIvr9UE8Y6lw")
                    val response = oneConnect.fetch(true)
                    Log.d("MyApp", "API Response: $response")
                    try {
                        Constants.FREE_SERVERS = oneConnect.fetch(true)
                        Constants.PREMIUM_SERVERS = oneConnect.fetch(true)
                        Log.d("MyApp", "Data fetched successfully") // Log that data was fetched successfully
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("MyApp", "IOException: " + e.message) // Log any IOException
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MyApp", "Exception: " + e.message) // Log any other exceptions
                }
            }
        }
        val view = binding.root
        setContentView(view)
        binding.lottieAnimationViewsplash.setAnimation(R.raw.loading_animation)

        binding.lottieAnimationViewsplash.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }
            override fun onAnimationEnd(animation: Animator) {
                startActivity(MainActivity.newIntent(this@SplashActivity))
                finish()
            }
            override fun onAnimationCancel(animation: Animator) {
            }
            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        binding.lottieAnimationViewsplash.playAnimation()
    }
}