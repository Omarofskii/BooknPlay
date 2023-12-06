package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourtAdapter(private val courts: List<Court>) : RecyclerView.Adapter<CourtAdapter.CourtViewHolder>() {

    class CourtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courtImageView: ImageView = view.findViewById(R.id.courtImageView)
        val courtNameTextView: TextView = view.findViewById(R.id.courtNameTextView)
        val bookButton: Button = view.findViewById(R.id.bookButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.court_item, parent, false)
        return CourtViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        val court = courts[position]
        holder.courtNameTextView.text = court.name
        holder.bookButton.setOnClickListener {

        }
    }

    override fun getItemCount() = courts.size
}
