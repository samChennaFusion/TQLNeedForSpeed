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
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.needforspeed.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    var running = false
    var sensorManager: SensorManager? = null
    private lateinit var binding: ActivityMainBinding


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

}