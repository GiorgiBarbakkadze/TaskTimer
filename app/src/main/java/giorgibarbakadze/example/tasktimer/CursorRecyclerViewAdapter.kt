package giorgibarbakadze.example.tasktimer

import android.annotation.SuppressLint
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import giorgibarbakadze.example.tasktimer.databinding.TaskListItemsBinding
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_list_items.*
import java.lang.IllegalStateException

private const val TAG = "CursorRecycler"
class TaskViewHolder(override val containerView: View):
                                RecyclerView.ViewHolder(containerView),
                                LayoutContainer {
        fun bind(task: Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener){
            tli_name.text = task.name
            tli_description.text = task.description
            tli_edit.visibility = View.VISIBLE
            tli_delete.visibility = View.VISIBLE

            tli_edit.setOnClickListener {
                listener.onEditClick(task)
            }
            tli_delete.setOnClickListener {
                listener.onDeleteClick(task)
            }
            containerView.setOnLongClickListener {
                listener.onTaskLongClick(task)
                true
            }
        }
}
class CursorRecyclerViewAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener): RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onTaskLongClick(task: Task)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, ".onCreateViewHolder() -> new view requested")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return TaskViewHolder(view)
    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val cursor = cursor // avoid problems with smart cast
        if (cursor == null || cursor.count == 0){
            holder.tli_name.setText(R.string.instructions_heading)
            holder.tli_description.setText(R.string.instructions)
            holder.tli_edit.visibility = View.GONE
            holder.tli_delete.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)){
                throw IllegalStateException("Could move cursor to position: $position")
            }

            //create a task object from data in the cursor
            val task = Task(
                    cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                    cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER)))
            //remember that the task is not set in the constructor
            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))
            holder.bind(task, listener)
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        val count = if (cursor == null || cursor.count == 0){
            1
        } else {
            cursor.count
        }
        return count
    }

    /**
     * Swap in a new Cursor, returning older Cursor.
     * The returned old cursor is *not* closed
     *
     * @param newCursor The new Cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned.
     */
    fun swapCursor(newCursor: Cursor?): Cursor?{
        if (newCursor == cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of database
            notifyItemRangeChanged(0, numItems)
        }
        return oldCursor
    }
}