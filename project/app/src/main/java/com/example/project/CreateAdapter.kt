package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CreateAdapter(private val courtsList: List<Court>, private val reservedTimesMap: Map<String, List<String>>, private val onMatchCreate: (String, String) -> Unit) : RecyclerView.Adapter<CreateAdapter.CourtViewHolder>()  {
    class CourtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {

        val imageView: ImageView = itemView.findViewById(R.id.imageViewCourt)
        val textViewName: TextView = itemView.findViewById(R.id.textViewCourtName)
        val spinnerTime: Spinner = itemView.findViewById(R.id.spinnerTime)
        val buttonCreateMatch: Button = itemView.findViewById(R.id.buttonCreateMatch)


    }
    private val drawableMap = mapOf(
        "ElCitedel" to R.drawable.court1,
        "Court 1" to R.drawable.court2,
        "luchtbal" to R.drawable.court3
    )
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.creat_item, parent, false)
        return CourtViewHolder(view)
    }

    override fun getItemCount(): Int {
        return courtsList.size
    }

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        val court = courtsList[position]
        holder.textViewName.text = court.name
        // Load the court image using Picasso or Glide

        val imageResId = drawableMap[court.name] ?: R.drawable.court1 // Replace with your default image resource
        holder.imageView.setImageResource(imageResId)

        val availableTimes = court.availableTimes.filter { time ->
            // Convert time string to Date or Calendar object if necessary
            // Check if this time is not in the list of reserved times for this court
            !reservedTimesMap[court.id].orEmpty().contains(time)
        }

        val timeAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, availableTimes)
        holder.spinnerTime.adapter = timeAdapter

        holder.buttonCreateMatch.setOnClickListener {
            val selectedTime = holder.spinnerTime.selectedItem.toString()
            onMatchCreate(court.id, selectedTime)
        }

    }
}