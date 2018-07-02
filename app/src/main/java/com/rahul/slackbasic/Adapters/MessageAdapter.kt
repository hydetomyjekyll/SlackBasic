package com.rahul.slackbasic.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.rahul.slackbasic.Models.Message
import com.rahul.slackbasic.R
import com.rahul.slackbasic.Services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(context, messages[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImage = itemView.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView.findViewById<TextView>(R.id.timeStampLabel)
        val userName = itemView.findViewById<TextView>(R.id.messageUserNameLabel)
        val messageBody = itemView.findViewById<TextView>(R.id.messageBodyLabel)
        val completeMessage = itemView

        fun onBind(context: Context, message: Message){
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)
            userImage.setImageResource(resourceId)
            userImage.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor, 255))
            userName.text = message.userName
            timeStamp.text = returnDateString(message.timeStamp)
            messageBody.text = message.message
            completeMessage.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor, 25))
        }

        fun returnDateString(isoString: String) : String {
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")

            var convertedDate = Date()

            try{
                convertedDate = isoFormatter.parse(isoString)
            }catch (e: ParseException){
                Log.v("Error", "Cannot parse date")
            }

            val outDateString = SimpleDateFormat("E, h:mm:a", Locale.getDefault())

            return outDateString.format(convertedDate)
        }
    }
}