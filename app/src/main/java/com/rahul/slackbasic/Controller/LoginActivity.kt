package com.rahul.slackbasic.Controller

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.rahul.slackbasic.R
import com.rahul.slackbasic.Services.AuthService
import com.rahul.slackbasic.Utilities.MyPreferences
import com.rahul.slackbasic.Utilities.MyPreferences.set
import com.rahul.slackbasic.Utilities.Constants
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    //Random number assigned to the request code

    private val REQUEST_SIGNUP = 44

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener{
            login()
        }

        tvLinkSignup.setOnClickListener {
            val createUserIntent = Intent(this, CreateUserActivity::class.java)
            startActivityForResult(createUserIntent, REQUEST_SIGNUP)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }

    }

    private fun login() {
        val email = inputEmailLogin.text.toString()
        val password = inputPasswordLogin.text.toString()

        if(!validate(email, password)){
            onLoginFailed()
            return
        }

        btnLogin.isEnabled = false

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating..")
        progressDialog.show()

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()


        AndroidNetworking.post(Constants.URL_LOGIN)
                .setContentType("application/json; charset=utf-8")
                .addByteBody(requestBody.toByteArray())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {

                    override fun onResponse(response: JSONObject) {

                        try{
                            val token = response.getString("token")
                            createUser(email, token)
                        } catch (e: Exception){
                            Log.d("Response", response.toString())
                        }
                        onLoginSuccess()
                        progressDialog.dismiss()
                    }

                    override fun onError(error: ANError) {

                        if (error.errorCode != 0) {
                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("username")) {
                                inputEmailLogin.error = errorResponse.getJSONArray("username").getString(0)
                            }
                            if (errorResponse.has("non_field_errors")) {
                                inputEmailLogin.error = "Either the username or the password is incorrect"
                            }
                        }
                        // handle error
                        onLoginFailed()
                        progressDialog.dismiss()
                    }
                })
    }


    private fun createUser(email: String, token: String) {

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        AndroidNetworking.get("${Constants.URL_GET_USER}$email")
                .addHeaders("Authorization", "Bearer $token")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {

                        var name = response.getString("name")
                        var email = response.getString("email")
                        var avatarName = response.getString("avatarName")
                        var avatarColor = response.getString("avatarColor")
                        var id = response.getString("_id")
                        AuthService.setPreferences(this@LoginActivity, name, email, avatarName, avatarColor, id, token)
                        onLoginSuccess()
                    }

                    override fun onError(error: ANError) {

                        if (error.errorCode != 0) {
                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("username")) {
                                inputEmailLogin.error = errorResponse.getJSONArray("username").getString(0)
                            }
                            if (errorResponse.has("non_field_errors")) {
                                inputEmailLogin.error = "Either the username or the password is incorrect"
                            }
                        }
                        Log.v("Error", "Login")
                        onLoginFailed()
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                //TODO If we need to implement any other activity or a transition on sign up this is where we
                //would do that
                this.finish()
            }
        }
    }


    private fun validate(email: String, password: String): Boolean {
        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmailLogin.error = "enter a valid email address"
            valid = false
        } else {
            inputEmailLogin.error = null
        }

        if (password.isEmpty() || password.length < 4 ) {
            inputPasswordLogin.error = "Password must be at least 8 characters"
            valid = false
        } else {
            inputPasswordLogin.error = null
        }

        return valid
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        //moveTaskToBack(true)
        finishAffinity()
    }

    fun onLoginSuccess() {
        val userDataChange = Intent(Constants.BROADCAST_USER_DATA_CHANGE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
        btnLogin.isEnabled = true
        finish()
    }

    fun onLoginFailed() {
        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()

        btnLogin.isEnabled = true
    }
}
