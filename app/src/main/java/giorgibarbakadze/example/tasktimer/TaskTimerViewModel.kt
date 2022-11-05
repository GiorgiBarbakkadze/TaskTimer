package giorgibarbakadze.example.tasktimer

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.SharedPreferences
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = " TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange() -> called. uri is $uri")
            loadTasks()
        }
    }

    private val settings = PreferenceManager.getDefaultSharedPreferences(application)
    private var ignore = settings.getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)

    private val sharedPrefObserve = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
        when (s) {
            SETTINGS_IGNORE_LESS_THAN -> {
                ignore = sharedPreferences.getInt(
                    s,
                    SETTINGS_DEFAULT_IGNORE_LESS_THAN
                )

            }
        }
    }

    private var currentTiming: Timing? = null
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String>
    get() = taskTiming

    init {
        Log.d(TAG, "init -> started")
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true,
            contentObserver
        )

        settings.registerOnSharedPreferenceChangeListener(sharedPrefObserve)
        currentTiming = retrieveTiming()
        loadTasks()
//        getLastDuration()
    }




    private fun loadTasks() {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER
        )

        // <order by> Tasks.SortOrder, Tasks.Name

        val sortOrder =
            "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"
        viewModelScope.launch {
            val cursor = getApplication<Application>().contentResolver?.query(
                TasksContract.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )
            databaseCursor.postValue(cursor!!)
        }
    }

    fun saveTask(task: Task): Task {
        val contentValues = ContentValues()
        if (task.name.isNotEmpty()) {
            // do not save data if task name is empty
            contentValues.put(TasksContract.Columns.TASK_NAME, task.name)
            contentValues.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            contentValues.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if (task.id == 0L) {
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d(TAG, ".saveTask() -> adding new task.")
                    val uri = getApplication<Application>().contentResolver?.insert(
                        TasksContract.CONTENT_URI,
                        contentValues
                    )
                    if (uri != null) {
                        task.id = TasksContract.getId(uri)
                        Log.d(TAG, ".saveTask() -> new id is ${task.id}")
                    }
                }

            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d(TAG, ".saveTask() -> updating task.")
                    getApplication<Application>().contentResolver?.update(
                        TasksContract.buildUriFromId(
                            task.id
                        ), contentValues, null, null
                    )
                }
            }
        }
        return task
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().contentResolver?.delete(
                TasksContract.buildUriFromId(
                    taskId
                ), null, null
            )

        }
    }

    fun timeTask (task: Task) {
        Log.d(TAG, "timeTask() called")
        // Use the local variable, to allow smart cast
        val timingRecord = currentTiming

        if (timingRecord == null) {
            // no task being timed, start timing the new task
            currentTiming = Timing(task.id)
                              saveTiming(currentTiming!!)
        } else {
            // We have  a task being timed, so save it
            timingRecord.setDuration()
                        saveTiming(timingRecord)

            if (task.id == timingRecord.taskId) {
                // the current task was tapped a second time, stop timing
                currentTiming = null
            } else {
                // a new task is being timed
                currentTiming = Timing(task.id)
                              saveTiming(currentTiming!!)
            }
        }

        //update the livedata
        taskTiming.value = if (currentTiming != null) task.name else null
    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, "saveTiming(): called")

        // are we updating, or inserting a new row?
        val inserting = (currentTiming.duration == 0L)

        val values = ContentValues().apply {
            if(inserting) {
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
                put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
            } else {
                put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (inserting) {
                val uri = getApplication<Application>().contentResolver?.insert(TimingsContract.CONTENT_URI, values)

                if (uri != null) {
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else {
                if (currentTiming.duration >= ignore) {
                    getApplication<Application>().contentResolver?.update(
                        TimingsContract.buildUriFromId(
                            currentTiming.id
                        ), values, null, null
                    )
                } else {
                    getApplication<Application>().contentResolver?.delete(TimingsContract.buildUriFromId(currentTiming.id), null, null)
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun retrieveTiming(): Timing? {
        val timing: Timing?

        val timingCursor = getApplication<Application>().contentResolver?.query(
            CurrentTimingContract.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (timingCursor != null && timingCursor.moveToNext()) {
            // we have untimed record
            val id = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TIMINGS_ID))
            val taskId = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_ID))
            val startTime = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TIMINGS_START_TIME))
            val name = timingCursor.getString(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASKS_NAME))
            timing = Timing(taskId, startTime, id)

            // update the livedata
            taskTiming.value = name
        } else {
            // no timing record found with zero duration
            timing = null
        }
        timingCursor?.close()
        return timing
    }

    override fun onCleared() {
        Log.d(TAG, ".onCleared() -> called ")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
        settings.unregisterOnSharedPreferenceChangeListener(sharedPrefObserve)
    }
}