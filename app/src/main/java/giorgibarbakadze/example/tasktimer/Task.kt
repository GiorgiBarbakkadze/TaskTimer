package giorgibarbakadze.example.tasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(val name: String, val description: String, var sortOrder: Int, var id: Long = 0): Parcelable