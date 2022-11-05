package giorgibarbakadze.example.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import android.text.format.DateFormat
import android.util.Log
import kotlinx.android.synthetic.main.task_duration_items.*
import java.lang.IllegalStateException
import java.util.*

class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
    LayoutContainer
class DurationsRVAdapter(context: Context, private var cursor: Cursor?): RecyclerView.Adapter<ViewHolder>() {
    private val dateFormat = DateFormat.getDateFormat(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("RR", "DurationsRVADAPTER called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_duration_items, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursor

        if (cursor != null && cursor.moveToNext()) {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }
            val name = cursor.getString(cursor.getColumnIndex(DurationsContract.Columns.TASK_NAME))
//            val description = cursor.getString(cursor.getColumnIndex(DurationsContract.Columns.TASK_DESCRIPTION))
            val startTime = cursor.getLong(cursor.getColumnIndex(DurationsContract.Columns.START_TIME))
            val totalDuration = cursor.getLong(cursor.getColumnIndex(DurationsContract.Columns.DURATION))

            val userDate = dateFormat.format(startTime * 1000) // database stores seconds, we need milliseconds

            val totalTime = formatDuration(totalDuration)

            holder.td_name.text = name
            holder.td_start.text = userDate
            holder.td_duration.text = totalTime

        }
    }

    private fun formatDuration(duration: Long): String {
        // duration is in seconds, convert to hours:minutes:seconds
        // (allowing for >24 hours - so we can't use a time data type

        val hours = duration / 3600
        val remainder = duration - hours * 3600
        val minutes = remainder / 60
        //  val seconds = remainder - minutes * 60
        val seconds = remainder % 60

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }
    fun swapCursor(newCursor: Cursor?): Cursor?{
        if (newCursor == cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of database
            notifyItemRangeChanged(0, numItems)
        }
        return oldCursor
    }
}