package com.rahul.slackbasic.Utilities

object Constants{
    const val BASE_URL = "https://chattychatrj.herokuapp.com/v1/"

    const val SOCKET_URL = "https://chattychatrj.herokuapp.com/"
    const val URL_REGISTER = "${BASE_URL}account/register"
    const val URL_LOGIN = "${BASE_URL}account/login"
    const val URL_CREAT_USER = "${BASE_URL}user/add"
    const val URL_GET_USER = "${BASE_URL}user/byEmail/"
    const val URL_GET_CHANNELS = "${BASE_URL}channel/"


    const val KEY_TOKEN = "token"
    const val TOKEN_DEFAULT = "0"
    const val KEY_EMAIL = "email"
    const val EMAIL_DEFAULT = ""
    const val KEY_NAME = "name"
    const val NAME_DEFAULT = ""
    const val KEY_AVATAR_NAME = "avatarName"
    const val AVATAR_NAME_DEFAULT = "profiledefault"
    const val KEY_AVATAR_BG = "background"
    const val AVATAR_BG_DEFAULT =  "[0.5, 0.5, 0.5, 1]"
    const val KEY_ID = "_id"
    const val ID_DEFAULT = ""
    const val MY_SHARED_PREFERENCE = "mypreference"




    const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHNAGED"

}

