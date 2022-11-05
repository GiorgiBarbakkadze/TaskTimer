package giorgibarbakadze.example.tasktimer

import android.net.Uri

object DurationsContract {
    internal const val TABLE_NAME = "vwTaskDurations"

    val CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.${TABLE_NAME}"

    object Columns {
        const val TASK_NAME = TasksContract.Columns.TASK_NAME
        const val TASK_DESCRIPTION = TasksContract.Columns.TASK_DESCRIPTION
        const val START_TIME = TimingsContract.Columns.TIMING_START_TIME
        const val START_DATE = "StartDate"
        const val DURATION = TimingsContract.Columns.TIMING_DURATION
    }
}