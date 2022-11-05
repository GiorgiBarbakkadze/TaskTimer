package giorgibarbakadze.example.tasktimer

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import giorgibarbakadze.example.tasktimer.debug.TestData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_first.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked, FirstFragment.OnTaskEdit, AppDialog.DialogEvents{

    // whether or the activity is in 2-pane mode
    // i.e running in a landscape, or in tablet
    private var mTwoPane = false

    //module scope because we need to dismiss it in onStop(e.g when orientation changes) to avoid memory leaks
    private var aboutDialog: AlertDialog? = null

    private val viewModel: TaskTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onCreate() -> Started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val fragment = findFragmentById(R.id.task_details_containter)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sire the panes are set correctly
            showEditPane()
        } else {
            task_details_containter.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            //hide a left hand pane if in single pane view
            mainfragment.visibility = View.VISIBLE
        }

        viewModel.timing.observe(this) { timing ->
            current_task.text = if (timing != null) {
                getString(R.string.timing_message, timing)
            } else {
                getString(R.string.no_task_message)
            }
        }
        Log.d(TAG, ".onCreate() -> Finished")
    }
    private fun showEditPane(){
        task_details_containter.visibility = View.VISIBLE
        mainfragment.visibility = if (mTwoPane) View.VISIBLE else View.GONE

    }

    private fun removeEditPane(fragment: Fragment? = null){
        Log.d(TAG, "RemoveEditPane() called")
        if (fragment != null) {
            removeFragment(fragment)
        }
        //set the visibility of the right hand pane
         task_details_containter.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainfragment.visibility = View.VISIBLE
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, ".onSaveClicked() -> called")
        removeEditPane(findFragmentById(R.id.task_details_containter))
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (BuildConfig.DEBUG) {
            val generate = menu.findItem(R.id.menumain_generate)
            generate.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            R.id.menumain_showAbout -> showAboutDialog()
            android.R.id.home -> {
                Log.d(TAG, ".onOptionsItemSelected(): Home button pressed")
                val fragment = findFragmentById(R.id.task_details_containter)
//                removeEditPane(fragment)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negtive_caption)
                } else {
                    removeEditPane(fragment)
                }
            }
            R.id.menumain_generate -> TestData.generateTestData(contentResolver)
            R.id.menumain_showDurations -> startActivity(Intent(this, DurationsReport::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            if (aboutDialog != null && aboutDialog?.isShowing == true){
                aboutDialog?.dismiss()
            }
        }
        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)


        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME

        aboutDialog?.show()
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, ".taskEditRequest: Starts")

        // Create a new fragment to edit a task


        replaceFragment(AddEditFragment.newInstance(task), R.id.task_details_containter)
        showEditPane()

        Log.d(TAG, "Exiting .taskEditRequest()")
    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.task_details_containter)
        if (fragment == null || mTwoPane){
            super.onBackPressed()
        } else {
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negtive_caption)
            } else {
                removeEditPane(fragment)
            }
            removeEditPane(fragment)
        }

    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult() called with dialogId -> $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT){
            val fragment = findFragmentById(R.id.task_details_containter)
            removeEditPane(fragment)
        }
    }
    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState: called")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: called")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }



}