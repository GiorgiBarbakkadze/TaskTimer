package giorgibarbakadze.example.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException


/** Basic database class for the application.
 * The only class that should use this is [AppProvider].
 */
private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 4
internal class AppDatabase private constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    init {
        Log.d(TAG, "AppDatabase: initializing")
    }

    override fun onCreate(db: SQLiteDatabase) {
        // CREATE TABLE Tasks (_id INTEGER PRIMARY KER NOT NULL, Name TEXT NOT NULL, Description TEXT, SortOrder INTEGER);
        Log.d(TAG, "onCreate: starts")
        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESCRIPTION} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_SORT_ORDER} TEXT NOT NULL);""".trimMargin()
        Log.d(TAG, sSQL)
        db.execSQL(sSQL)

        addTimingsTable(db)
        addCurrentTimingView(db)
        addDurationsView(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, ".onUpgrade: starts")
        when (oldVersion) {
            1 -> {
                addTimingsTable(db)
                addCurrentTimingView(db)
                addDurationsView(db)
            }
            2 -> {
                addCurrentTimingView(db)
                addDurationsView(db)
            }
            3 -> {
                addDurationsView(db)
            }
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion")
        }
    }

    private fun addTimingsTable(db: SQLiteDatabase) {
        val sSQLTiming = """CREATE TABLE ${TimingsContract.TABLE_NAME} (
            ${TimingsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TimingsContract.Columns.TIMING_TASK_ID} INTEGER NOT NULL,
            ${TimingsContract.Columns.TIMING_START_TIME} INTEGER,
            ${TimingsContract.Columns.TIMING_DURATION} INTEGER);""".trimMargin()
        Log.d(TAG, sSQLTiming)
        db.execSQL(sSQLTiming)

        val sSQLTrigger = """CREATE TRIGGER Remove_Task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN
            DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TasksContract.Columns.ID} = ${TimingsContract.Columns.TIMING_TASK_ID};
            END;""".trimMargin()
        Log.d(TAG, sSQLTrigger)
        db.execSQL(sSQLTrigger)
    }

    private fun addCurrentTimingView(db: SQLiteDatabase) {
        val sSQLvwCurrentTiming = """CREATE VIEW ${CurrentTimingContract.VIEW_NAME}
        AS SELECT ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME}
        FROM ${TimingsContract.TABLE_NAME}
        JOIN ${TasksContract.TABLE_NAME}
        ON ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID} = ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}
        WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION} = 0
        ORDER BY ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME} DESC;"""
        Log.d(TAG, sSQLvwCurrentTiming)
        db.execSQL(sSQLvwCurrentTiming)
    }

    private fun addDurationsView(db: SQLiteDatabase) {
//        CREATE VIEW vwTaskDurations AS
//                SELECT Tasks.Name,
//        Tasks.Description,
//        Timings.StartTime,
//        DATE(Timings.StartTime, 'unixepoch', 'localtime') AS StartDate,
//        SUM(Timings.Duration) AS Duration
//        FROM Tasks INNER JOIN Timings
//        ON Tasks._id = Timings.TaskId
//                GROUP BY Tasks._id, StartDate;

        val sSQL = """CREATE VIEW ${DurationsContract.TABLE_NAME}
                AS SELECT ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME},
                ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_DESCRIPTION},
                ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
                DATE(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME}, 'unixepoch', 'localtime')
                AS ${DurationsContract.Columns.START_DATE},
                SUM(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION})
                AS ${DurationsContract.Columns.DURATION}
                FROM ${TasksContract.TABLE_NAME} INNER JOIN ${TimingsContract.TABLE_NAME}
                ON ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID} =
                ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID}
                GROUP BY ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}, ${DurationsContract.Columns.START_DATE}
                ;""".trimMargin()
        Log.d(TAG, sSQL)
        db.execSQL(sSQL)
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)

//    companion object {
//        @Volatile
//        private var instance: AppDatabase? = null
//
//        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
//            instance ?: AppDatabase(context).also { instance = it }
//        }
//    }
}