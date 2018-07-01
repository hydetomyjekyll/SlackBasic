package com.rahul.slackbasic.Controller

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
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
import com.androidnetworking.interfaces.StringRequestListener
import com.rahul.slackbasic.R
import com.rahul.slackbasic.Services.AuthService
import com.rahul.slackbasic.Utilities.Constants
import com.rahul.slackbasic.Utilities.MyPreferences
import com.rahul.slackbasic.Utilities.MyPreferences.set
import com.rahul.slackbasic.Services.UserDataService
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        createUserBtn.setOnClickListener {
            signup()
        }

        tvLinkLogin.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
        }
    }


    private fun signup() {
        val username = inputUsernameSignup.text.toString()
        val email = inputEmailSignup.text.toString()

        val password = inputPasswordSignup.text.toString()

        if (!validate(username, email, password)) {
            onSignupFailed()
            return
        }

        createUserBtn.isEnabled = false

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()


        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        AndroidNetworking.post(Constants.URL_REGISTER)
                .addByteBody(requestBody.toByteArray())
                .setContentType("application/json; charset=utf-8")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        progressDialog.dismiss()
                        login(email, password, username)
                    }


                    override fun onError(error: ANError) {
                        if (error.errorCode != 0) {
                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("username")) {
                                inputUsernameSignup.error = errorResponse.getJSONArray("username").getString(0)
                            }

                            if (errorResponse.has("email")) {
                                inputEmailSignup.error = errorResponse.getJSONArray("email").getString(0)
                            }
                        }
                        // handle error
                        Log.v("Error", "Signup" + error.errorBody)
                        onSignupFailed()
                        progressDialog.dismiss()
                    }
                })

    }


    private fun login(email: String, password: String, username: String) {

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating..")
        progressDialog.show()


        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        AndroidNetworking.post(Constants.URL_LOGIN)
                .addByteBody(requestBody.toByteArray())
                .setContentType("application/json; charset=utf-8")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {

                    override fun onResponse(response: JSONObject) {

                        try {
                            val token = response.getString("token")
                            createUser(username, email, token)
                        } catch (e: Exception) {
                            Log.d("Response", response.toString())
                        }
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
                        Log.v("Error", "Login")
                        onSignupFailed()
                        progressDialog.dismiss()
                    }
                })
    }


    private fun createUser(username: String, email: String, token: String) {
        val jsonBody = JSONObject()
        jsonBody.put("name", username)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", userAvatar)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        AndroidNetworking.post(Constants.URL_CREAT_USER)
                .addByteBody(requestBody.toByteArray())
                .addHeaders("Authorization", "Bearer $token")
                .setContentType("application/json; charset=utf-8")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {

                    override fun onResponse(response: JSONObject) {
                        try{
                            var name = response.getString("name")
                            var email = response.getString("email")
                            var avatarName = response.getString("avatarName")
                            var avatarColor = response.getString("avatarColor")
                            var id = response.getString("_id")
                            AuthService.setPreferences(this@CreateUserActivity, name, email, avatarName, avatarColor, id, token)
                            onSignupSuccess()
                        }
                        catch(e: JSONException){
                            Log.v("Hello", ""+e.localizedMessage)
                        }
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
                        onSignupFailed()
                    }
                })
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        //moveTaskToBack(true)
        finishAffinity()
    }


    fun onSignupSuccess() {
        createUserBtn.isEnabled = true
        setResult(RESULT_OK, null)
        val userDataChange = Intent(Constants.BROADCAST_USER_DATA_CHANGE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
        finish()
    }

    fun onSignupFailed() {
        Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()

        createUserBtn.isEnabled = true
    }

    private fun validate(username: String, email: String, password: String): Boolean {
        var valid = true

        if (username.isEmpty() || username.length < 3) {
            inputUsernameSignup.error = "at least 3 characters"
            valid = false
        } else {
            inputUsernameSignup.error = null
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmailSignup.error = "enter a valid email address"
            valid = false
        } else {
            inputEmailSignup.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            inputPasswordSignup.error = "Password must be at least 8 characters"
            valid = false
        } else {
            inputPasswordSignup.error = null
        }

        return valid
    }

    fun createUserAvatarClicked(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color == 0) {
            userAvatar = "light$avatar"
        } else {
            userAvatar = "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceId)
        generateColorClicked(createAvatarImageView);

    }


    fun generateColorClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }


}
