package com.shekhar.demo.smsretrieversample

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MySMSBroadcastReceiver.OTPReceiveListener {

    var mCredentialsApiClient: GoogleApiClient? = null
    private val KEY_IS_RESOLVING = "is_resolving"
    private val RC_HINT = 2
    private var otpReceiver: MySMSBroadcastReceiver.OTPReceiveListener = this

    val smsBroadcast = MySMSBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCredentialsApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()

        requestHint()

        startSMSListener()

        smsBroadcast.initOTPListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)

        applicationContext.registerReceiver(smsBroadcast, intentFilter)

        //Used to generate hash signature
        AppSignatureHelper(applicationContext).appSignatures

    }

    override fun onOTPReceived(otp: String) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsBroadcast)
        otpTxtView.text = "Your OTP is: $otp"
    }

    override fun onOTPTimeOut() {
        otpTxtView.text = "Timeout"
        Toast.makeText(this, " SMS retriever API Timeout", Toast.LENGTH_SHORT).show()
    }

    private fun startSMSListener() {

        SmsRetriever.getClient(this).startSmsRetriever()
            .addOnSuccessListener {
                otpTxtView.text = "Waiting for OTP"
                Toast.makeText(this, "SMS Retriever starts", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                otpTxtView.text = "Cannot Start SMS Retriever"
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
            }
    }

    private fun requestHint() {

        val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
        val intent = Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient, hintRequest)

        try {
            startIntentSenderForResult(intent.intentSender, RC_HINT, null, 0, 0, 0)
        } catch (e: Exception) {
            Log.e("Error In getting Msg", e.message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_HINT && resultCode == Activity.RESULT_OK) {
            val credential: Credential = data!!.getParcelableExtra(Credential.EXTRA_KEY)
            print("credential : $credential")
        }
    }
}
