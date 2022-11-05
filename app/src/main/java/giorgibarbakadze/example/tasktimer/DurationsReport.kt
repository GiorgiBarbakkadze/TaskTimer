package giorgibarbakadze.example.tasktimer

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_durations_report.*
import kotlinx.android.synthetic.main.task_durations.*
import java.lang.IllegalArgumentException
import android.text.format.DateFormat
import java.util.*

private const val TAG = "DurationsReport"

private const val DIALOG_FILTER = 1
private const val DIALOG_DELETE = 2
private const val DELETION_DATE = "Deletion date"
class DurationsReport : AppCompatActivity(), DatePickerDialog.OnDateSetListener, View.OnClickListener, AppDialog.DialogEvents {

    private val viewModel: DurationsViewModel by viewModels()
    private val reportAdapter by lazy { DurationsRVAdapter(this, null)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_durations_report)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeButtonEnabled(true)

        td_list.layoutManager = LinearLayoutManager(this)
        td_list.adapter = reportAdapter

        viewModel.cursor.observe(this) { cursor -> reportAdapter.swapCursor(cursor)?.close() }
        td_name_heading.setOnClickListener(this)
        td_start_heading.setOnClickListener(this)
        td_duration_heading.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.td_name_heading -> viewModel.sortOrder = SortColumns.NAME
            R.id.td_start_heading -> viewModel.sortOrder = SortColumns.START_DATE
            R.id.td_duration_heading -> viewModel.sortOrder = SortColumns.DURATION
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.rm_filter_period -> {
                viewModel.toggleDisplayWeek()
                invalidateOptionsMenu()
                return true
            }
            R.id.rm_filter_date -> {
                showDatePickerDialog( DIALOG_FILTER,getString(R.string.date_dialog_title))
                return true
            }
            R.id.rm_delete -> {
                showDatePickerDialog(DIALOG_DELETE, getString(R.string.date_title_delete))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.rm_filter_period)
        if (item != null) {
            //switch icon and title to represent 7 days or 1 day, as appropriate to the future function of the menu item
            if (viewModel.displayWeek){
                item.setIcon(R.drawable.ic_baseline_filter_1_24)
                item.setTitle(R.string.rm_title_filter_day)
            } else {
                item.setIcon(R.drawable.ic_baseline_filter_7_24)
                item.setTitle(R.string.rm_title_filter_week)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showDatePickerDialog(dialogId: Int, title: String) {
        val dialogFragment = DatePickerFragment()

        val arguments = Bundle()
        arguments.putInt(DATE_PICKER_ID, dialogId)
        arguments.putString(DATE_PICKER_TITLE, title)
        arguments.putSerializable(DATE_PICKER_DATE, viewModel.getFilterDate())
        arguments.putInt(DATE_PICKER_FDOW, viewModel.firstDayOfWeek)
        dialogFragment.arguments = arguments
        dialogFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSet(p0: DatePicker, p1: Int, p2: Int, p3: Int) {
        Log.d(TAG, "onDataset() called")



        val dialogId = p0.tag as Int
        when(dialogId) {
            DIALOG_FILTER -> {
                viewModel.setReportDate(p1, p2, p3)
            }
            DIALOG_DELETE ->{
                // we need to format the date for user's locale
                val cal = GregorianCalendar()
                cal.set(p1, p2, p3, 0, 0, 0)
                val fromDate = DateFormat.getDateFormat(this).format(cal.time)

                val dialog = AppDialog()
                val args = Bundle()
                args.putInt(DIALOG_ID, DIALOG_DELETE) // use the same id value
                args.putString(DIALOG_MESSAGE, getString(R.string.delet_timings_message, fromDate))

                args.putLong(DELETION_DATE, cal.timeInMillis)
                dialog.arguments = args
                dialog.show(supportFragmentManager, null)
            }
            else -> throw IllegalArgumentException("Invalid mode when receiving DatePickerDialog result")
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG,  "onPositiveDialogResult: called with id $dialogId")

        if (dialogId == DIALOG_DELETE) {
            val deleteDate = args.getLong(DELETION_DATE)
            viewModel.deleteRecords(deleteDate)
        }
    }
}