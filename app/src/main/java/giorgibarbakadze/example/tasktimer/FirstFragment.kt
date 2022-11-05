package giorgibarbakadze.example.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_first.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */

private const val TAG = "MainActivityFragment"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "dialog_id"
class FirstFragment : Fragment(), CursorRecyclerViewAdapter.OnTaskClickListener, AppDialog.DialogEvents {
    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }
    private val viewModel: TaskTimerViewModel by activityViewModels()
    private val mAdapter = CursorRecyclerViewAdapter(null, this)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, ".onCreateView() -> called.")
        return inflater.inflate(R.layout.fragment_first, container, false)

    }
    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)
        if (context !is OnTaskEdit){
            throw RuntimeException ("${context.toString()} must implement interface")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this) { cursor -> mAdapter.swapCursor(cursor)?.close() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)
        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = mAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: called")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: called")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
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
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: called")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: called")
        super.onDetach()
    }

    override fun onEditClick(task: Task) {
        Log.d(TAG, "\n.onEditClick() -> name: ${task.name}" +
                "\ndesc: ${task.description}" +
                "\nid: ${task.id}")
        (activity as OnTaskEdit).onTaskEdit(task)

    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.deldialog_message, task.id, task.name))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID, task.id)
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        Log.d(TAG, "onTaskLongClick(): called")
        viewModel.timeTask(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult() called")
        if (dialogId == DIALOG_ID_DELETE){
            val taskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task id is zero")
            viewModel.deleteTask(taskId)
        }

    }


}