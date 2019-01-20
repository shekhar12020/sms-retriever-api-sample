package com.shekhar.demo.smsretrieversample

import android.annotation.SuppressLint
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, MySMSBroadcastReceiver.OTPReceiveListener {

    var mCredentialsApiClient: GoogleApiClient? = null
    private val KEY_IS_RESOLVING = "is_resolving"
    private val RC_HINT = 2
    private var otpReceiver: MySMSBroadcastReceiver.OTPReceiveListener = this

    val smsBroadcast = MySMSBroadcastReceiver()

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onOTPReceived(otp: String) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsBroadcast)
        otpTxtView.text = "Your OTP is: $otp"
    }

    override fun onOTPTimeOut() {
        otpTxtView.text = "Timeout"
        Toast.makeText(this, " SMS retriever API Timeout", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCredentialsApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .enableAutoManage(this, this)
            .addApi(Auth.CREDENTIALS_API)
            .build()

        startSMSListener()

        smsBroadcast.initOTPListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)

        applicationContext.registerReceiver(smsBroadcast, intentFilter)

        AppSignatureHelper(applicationContext).appSignatures

        requestHint()
    }

    private fun startSMSListener() {

        val client = SmsRetriever.getClient(this /* context */)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            otpTxtView.text = "Waiting for the OTP"
            Toast.makeText(this, "SMS Retriever starts", Toast.LENGTH_LONG).show()
        }

        task.addOnFailureListener {
            otpTxtView.text = "Cannot Start SMS Retriever"
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("LongLogTag")
    private fun requestHint() {
        val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(
            mCredentialsApiClient, hintRequest
        )

        try {
            startIntentSenderForResult(intent.intentSender, RC_HINT, null, 0, 0, 0)
        } catch (e: Exception) {
            Log.e("Error In getting Message", e.message)
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
