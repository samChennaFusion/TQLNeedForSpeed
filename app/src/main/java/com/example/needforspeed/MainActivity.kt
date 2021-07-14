package com.example.needforspeed

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.needforspeed.databinding.ActivityMainBinding
import com.tql.models.bookItNow.LeaderBoard
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), SensorEventListener {

    var running = false
    var sensorManager: SensorManager? = null
    private lateinit var binding: ActivityMainBinding
    private var adapter: LeaderBoardAdapter? = null
    private var leaderBoardList = ArrayList<LeaderBoard>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        binding.circularProgressBar.progressMax = 1500f
        binding.circularProgressBar.setProgressWithAnimation(500f, 1000, AccelerateInterpolator())
        val calendar: Calendar = Calendar.getInstance()

        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
            Calendar.HOUR_OF_DAY, Calendar.MINUTE + 1, 0);
        setAlarm(calendar.timeInMillis)


        leaderBoardList = ArrayList()
        val l1 = LeaderBoard().apply {
            name = "joe"
            stepCount = "2000"
        }
        val l2 = LeaderBoard().apply {
            name = "Scott"
            stepCount = "2400"
        }
        val l3 = LeaderBoard().apply {
            name = "Sandeep"
            stepCount = "1600"
        }
        leaderBoardList.add(l1)
        leaderBoardList.add(l2)
        leaderBoardList.add(l3)
        adapter = LeaderBoardAdapter(leaderBoardList)
        binding.rv.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        running = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            binding.todayStepsCount.text = "" + event.values[0]
        }
    }

    private fun setAlarm(time: Long) { //getting the alarm manager
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 18
        calendar[Calendar.MINUTE] = 26
        calendar[Calendar.SECOND] = 0

        if (calendar.time.compareTo(Date()) < 0) calendar.add(Calendar.DAY_OF_MONTH, 1)
        val intent = Intent(applicationContext, SyncReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    internal inner class LeaderBoardAdapter(private val leaderBoardList: ArrayList<LeaderBoard>) : RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.single_item_leader_board, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.name.text = leaderBoardList[position].name
            viewHolder.stepCount.text = leaderBoardList[position].stepCount
        }

        override fun getItemCount(): Int {
            return leaderBoardList.size
        }
        inner class ViewHolder(rowView: View) : RecyclerView.ViewHolder(rowView) {
            var image: ImageView = rowView.findViewById<View>(R.id.image) as ImageView
            var name: TextView = rowView.findViewById<View>(R.id.tv_name) as TextView
            var stepCount: TextView = rowView.findViewById<View>(R.id.tv_stepsCount) as TextView
        }
    }

}