package com.aireceptionist.app.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aireceptionist.app.R
import com.aireceptionist.app.data.models.CallRecord
import com.aireceptionist.app.data.models.CallType
import com.aireceptionist.app.data.models.CallResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for call history display
 */
class CallHistoryAdapter(
    private val onCallClick: (CallRecord) -> Unit
) : ListAdapter<CallRecord, CallHistoryAdapter.CallHistoryViewHolder>(CallHistoryDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_history, parent, false)
        return CallHistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class CallHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconCall: ImageView = itemView.findViewById(R.id.iconCall)
        private val textCallerName: TextView = itemView.findViewById(R.id.textCallerName)
        private val textCallerNumber: TextView = itemView.findViewById(R.id.textCallerNumber)
        private val textCallTime: TextView = itemView.findViewById(R.id.textCallTime)
        private val textCallDuration: TextView = itemView.findViewById(R.id.textCallDuration)
        private val textCallResult: TextView = itemView.findViewById(R.id.textCallResult)
        private val iconResult: ImageView = itemView.findViewById(R.id.iconResult)
        
        fun bind(callRecord: CallRecord) {
            // Set caller information
            textCallerName.text = callRecord.callerName ?: "Unknown Caller"
            textCallerNumber.text = callRecord.callerNumber ?: "Unknown Number"
            
            // Set call time
            textCallTime.text = dateFormat.format(Date(callRecord.startTime))
            
            // Set call duration
            val durationMinutes = callRecord.duration / 60000 // Convert to minutes
            val durationSeconds = (callRecord.duration % 60000) / 1000 // Remaining seconds
            textCallDuration.text = String.format("%02d:%02d", durationMinutes, durationSeconds)
            
            // Set call type icon
            when (callRecord.callType) {
                CallType.INCOMING -> {
                    iconCall.setImageResource(R.drawable.ic_call_received)
                    iconCall.setColorFilter(getColor(R.color.green_500))
                }
                CallType.OUTGOING -> {
                    iconCall.setImageResource(R.drawable.ic_call_made)
                    iconCall.setColorFilter(getColor(R.color.blue_500))
                }
                CallType.INTERNAL -> {
                    iconCall.setImageResource(R.drawable.ic_call)
                    iconCall.setColorFilter(getColor(R.color.orange_500))
                }
                CallType.CONFERENCE -> {
                    iconCall.setImageResource(R.drawable.ic_group_call)
                    iconCall.setColorFilter(getColor(R.color.purple_500))
                }
            }
            
            // Set call result
            textCallResult.text = getCallResultText(callRecord.callResult)
            
            // Set result icon and color
            when (callRecord.callResult) {
                CallResult.COMPLETED -> {
                    iconResult.setImageResource(R.drawable.ic_check_circle)
                    iconResult.setColorFilter(getColor(R.color.green_500))
                }
                CallResult.TRANSFERRED_TO_HUMAN -> {
                    iconResult.setImageResource(R.drawable.ic_person)
                    iconResult.setColorFilter(getColor(R.color.orange_500))
                }
                CallResult.DROPPED -> {
                    iconResult.setImageResource(R.drawable.ic_call_end)
                    iconResult.setColorFilter(getColor(R.color.red_500))
                }
                CallResult.BUSY -> {
                    iconResult.setImageResource(R.drawable.ic_busy)
                    iconResult.setColorFilter(getColor(R.color.yellow_700))
                }
                CallResult.NO_ANSWER -> {
                    iconResult.setImageResource(R.drawable.ic_no_answer)
                    iconResult.setColorFilter(getColor(R.color.grey_500))
                }
                CallResult.FAILED -> {
                    iconResult.setImageResource(R.drawable.ic_error)
                    iconResult.setColorFilter(getColor(R.color.red_500))
                }
                CallResult.VOICEMAIL -> {
                    iconResult.setImageResource(R.drawable.ic_voicemail)
                    iconResult.setColorFilter(getColor(R.color.blue_500))
                }
            }
            
            // Set satisfaction score background color if available
            callRecord.satisfactionScore?.let { score ->
                val backgroundColor = when {
                    score >= 4.0f -> R.color.green_100
                    score >= 3.0f -> R.color.yellow_100
                    else -> R.color.red_100
                }
                itemView.setBackgroundResource(backgroundColor)
            }
            
            // Set click listener
            itemView.setOnClickListener { onCallClick(callRecord) }
        }
        
        private fun getCallResultText(result: CallResult): String {
            return when (result) {
                CallResult.COMPLETED -> "Completed"
                CallResult.TRANSFERRED_TO_HUMAN -> "Transferred"
                CallResult.DROPPED -> "Dropped"
                CallResult.BUSY -> "Busy"
                CallResult.NO_ANSWER -> "No Answer"
                CallResult.FAILED -> "Failed"
                CallResult.VOICEMAIL -> "Voicemail"
            }
        }
        
        private fun getColor(colorRes: Int): Int {
            return itemView.context.getColor(colorRes)
        }
    }
    
    class CallHistoryDiffCallback : DiffUtil.ItemCallback<CallRecord>() {
        override fun areItemsTheSame(oldItem: CallRecord, newItem: CallRecord): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: CallRecord, newItem: CallRecord): Boolean {
            return oldItem == newItem
        }
    }
}