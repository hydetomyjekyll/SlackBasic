package com.rahul.slackbasic.Services

import android.content.Context
import android.util.Log
import com.rahul.slackbasic.Utilities.*
import com.rahul.slackbasic.Utilities.MyPreferences.get
import com.rahul.slackbasic.Utilities.MyPreferences.set


object AuthService {

    var isLoggedIn = true
    var userEmail = ""
    var authToken = ""


    fun setPreferences(context: Context, username: String, email: String, avatarName: String, avatarColor: String, id: String, token: String){
        val prefs = MyPreferences.customPrefs(context, Constants.MY_SHARED_PREFERENCE)
        prefs[Constants.KEY_TOKEN] = token
        prefs[Constants.KEY_NAME] = username
        prefs[Constants.KEY_EMAIL] = email
        prefs[Constants.KEY_AVATAR_NAME] = avatarName
        prefs[Constants.KEY_AVATAR_BG] = avatarColor
        prefs[Constants.KEY_ID] = id
        isLoggedIn = true
        setUserData(context)


    }

    fun setUserData(context: Context){
        val prefs = MyPreferences.customPrefs(context, Constants.MY_SHARED_PREFERENCE)
        UserDataService.name = prefs[Constants.KEY_NAME, Constants.NAME_DEFAULT]
        UserDataService.email = prefs[Constants.KEY_EMAIL, Constants.EMAIL_DEFAULT]
        UserDataService.avatarName = prefs[Constants.KEY_AVATAR_NAME, Constants.AVATAR_NAME_DEFAULT]
        UserDataService.avatarColor = prefs[Constants.KEY_AVATAR_BG, Constants.AVATAR_BG_DEFAULT]
        UserDataService.id = prefs[Constants.KEY_ID, Constants.ID_DEFAULT]
        UserDataService.token = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
        Log.v("${UserDataService.token}", "${UserDataService.avatarColor}")
        isLoggedIn = UserDataService.token != Constants.TOKEN_DEFAULT
    }

    fun Logout(context: Context){
        val prefs = MyPreferences.customPrefs(context, Constants.MY_SHARED_PREFERENCE)
        prefs[Constants.KEY_TOKEN] = Constants.TOKEN_DEFAULT
        prefs[Constants.KEY_NAME] = Constants.NAME_DEFAULT
        prefs[Constants.KEY_EMAIL] = Constants.EMAIL_DEFAULT
        prefs[Constants.KEY_AVATAR_NAME] = Constants.AVATAR_NAME_DEFAULT
        prefs[Constants.KEY_AVATAR_BG] = Constants.AVATAR_BG_DEFAULT
        prefs[Constants.KEY_ID] = Constants.ID_DEFAULT
        setUserData(context)
        MessageService.clearMessages()
        MessageService.clearChannels()
        isLoggedIn = false
    }

}