package com.example.project

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityCourtsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CourtsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var courtAdapter: CourtAdapter
    private val courts = mutableListOf<Court>()
    private lateinit var binding: ActivityCourtsBinding
    private var currentUserProfile: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourtsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupRecyclerView()
        fetchCourts()
        loadUserProfile()
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Assuming you want to remove extra space and set it to 0
        val itemDecoration = CustomItemDecoration(2)
        recyclerView.addItemDecoration(itemDecoration)

        courtAdapter = CourtAdapter(courts) { court, selectedTime ->
            bookCourt(court, selectedTime)
        }
        recyclerView.adapter = courtAdapter
    }

    class CustomItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            with(outRect) {
                left =  spaceHeight
                right = spaceHeight
                bottom = spaceHeight
            }
        }
    }

    private fun fetchCourts() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("courts").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val court = document.toObject(Court::class.java)
                courts.add(court)
            }
            courtAdapter = CourtAdapter(courts) { court, selectedTime ->
                bookCourt(court, selectedTime)
            }
            recyclerView.adapter = courtAdapter
        }.addOnFailureListener {
            // Handle errors
        }
    }

    private fun loadUserProfile() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return // Return if UID is null

        db.collection("users").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                currentUserProfile = documentSnapshot.toObject<User>()
                // You can now use currentUserProfile when making a booking
            }
            .addOnFailureListener { e ->
                Log.w("CourtsActivity", "Error loading user profile", e)
            }
    }

    private fun bookCourt(court: Court, selectedTime: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val userName = documentSnapshot.getString("name") ?: "User"
                    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val startTime = format.parse(selectedTime)
                    val endTime = Calendar.getInstance().apply {
                        time = startTime
                        add(Calendar.MINUTE, 90)
                    }.time

                    // Check for existing reservations that would conflict with this timeslot
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("reserved_courts")
                        .whereEqualTo("courtId", court.id)
                        .get()
                        .addOnSuccessListener { documents ->
                            val conflicts = documents.mapNotNull { document ->
                                val existingStartTime = document.getTimestamp("startTime")?.toDate()
                                val existingEndTime = document.getTimestamp("endTime")?.toDate()

                                if (existingStartTime != null && existingEndTime != null &&
                                    !(startTime.after(existingEndTime) || endTime.before(
                                        existingStartTime
                                    ))
                                ) {
                                    existingStartTime to existingEndTime // Return the conflicting times
                                } else {
                                    null // No conflict, continue searching
                                }
                            }.firstOrNull()

                            if (conflicts != null) {
                                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val message = "This timeslot is already booked from " +
                                        "${format.format(conflicts.first)} until " +
                                        "${format.format(conflicts.second)}."
                                showBookingResultDialog(false, court.name, selectedTime, message)
                            } else {
                                // Proceed with the booking as the timeslot is available
                                createReservation(currentUser.uid, court, startTime, endTime, selectedTime)
                            }
                        }
                        .addOnFailureListener {
                            // Handle error, e.g., could not check existing reservations
                            showBookingResultDialog(
                                false,
                                court.name,
                                selectedTime,
                                "Failed to check existing bookings."
                            )
                        }
                } ?: run {
                    // User is not logged in, handle this case
            }
        }
    }


    private fun createReservation(userId: String, court: Court, startTime: Date, endTime: Date, selectedTime: String) {
        val reservation = hashMapOf(
            "courtId" to court.id,
            "courtName" to court.name,
            "userId" to userId,
            "startTime" to startTime,
            "endTime" to endTime
        )

        FirebaseFirestore.getInstance().collection("reserved_courts")
            .add(reservation)
            .addOnSuccessListener {
                // Booking successful
                val userName = currentUserProfile?.firstName ?: "User"
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val message = "Thank you, $userName. Your booking for ${court.name} is confirmed on $selectedTime " +
                        "and will end at ${format.format(endTime)}."
                showBookingResultDialog(true, court.name, selectedTime, message)
            }
            .addOnFailureListener { e ->
                // Booking failed
                showBookingResultDialog(false, court.name, selectedTime, "Failed to create a booking. Please try again.")
            }
    }

    private fun showBookingConfirmation(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showBookingResultDialog(success: Boolean, courtName: String, selectedTime: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(if (success) "Booking Confirmed" else "Booking Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }




}