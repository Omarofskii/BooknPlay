package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityCourtsBinding
import com.google.firebase.firestore.FirebaseFirestore

class CourtsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var courtAdapter: CourtAdapter
    private val courts = mutableListOf<Court>()
    private lateinit var binding: ActivityCourtsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourtsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = binding.recyclerView // If using ViewBinding, replace findViewById with binding
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchCourts()
    }

    private fun fetchCourts() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("courts").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val court = document.toObject(Court::class.java)
                courts.add(court)
            }
            courtAdapter = CourtAdapter(courts)
            recyclerView.adapter = courtAdapter
        }.addOnFailureListener {
            // Handle errors
        }


    }
}