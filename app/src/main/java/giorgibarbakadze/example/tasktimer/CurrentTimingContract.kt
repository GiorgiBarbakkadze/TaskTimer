package giorgibarbakadze.example.tasktimer

import android.content.ContentUris
import android.net.Uri

object CurrentTimingContract {
    internal const val VIEW_NAME = "vwCurrentTiming"

    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, VIEW_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$VIEW_NAME"

    object Columns {
        const val TIMINGS_ID = TimingsContract.Columns.ID
        const val TASK_ID = TimingsContract.Columns.TIMING_TASK_ID
        const val TIMINGS_START_TIME = TimingsContract.Columns.TIMING_START_TIME
        const val TASKS_NAME = TasksContract.Columns.TASK_NAME
    }
}