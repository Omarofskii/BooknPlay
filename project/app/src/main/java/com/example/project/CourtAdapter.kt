package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CourtAdapter(
    private val courts: List<Court>,
    private val onBookCourt: (Court, String) -> Unit
) : RecyclerView.Adapter<CourtAdapter.CourtViewHolder>() {

    class CourtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courtImageView: ImageView = view.findViewById(R.id.courtImageView)
        val courtNameTextView: TextView = view.findViewById(R.id.courtNameTextView)
        val timesSpinner: Spinner = view.findViewById(R.id.timesSpinner)
        val bookButton: Button = view.findViewById(R.id.bookButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.court_item, parent, false)
        return CourtViewHolder(view)
    }

    val drawableMap = mapOf(
        "ElCitedel" to R.drawable.court1,
        "Court 1" to R.drawable.court2,
        "luchtbal" to R.drawable.court3
    )

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        val court = courts[position]
        holder.courtNameTextView.text = court.name

//        Glide.with(holder.courtImageView.context)
//            .load(court.imageUrl)
//            .placeholder(R.drawable.court2)
//            .into(holder.courtImageView)

        val imageResId = drawableMap[court.name] ?: R.drawable.court1 // Fallback to default image
        holder.courtImageView.setImageResource(imageResId)

        // Set up the Spinner with available times
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, court.availableTimes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.timesSpinner.adapter = adapter

        var selectedTime = if (court.availableTimes.isNotEmpty()) court.availableTimes[0] else ""
        holder.timesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTime = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle the case where nothing is selected
            }
        }

        // When the book button is clicked, use the selected time for booking
        holder.bookButton.setOnClickListener {
            // Use the selected time for booking
            onBookCourt(court, selectedTime)
        }

    }

    override fun getItemCount() = courts.size
}
