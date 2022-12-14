package giorgibarbakadze.example.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext

/**
 * provider for the TaskTimer app. This is the only lass that knows about [AppDatabase].
 */
private const val TAG = "AppProvider"
const val CONTENT_AUTHORITY = "giorgibarbakadze.example.tasktimer.provider"
private const val TASKS = 100
private const val TASKS_ID = 101
private const val TIMINGS = 200
private const val TIMINGS_ID = 201
private const val CURRENT_DURATION = 300
private const val TASK_DURATIONS = 400


val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher() : UriMatcher {
        Log.d(TAG, "buildUriMatcher: starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)

        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS)
        matcher.addURI(CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#", TIMINGS_ID)
//
        matcher.addURI(CONTENT_AUTHORITY, CurrentTimingContract.VIEW_NAME, CURRENT_DURATION)

        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS)

        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: starts")
        return true
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            TASKS -> TasksContract.CONTENT_TYPE

            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE

            TIMINGS -> TimingsContract.CONTENT_TYPE

            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE

            CURRENT_DURATION -> CurrentTimingContract.CONTENT_TYPE

            TASK_DURATIONS -> DurationsContract.CONTENT_TYPE

            else -> throw IllegalArgumentException("unknown Uri: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        Log.d(TAG, ".query: called with uri --> $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".query: -> matcher $match")

        val queryBuilder = SQLiteQueryBuilder()

        val context = requireContext(this)

        when (match) {
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME

            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")       // <-- change method
                queryBuilder.appendWhereEscapeString("$taskId")       // <-- change method
            }

            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME

            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val timingId = TimingsContract.getId(uri)
                queryBuilder.appendWhere("${TimingsContract.Columns.ID} = ")   // <-- and here
                queryBuilder.appendWhereEscapeString("$timingId")
            }

            CURRENT_DURATION -> queryBuilder.tables = CurrentTimingContract.VIEW_NAME

            TASK_DURATIONS -> queryBuilder.tables = DurationsContract.TABLE_NAME


            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val db = AppDatabase.getInstance(context).readableDatabase
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)

        return cursor
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri {
        Log.d(TAG, ".insert: called with uri --> $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".insert: -> matcher $match")

        val recordId: Long
        val returnURI: Uri
        val context = requireContext(this)

        when(match){
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                if (recordId != -1L){
                    returnURI = TasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed To insert: uri -> $uri")
                }
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values)
                if (recordId != -1L){
                    returnURI = TimingsContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed To insert: uri -> $uri")
                }
            }
            else -> throw java.lang.IllegalArgumentException("Unknown uri: $uri")
        }
        if (recordId > 0) {
            //something was inserted
            Log.d(TAG, ".insert() -> setting notifyChange with $uri")
            context.contentResolver?.notifyChange(uri, null)
        }
        Log.d(TAG, "Exciting insert, returning -> $returnURI")
        return returnURI
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, ".update: called with uri --> $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".update: -> matcher $match")
        val context = requireContext(this)

        val count: Int
        var selectionCriteria: String

        when(match){
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"
                if (selection != null && selection.isNotEmpty()){
                    selectionCriteria += " And ($selection)"
                }
                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"
                if (selection != null && selection.isNotEmpty()){
                    selectionCriteria += " And ($selection)"
                }
                count = db.update(TimingsContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown uri -> $uri")
        }
        if (count > 0){
            //something was updated
            Log.d(TAG, ".update() -> setting notifyingChange with $uri")
            context.contentResolver?.notifyChange(uri, null)
        }
        Log.d(TAG, "Exiting update returning $count")
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, ".delete: called with uri --> $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".delete: -> matcher $match")
        val context = requireContext(this)

        val count: Int
        var selectionCriteria: String

        when(match){
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"
                if (selection != null && selection.isNotEmpty()){
                    selectionCriteria += " And ($selection)"
                }
                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs)
            }
            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"
                if (selection != null && selection.isNotEmpty()){
                    selectionCriteria += " And ($selection)"
                }
                count = db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown uri -> $uri")
        }
        if (count > 0){
            //something was deleted
            Log.d(TAG, ".delete() -> setting notifyingChange with $uri")
            context.contentResolver?.notifyChange(uri, null)
        }
        Log.d(TAG, "Exiting delete returning $count")
        return count
    }
}