package com.example.project


import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Handler

interface MatchInteractionListener {
    fun onJoinMatchClicked(matchId: String)
}


class MatchAdapter(private var matches: List<Match>, private val listener: MatchInteractionListener): RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {


    private val userNameCache = mutableMapOf<String, String>()

    private val db = FirebaseFirestore.getInstance()

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val textViewCourtId: TextView = itemView.findViewById(R.id.Place)
            val textViewDateTime: TextView = itemView.findViewById(R.id.Date)
            val textViewOrganizerName: TextView = itemView.findViewById(R.id.textViewOrganizerId)
            val joinButton: Button = itemView.findViewById(R.id.joinMatch)
            val textViewPlayerName1: TextView = itemView.findViewById(R.id.playerName1)
            val textViewPlayerName2: TextView = itemView.findViewById(R.id.playerName2)
            val textViewPlayerName3: TextView = itemView.findViewById(R.id.playerName3)
            // Bind other views in the item layout as needed

    }

    fun displayPlayerName(playerId: String, textView: TextView){
        db.collection("users").document(playerId).get()
            .addOnSuccessListener { documentSnapshot ->
                val playerName = documentSnapshot.getString("firstName") // Replace "name" with the actual field name
                Log.d("MatchAdapter", "Fetched player name: $playerName")
                textView.post{
                    if (playerName != null) {
                        textView.text = if (playerName.isNotBlank()) playerName else "Name not found"

                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                e.printStackTrace()
                textView.text = "Unknown" // Fallback text
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.match_item, parent, false)
        return MatchViewHolder(view)
    }

    override fun getItemCount() = matches.size



    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.textViewCourtId.text = match.courtId
        holder.textViewDateTime.text = match.dateTime.toDate().let { date ->
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        }


        holder.textViewPlayerName1.visibility = View.VISIBLE
        holder.textViewPlayerName2.visibility = View.VISIBLE
        holder.textViewPlayerName3.visibility = View.VISIBLE


        val organizerName = userNameCache[match.organizerId]
        if (organizerName != null){
            holder.textViewOrganizerName.text = organizerName

        } else{
            db.collection("users").document(match.organizerId)
                .get()
                .addOnSuccessListener { document->
                    val user = document.toObject(User::class.java)
                    if (user != null){
                        holder.textViewOrganizerName.text = user.firstName
                        userNameCache[match.organizerId] = user.firstName
                    }
                }
                .addOnFailureListener{
                    holder.textViewOrganizerName.text = "unknown"
                }
        }

        Log.d("MatchAdapter", "Player list size: ${match.playerList.size}")
        match.playerList.forEachIndexed { index, playerId ->
            Log.d("MatchAdapter", "Player at index $index: $playerId")
            when (index) {

                1 -> displayPlayerName(playerId, holder.textViewPlayerName1)
                2 -> displayPlayerName(playerId, holder.textViewPlayerName3)
            }
        }


        holder.joinButton.setOnClickListener{
            listener.onJoinMatchClicked(match.id)
        }

    }

    fun updateMatches(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }

}

