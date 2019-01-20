package com.shekhar.demo.smsretrieversample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class MySMSBroadcastReceiver : BroadcastReceiver() {

    private var otpReceiver: OTPReceiveListener? = null

    fun initOTPListener(receiver: OTPReceiveListener) {
        this.otpReceiver = receiver
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {

            val extras = intent.extras
            val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {

                    // Get SMS message contents
                    var otp: String = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String

                    val pattern = Pattern.compile("(\\d{4})")
                    val matcher = pattern.matcher(otp)

                    // Extract one-time code from the message and complete verification
                    var value = ""
                    if (matcher.find()) {
                        System.out.println(matcher.group(1))
                        value = matcher.group(1)
                    }

                    println("message : $value")
                    otpReceiver?.onOTPReceived(value)
                }

                CommonStatusCodes.TIMEOUT ->
                    // Waiting for SMS timed out (5 minutes)
                    otpReceiver?.onOTPTimeOut()
            }
        }
    }

    interface OTPReceiveListener {

        fun onOTPReceived(otp: String)

        fun onOTPTimeOut()
    }
}