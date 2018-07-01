package com.rahul.slackbasic.Controller

import android.content.*
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.rahul.slackbasic.R
import com.rahul.slackbasic.Services.AuthService
import com.rahul.slackbasic.Utilities.Constants
import com.rahul.slackbasic.Services.UserDataService
import com.rahul.slackbasic.Utilities.MyPreferences
import com.rahul.slackbasic.Utilities.MyPreferences.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {


    lateinit var prefs: SharedPreferences

    private val userDataChangeReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            displayUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
                IntentFilter(Constants.BROADCAST_USER_DATA_CHANGE))

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        UserDataService.name = prefs[Constants.KEY_NAME, Constants.NAME_DEFAULT]
        UserDataService.email = prefs[Constants.KEY_EMAIL, Constants.EMAIL_DEFAULT]
        UserDataService.avatarName = prefs[Constants.KEY_AVATAR_NAME, Constants.AVATAR_NAME_DEFAULT]
        UserDataService.avatarColor = prefs[Constants.KEY_AVATAR_BG, Constants.AVATAR_BG_DEFAULT]
        UserDataService.id = prefs[Constants.KEY_ID, Constants.ID_DEFAULT]
        UserDataService.token = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
        Log.v("${UserDataService.token}", "${UserDataService.avatarColor}")
        AuthService.isLoggedIn = UserDataService.token != Constants.TOKEN_DEFAULT
        //If its the default token it means we need to launch the login activity
        if(AuthService.isLoggedIn){
            displayUserData()
        }

        else {
           startLogin()
        }
    }


    fun displayUserData(){
        if(AuthService.isLoggedIn){
            userNameNavHeader.text = UserDataService.name
            userEmailNavHeader.text = UserDataService.email
            var resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
            userImageNavHeader.setImageResource(resourceId)
            userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
            loginBtnNavHeader.text = "Logout"
        } else{
            userNameNavHeader.text = UserDataService.name
            userEmailNavHeader.text = UserDataService.email
            var resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
            userImageNavHeader.setImageResource(resourceId)
            userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
            loginBtnNavHeader.text = "Login"
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNavClicked(view: View){
        if(AuthService.isLoggedIn){
            AuthService.Logout(this)
            displayUserData()

        }else {
            startLogin()
        }

    }

    fun addChannelClicked(view: View) {

    }

    fun sendMessageBtnClicked(view: View){

    }


    fun startLogin(){
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }
}
