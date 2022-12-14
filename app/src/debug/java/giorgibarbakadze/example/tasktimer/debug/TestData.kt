package giorgibarbakadze.example.tasktimer.debug

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import giorgibarbakadze.example.tasktimer.Task
import giorgibarbakadze.example.tasktimer.TasksContract
import giorgibarbakadze.example.tasktimer.Timing
import giorgibarbakadze.example.tasktimer.TimingsContract
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

internal class TestTiming internal constructor(var taskId: Long, date: Long, var duration: Long) {
    var startTime : Long = 0

    init {
        this.startTime = date / 1000
    }
}
object TestData {
    private const val SECS_IN_DAY = 86400
    private const val LOWER_BOUND = 100
    private const val UPPER_BOUND = 500
    private const val MAX_DURATION = SECS_IN_DAY / 6
    @SuppressLint("Range")
    fun generateTestData(contentResolver: ContentResolver) {
        val projection = arrayOf(TasksContract.Columns.ID)
        val cursor = contentResolver.query(TasksContract.CONTENT_URI, projection, null, null, null)
        if (cursor != null && cursor.moveToNext()) {
            do {
                val taskId = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

                val loopCount = LOWER_BOUND + getRandomInt(UPPER_BOUND - LOWER_BOUND)
                for (i in 0 until loopCount) {

                    //generate a random data/time
                    val randomDate = getRandomDateTime()

                    //generate a random duration between 0 and 4 hours
                    val duration = getRandomInt(MAX_DURATION).toLong()

                    //create a new TestTiming record with our random date and duration
                    val testTiming = TestTiming(taskId, randomDate, duration)

                    // and add it to the database
                    saveCurrentTiming(contentResolver, testTiming)
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
    }

    private fun getRandomInt(max: Int): Int {
        return Math.round(Math.random() * max).toInt()
    }

    private fun getRandomDateTime(): Long {
        val startYear = 2022
        val endYear = 2021

        val sec = getRandomInt(59)
        val min = getRandomInt(59)
        val hour = getRandomInt(23)
        val month = getRandomInt(11)

        val year = startYear + getRandomInt(endYear - startYear)

        val gc = GregorianCalendar(year, month, 1)
        val day = 1 + getRandomInt(gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH - 1))

        gc.set(year, month, day, hour, min, sec)

        return gc.timeInMillis
    }

    private fun saveCurrentTiming(contentResolver: ContentResolver, currentTiming: TestTiming) {
        val values = ContentValues()
        values.put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
        values.put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
        values.put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)

        GlobalScope.launch {
            contentResolver.insert(TimingsContract.CONTENT_URI, values)
        }
    }
}