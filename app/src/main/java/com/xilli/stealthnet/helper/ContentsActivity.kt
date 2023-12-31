package com.xilli.stealthnet.helper

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.onesignal.OneSignal
import com.xilli.stealthnet.R
import com.xilli.stealthnet.ui.activity.MainActivity

abstract class ContentsActivity: AppCompatActivity() {

    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0

    var lottieAnimationView: LottieAnimationView? = null
    var vpnToastCheck = true
    var handlerTraffic: Handler? = null
    private val adCount = 0

    private var loadingAd: Boolean? = false
    var frameLayout: RelativeLayout? = null

    @JvmField


    var progressBarValue = 0
    var handler = Handler(Looper.getMainLooper())
    private val customHandler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    var timeInMilliseconds = 0L
    var timeSwapBuff = 0L
    var updatedTime = 0L


    var tvIpAddress: TextView? = null
    var textDownloading: TextView? = null
    var textUploading: TextView? = null
    var tvConnectionStatus: TextView? = null
    var ivConnectionStatusImage: ImageView? = null
    var ivVpnDetail: ImageView? = null
    var timerTextView: TextView? = null
    var connectBtnTextView: ImageView? = null
    var connectionStateTextView: TextView? = null
    var rcvFree: RecyclerView? = null
    var footer: RelativeLayout? = null
    lateinit var sharedPreferences : SharedPreferences

    @JvmField
    var imgFlag: ImageView? = null

    @JvmField
    var flagName: TextView? = null




    private var STATUS: String? = "DISCONNECTED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        tvConnectionStatus = findViewById(R.id.connection_status)

//        ivConnectionStatusImage = findViewById(R.id.connection_status_image)

        connectBtnTextView = findViewById(R.id.connect)

//        connectionStateTextView = findViewById(R.id.connection_state)

        imgFlag = findViewById(R.id.flagimageView)

        flagName = findViewById(R.id.flag_name)

        connectBtnTextView?.setOnClickListener {
            btnConnectDisconnect()
        }

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        tvIpAddress = findViewById<TextView>(R.id.vpn_ip)
        showIP()
    }

    private fun showIP() {
        val queue = Volley.newRequestQueue(this)
        val urlip = "https://checkip.amazonaws.com/"

        val stringRequest =
            StringRequest(Request.Method.GET, urlip, { response -> tvIpAddress?.setText(response) })
            { e ->
                run {
                    tvIpAddress?.setText(getString(R.string.app_name))
                }
            }
        queue.add(stringRequest)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showOrHideAppendLayout() {
        if (footer!!.visibility == View.VISIBLE) {
            footer!!.visibility = View.GONE
        } else {
            footer!!.visibility = View.VISIBLE
        }
    }

    private val mUIHandler = Handler(Looper.getMainLooper())
    val mUIUpdateRunnable: Runnable = object : Runnable {
        override fun run() {

            checkRemainingTraffic()
            mUIHandler.postDelayed(this, 10000)
        }
    }

    private fun btnConnectDisconnect() {
        if (STATUS != "DISCONNECTED") {
            disconnectAlert()
        } else {
            if (!Utility.isOnline(applicationContext)) {
                showMessage("No Internet Connection", "error")
            } else {
                checkSelectedCountry()
            }
        }
    }

    protected abstract fun checkRemainingTraffic()

    protected fun updateUI(status:String) {
        when (status) {
            "CONNECTED" -> {
                STATUS = "CONNECTED"
                textDownloading!!.visibility = View.VISIBLE
                textUploading!!.visibility = View.VISIBLE
                connectBtnTextView!!.isEnabled = true
                connectionStateTextView!!.setText(R.string.connected)
                timerTextView!!.visibility = View.GONE
                hideConnectProgress()
                showIP()
                connectBtnTextView!!.visibility = View.VISIBLE
                tvConnectionStatus!!.text = "Selected"
                lottieAnimationView!!.visibility = View.GONE
                Toast.makeText(this@ContentsActivity, "Server Connected", Toast.LENGTH_SHORT).show()
            }
            "AUTH" -> {
                STATUS = "AUTHENTICATION"
                connectBtnTextView!!.visibility = View.VISIBLE
                lottieAnimationView!!.visibility = View.VISIBLE

                connectionStateTextView!!.setText(R.string.auth)
                connectBtnTextView!!.isEnabled = true
                timerTextView!!.visibility = View.GONE
                Toast.makeText(this@ContentsActivity, "Server AUTHENTICATION", Toast.LENGTH_SHORT).show()
            }
            "WAIT" -> {
                STATUS = "WAITING"
                connectBtnTextView!!.visibility = View.VISIBLE
                lottieAnimationView!!.visibility = View.VISIBLE

                connectionStateTextView!!.setText(R.string.wait)
                connectBtnTextView!!.isEnabled = true
                timerTextView!!.visibility = View.GONE
                Toast.makeText(this@ContentsActivity, "Server AUTHENTICATION", Toast.LENGTH_SHORT).show()
            }
            "RECONNECTING" -> {
                STATUS = "RECONNECTING"
                connectBtnTextView!!.visibility = View.VISIBLE
                lottieAnimationView!!.visibility = View.VISIBLE

                connectionStateTextView!!.setText(R.string.recon)
                connectBtnTextView!!.isEnabled = true
                timerTextView!!.visibility = View.GONE
            }
            "LOAD" -> {
                STATUS = "LOAD"
                connectBtnTextView!!.visibility = View.VISIBLE
                lottieAnimationView!!.visibility = View.VISIBLE

                connectionStateTextView!!.setText(R.string.connecting)
                connectBtnTextView!!.isEnabled = true
                timerTextView!!.visibility = View.GONE
            }
            "ASSIGN_IP" -> {
                STATUS = "LOAD"
                connectBtnTextView!!.visibility = View.VISIBLE
                lottieAnimationView!!.visibility = View.VISIBLE

                connectionStateTextView!!.setText(R.string.assign_ip)
                connectBtnTextView!!.isEnabled = true
                timerTextView!!.visibility = View.GONE
            }
            "GET_CONFIG" -> {
                STATUS = "LOAD"
                connectBtnTextView!!.visibility = View.VISIBLE
                lottieAnimationView!!.visibility = View.VISIBLE

                connectionStateTextView!!.setText(R.string.config)
                connectBtnTextView!!.isEnabled = true
                timerTextView!!.visibility = View.GONE
            }
            "USERPAUSE" -> {
                STATUS = "DISCONNECTED"
                tvConnectionStatus!!.text = "Not Selected"
                tvConnectionStatus!!.text = "Not Selected"
            }
            "NONETWORK" -> {
                STATUS = "DISCONNECTED"
                tvConnectionStatus!!.text = "Not Selected"
                showIP()


                tvConnectionStatus!!.text = "Not Selected"
            }
            "DISCONNECTED" -> {
                STATUS = "DISCONNECTED"
                tvConnectionStatus!!.text = "Not Selected"
                timerTextView!!.visibility = View.INVISIBLE
                hideConnectProgress()
                showIP()

                tvConnectionStatus!!.text = "Not Selected"
            }
        }
    }

     private fun hideConnectProgress() {
        connectionStateTextView!!.visibility = View.VISIBLE
    }

     private fun disconnectAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to disconnect?")
        builder.setPositiveButton(
            "Disconnect"
        ) { _, _ ->
            disconnectFromVpn()
            STATUS = "DISCONNECTED"

            textDownloading!!.text = "0.0 kB/s"
            textUploading!!.text = "0.0 kB/s"

            showMessage("Server Disconnected", "success")
        }
        builder.setNegativeButton(
            "Cancel"
        ) { _, _ ->
            showMessage("VPN Remains Connected", "success")
        }
        builder.show()
    }




    private fun proceedToNextView(id: Int) {
        when (id) {

        }

    }

    companion object {
        protected val TAG = MainActivity::class.java.simpleName
    }

     private fun showMessage(msg: String?, type:String) {

        if(type == "success") {
            Toast.makeText(
                this@ContentsActivity,
                msg + "",
                Toast.LENGTH_SHORT
            ).show()
        } else if (type == "error") {
            Toast.makeText(
                this@ContentsActivity,
                msg + "",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this@ContentsActivity,
                msg + "",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    open fun updateConnectionStatus(
        duration: String?,
        lastPacketReceive: String?,
        byteIn: String,
        byteOut: String
    ) {
        val byteinKb = byteIn.split("-").toTypedArray()[1]
        val byteoutKb = byteOut.split("-").toTypedArray()[1]

        textDownloading!!.text = byteinKb
        textUploading!!.text = byteoutKb
        timerTextView!!.text = duration
    }

    fun showInterstitialAndConnect() {
       prepareVpn()
    }

    abstract fun checkSelectedCountry()
    abstract fun prepareVpn()
    abstract fun disconnectFromVpn()
}