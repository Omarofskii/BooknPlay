package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CourtAdapter(private val courts: List<Court>) : RecyclerView.Adapter<CourtAdapter.CourtViewHolder>() {

    class CourtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courtImageView: ImageView = view.findViewById(R.id.courtImageView)
        val courtNameTextView: TextView = view.findViewById(R.id.courtNameTextView)
        val availableTimesTextView: TextView = view.findViewById(R.id.availableTimesTextView)
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

        val availableTimesText = court.availableTimes.joinToString(separator = ", ")
        holder.availableTimesTextView.text = availableTimesText
        holder.bookButton.setOnClickListener {

        }
    }

    override fun getItemCount() = courts.size
}
