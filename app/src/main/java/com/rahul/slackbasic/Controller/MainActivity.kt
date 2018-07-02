package com.rahul.slackbasic.Controller

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.google.gson.internal.bind.ArrayTypeAdapter
import com.rahul.slackbasic.Adapters.MessageAdapter
import com.rahul.slackbasic.Models.Channel
import com.rahul.slackbasic.Models.Message
import com.rahul.slackbasic.R
import com.rahul.slackbasic.Services.AuthService
import com.rahul.slackbasic.Services.MessageService
import com.rahul.slackbasic.Utilities.Constants
import com.rahul.slackbasic.Services.UserDataService
import com.rahul.slackbasic.Utilities.MyPreferences
import com.rahul.slackbasic.Utilities.MyPreferences.get
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {


    val socket = IO.socket(Constants.SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter

    var selectedChannel : Channel? = null
    var selectedChannelPosition = 0

    private val userDataChangeReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            displayUserData()
        }
    }

    private fun setUpAdapters(){
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        channel_list.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            channel_list.getChildAt(selectedChannelPosition).setBackgroundColor(Color.rgb(255, 255, 255))
            selectedChannelPosition = i
            Log.v("Selected", "$i")
            updateWithChannel()
            channel_list.getChildAt(selectedChannelPosition).setBackgroundColor(Color.rgb(250, 218, 94))
        }


        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter

        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)



        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setUpAdapters()

        AuthService.setUserData(this)
        //If its the default token it means we need to launch the login activity
        if(AuthService.isLoggedIn){
            mainChannelName.text = "Loading channels"
            displayUserData()
        }else {
            LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
                    IntentFilter(Constants.BROADCAST_USER_DATA_CHANGE))
           startLogin()
        }

        socket.connect()
        socket.on("channelCreated", onNewChannel )
        socket.on("messageCreated", onNewMessage )

    }


    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
        super.onDestroy()
    }

    fun displayUserData(){

        userNameNavHeader.text = UserDataService.name
        userEmailNavHeader.text = UserDataService.email
        var resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
        userImageNavHeader.setImageResource(resourceId)
        userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor,255))

        if(AuthService.isLoggedIn){
            MessageService.channels.clear()
            MessageService.getChannels(this){complete ->
                if(complete){
                    if(MessageService.channels.count() > 0){
                        selectedChannel = MessageService.channels[0]
                        channelAdapter.notifyDataSetChanged()
                        updateWithChannel()
                    } else{
                        mainChannelName.text = "Please select a channel"
                    }

                }
            }
            loginBtnNavHeader.text = "Logout"
        } else{

            mainChannelName.text = "Please Log in"
            loginBtnNavHeader.text = "Login"
        }
    }


    fun updateWithChannel() {
        mainChannelName.text = selectedChannel.toString()

        if(selectedChannel != null){
            MessageService.getMessages(selectedChannel!!.id){complete ->
                if(complete){
                    messageAdapter.notifyDataSetChanged()
                    if(messageAdapter.itemCount > 0){
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
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
            messageAdapter.notifyDataSetChanged()
            channelAdapter.notifyDataSetChanged()
            displayUserData()

        }else {
            LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
                    IntentFilter(Constants.BROADCAST_USER_DATA_CHANGE))
            startLogin()
        }

    }

    fun addChannelClicked(view: View) {
        if(AuthService.isLoggedIn){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                    .setPositiveButton("Add"){dialogInterface, i ->
                        val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                        val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescriptionText)

                        val channelName = nameTextField.text.toString()
                        val channelDescription = descTextField.text.toString()

                        if(channelName.isNullOrEmpty() || channelDescription.isNullOrEmpty()){
                            Toast.makeText(this, "Some of the fields were blank. Adding channel cancelled", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            socket.emit("newChannel",channelName, channelDescription)
                        }

                    }
                    .setNegativeButton("Cancel"){dialogInterface, i ->
                        //cancel and close the dialog

                    }
                    .show()
        }
    }

    private val onNewChannel = Emitter.Listener{ args ->

        if(AuthService.isLoggedIn){
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()

            }
        }


    }


    private val onNewMessage = Emitter.Listener { args ->

        if(AuthService.isLoggedIn){
            runOnUiThread {
                val channelID = args[2] as String
                if(channelID == selectedChannel?.id){
                    val messageBody = args[0] as String

                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(messageBody, userName, channelID, userAvatar, userAvatarColor, id, timeStamp)

                    MessageService.messages.add(newMessage)

                    messageAdapter.notifyDataSetChanged()
                    if(messageAdapter.itemCount > 0){
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }

    }

    fun sendMessageBtnClicked(view: View){
        if(AuthService.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId
                    , UserDataService.name
                    , UserDataService.avatarName
                    , UserDataService.avatarColor)

            messageTextField.text.clear()
        }
        hideKeyboard()
    }


    fun startLogin(){
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
