package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateMatchActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private lateinit var spinnerCourt: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var buttonSaveMatch: Button

    private var courts: List<Court> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match)



        spinnerCourt = findViewById(R.id.spinnerCourt)
        spinnerTime = findViewById(R.id.spinnerTime)

        setupCourtSpinner()

        buttonSaveMatch = findViewById(R.id.buttonSaveMatch)
        buttonSaveMatch.setOnClickListener {
            val selectedCourtName = spinnerCourt.selectedItem as String
            val selectedCourt = courts.firstOrNull { it.name == selectedCourtName }
            val selectedCourtId = selectedCourt?.id ?: return@setOnClickListener

            val selectedTimeString = spinnerTime.selectedItem as String
            val organizerId = auth.currentUser?.uid ?: return@setOnClickListener


            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val selectedTime = timeFormat.parse(selectedTimeString)

            // Get today's date
            val today = Calendar.getInstance()

            // Set the time to the selected time
            val selectedTimeCalendar = Calendar.getInstance().apply {
                time = selectedTime
            }

            today.set(Calendar.HOUR_OF_DAY, selectedTimeCalendar.get(Calendar.HOUR_OF_DAY))
            today.set(Calendar.MINUTE, selectedTimeCalendar.get(Calendar.MINUTE))
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val timestamp = Timestamp(Date(today.timeInMillis))

            val newMatch = hashMapOf(
                "courtId" to selectedCourtId,
                "dateTime" to timestamp, // You can adjust this to your needs
                "organizerId" to organizerId,
                "playerList" to listOf<String>() // Initially empty
            )

            db.collection("Matches")
                .add(newMatch)
                .addOnSuccessListener {
                    Toast.makeText(this, "Match created successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error creating match: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }


    private fun setupCourtSpinner() {
        db.collection("courts")
            .get()
            .addOnSuccessListener { documents ->
                courts = documents.toObjects(Court::class.java)
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    courts.map { it.name }
                )
                spinnerCourt.adapter = adapter

                spinnerCourt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val court = courts[position]
                        setupTimeSpinner(court.availableTimes)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }

    private fun setupTimeSpinner(availableTimes: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            availableTimes
        )
        spinnerTime.adapter = adapter
    }

}