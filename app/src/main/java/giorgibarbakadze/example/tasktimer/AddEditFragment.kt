package giorgibarbakadze.example.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_add_edit.*
import kotlinx.android.synthetic.main.fragment_add_edit.view.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TAG = "AddEditFragment"
private const val ARG_TASK = "task"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class AddEditFragment : Fragment() {
    private val viewModel: TaskTimerViewModel by activityViewModels()
    private var task: Task? = null
    private var listener: OnSaveClicked? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onCreate: starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, ".onCreateView: starts")
        // Inflate the layout for this fragment
        //fixed some bug
        // a;slkfhasldgkas;dlgkhsa

        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }
    @SuppressLint( "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (activity is AppCompatActivity){
            val actionbar = (activity as AppCompatActivity?)?.supportActionBar
            actionbar?.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null){
            val task = task
            if(task != null){
                Log.d(TAG, ".onViewCreated() -> task details found, editing task ${task.id}")
                addedit_name.setText(task.name)
                addedit_description.setText(task.description)
                addedit_sortorder.setText(task.sortOrder.toString())
            } else {
                //no task so we must be adding a new task and editing an existing one
                Log.d(TAG, ".onViewCreated() -> No arguments found, adding a new record")
            }
        }
        view.addedit_save.setOnClickListener{
            saveTask()
            listener?.onSaveClicked()
        }

    }
    private fun taskFromUi(): Task {
        val sortOrder = if(addedit_sortorder.text.isNotEmpty()){
            Integer.parseInt(addedit_sortorder.text.toString())
        } else {
            0
        }
        val newTask = Task(addedit_name.text.toString(), addedit_description.text.toString(), sortOrder)
        newTask.id = task?.id ?: 0
        return newTask
    }

    fun isDirty(): Boolean {
        val newTask = taskFromUi()
        return ((newTask!=task) &&
                (newTask.name.isNotBlank()
                        || newTask.description.isNotBlank()
                        || newTask.sortOrder != 0)
                )
    }
    private fun saveTask() {
        // Create a newTask object with the details to be saved, then
        // call the viewModel's saveTask function to save it.
        // Task is now a data class, so we can compare the new details with the original task,
        // and only save if they are different.

        val newTask = taskFromUi()
        if (newTask != task){
            Log.d(TAG, ".saveTask() -> saving task, id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG, ".saveTask() -> d is ${task?.id}")

        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, ".onAttach: starts")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement onSaveClicked")
        }

    }

    override fun onDetach() {
        Log.d(TAG, ".onDetach: starts")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task Parameter 1.
         * @return A new instance of fragment AddEditFragment.
         */
        @JvmStatic fun newInstance(task: Task?) =
                AddEditFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TASK, task)
                    }
                }
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

}