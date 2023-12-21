package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.util.TimeZone

class CreateMatchActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var createAdapter: CreateAdapter

    val reservedTimesMap = mutableMapOf<String, List<String>>()



    private var courts: List<Court> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match)

        recyclerView = findViewById(R.id.recyclerViewCourts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchReservedTimes {
            setupCourtRecyclerView()
        }




    }

    private fun fetchReservedTimes(completion: () -> Unit) {
        db.collection("reserved_courts").get().addOnSuccessListener { documents ->
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC+1")

            reservedTimesMap.clear()
            for (document in documents) {
                val courtId = document.getString("courtId") ?: continue
                val startTime = document.getTimestamp("startTime")?.toDate().toString() // Adjust date formatting as needed
                val endTime = document.getTimestamp("endTime")?.toDate().toString() // Adjust date formatting as needed

                // Assuming your availableTimes are in the same format
                val times = reservedTimesMap.getOrPut(courtId) { mutableListOf() }.toMutableList()
                times += startTime
                times += endTime
            }
            completion()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching reserved times: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCourtRecyclerView() {
        db.collection("courts")
            .get()
            .addOnSuccessListener { documents ->
                val courts = documents.toObjects(Court::class.java)
                createAdapter = CreateAdapter(courts, reservedTimesMap) { courtId, time ->
                    createMatch(courtId, time)
                }
                recyclerView.adapter = createAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading courts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createMatch(courtId: String, time: String) {
        val organizerId = auth.currentUser?.uid ?: return
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC+1")
        }
        val selectedTime = timeFormat.parse(time) ?: return
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC+1")).apply {
            set(Calendar.HOUR_OF_DAY, selectedTime.hours)
            set(Calendar.MINUTE, selectedTime.minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }




        val endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC+1")).apply {
            timeInMillis = today.timeInMillis
            add(Calendar.MINUTE, 90)
        }

        val startTimestamp = Timestamp(today.time)
        val endTimestamp = Timestamp(endTime.time)



        db.collection("reserved_courts")
            .whereEqualTo("courtId", courtId)
            .get()
            .addOnSuccessListener { documents ->
                val conflict = documents.any { document ->
                    val existingStartTime = document.getTimestamp("startTime")?.toDate()
                    val existingEndTime = document.getTimestamp("endTime")?.toDate()
                    existingStartTime != null && existingEndTime != null &&
                            !today.time.after(existingEndTime) && !endTime.time.before(existingStartTime)
                }

                if (conflict) {
                    // Inform the user about the conflict and do not proceed with the creation
                    Toast.makeText(this, "Selected time slot is already reserved.", Toast.LENGTH_LONG).show()
                } else {
                    // Proceed with creating the match as no conflict exists
                    val matchTimestamp = Timestamp(today.time)
                    val endMatchTimestamp = Timestamp(endTime.time)

                    val newMatch = hashMapOf(
                        "courtId" to courtId,
                        "dateTime" to matchTimestamp,
                        "organizerId" to organizerId,
                        "playerList" to listOf<String>()
                    )

                    db.collection("Matches").add(newMatch)
                        .addOnSuccessListener { documentReference ->
                            val reservedCourt = hashMapOf(
                                "courtId" to courtId,
                                "courtName" to "Court Name Here", // Replace with actual court name
                                "startTime" to startTimestamp,
                                "endTime" to endTimestamp,
                                "userId" to organizerId
                            )
                            db.collection("reserved_courts").add(reservedCourt)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Match and reservation created successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error creating reservation: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error creating match: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check existing bookings.", Toast.LENGTH_SHORT).show()
            }


       /* db.collection("Matches").add(newMatch)
            .addOnSuccessListener {
                    documentReference ->
                // If the match is successfully created, add a reservation to the reserved_courts collection
                val reservedCourt = hashMapOf(
                    "courtId" to courtId,
                    // You'll need to provide the court name here, you can fetch it based on the courtId
                    "courtName" to "Court Name Here",
                    "startTime" to timestamp,
                    // Calculate the end time based on your match duration
                    "endTime" to Timestamp(Date(timestamp.seconds * 1000 + 5400000)),
                    "userId" to organizerId
                )
                db.collection("reserved_courts").add(reservedCourt)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Match and reservation created successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error creating reservation: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating match: ${e.message}", Toast.LENGTH_SHORT).show()
            }*/

    }




}