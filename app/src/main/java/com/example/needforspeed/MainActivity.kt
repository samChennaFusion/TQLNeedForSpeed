package com.example.needforspeed

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.needforspeed.databinding.ActivityMainBinding
import java.util.*

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
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        binding.circularProgressBar.progressMax = 1500f
        //binding.circularProgressBar.setProgressWithAnimation(500f, 1000, AccelerateInterpolator())

        binding.cardStepCount.setOnClickListener {
            Log.d("MainAc", "onCreate: ${AppDatabase.getInstance(this@MainActivity).userDao().getUserDetails().size}")
        }

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION
            ),
            102
        )

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
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
        setAlermManager()
        //startBackgroundJob()
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

            val totalCount = AppDatabase.getInstance(this@MainActivity).userDao().getUserDetails()
            if (totalCount.isNotEmpty()) {
                val lastDay = totalCount.first()
                val today = event.values[0] - lastDay.steps!!
                binding.circularProgressBar.setProgressWithAnimation(today, 1000, AccelerateInterpolator())
                binding.todayStepsCount.text = "" + today.toInt()
            } else {
                val stepsCount = StepsCount(steps = event!!.values[0].toInt(), date = System.currentTimeMillis().toString())
                AppDatabase.getInstance(this@MainActivity).userDao().insertToRoomDatabase(stepsCount)
                val totalCount = AppDatabase.getInstance(this@MainActivity).userDao().getUserDetails()
                val lastDay = totalCount.first()
                val today = event.values[0] - lastDay.steps!!
                binding.todayStepsCount.text = "" + today.toInt()
                binding.circularProgressBar.setProgressWithAnimation(today, 1000, AccelerateInterpolator())
            }

        }
    }


    private fun setAlermManager() {
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this@MainActivity, SyncReceiver::class.java).let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent.getBroadcast(this@MainActivity, 100, intent, 0)
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = 17 // MIDNIGHT 12 AM
        calendar[Calendar.MINUTE] = 35
        calendar[Calendar.SECOND] = 0

        alarmMgr.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
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