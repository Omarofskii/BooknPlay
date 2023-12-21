package com.example.project


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

interface MatchInteractionListener {
    fun onJoinMatchClicked(matchId: String)
}


class MatchAdapter(private var matches: List<Match>, private val listener: MatchInteractionListener): RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {


    private val userNameCache = mutableMapOf<String, String>()

    private val db = FirebaseFirestore.getInstance()

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val imageViewCourt: ImageView = itemView.findViewById(R.id.imageViewCourt)
            val textViewCourtName: TextView = itemView.findViewById(R.id.textViewCourtName)


            val textViewDateTime: TextView = itemView.findViewById(R.id.Date)
            val textViewOrganizerName: TextView = itemView.findViewById(R.id.textViewOrganizerId)
            val joinButton: Button = itemView.findViewById(R.id.joinMatch)
            val textViewPlayerName1: TextView = itemView.findViewById(R.id.playerName1)
            val textViewPlayerName2: TextView = itemView.findViewById(R.id.playerName2)
            val textViewPlayerName3: TextView = itemView.findViewById(R.id.playerName3)
            val textViewPricePerPlayer: TextView = itemView.findViewById(R.id.PriceView)
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

        holder.textViewDateTime.text = match.dateTime.toDate().let { date ->
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        }




        val playerListSize = match.playerList.size
        holder.textViewPlayerName1.visibility = if (playerListSize > 0) View.VISIBLE else View.GONE
        holder.textViewPlayerName2.visibility = if (playerListSize > 1) View.VISIBLE else View.GONE
        holder.textViewPlayerName3.visibility = if (playerListSize > 2) View.VISIBLE else View.GONE


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

                0 -> displayPlayerName(playerId, holder.textViewPlayerName1)
                1 -> displayPlayerName(playerId, holder.textViewPlayerName2)
                2 -> displayPlayerName(playerId, holder.textViewPlayerName3)
            }
        }
        val drawableMap = mapOf(
            "ElCitedel" to R.drawable.court1,
            "Court 1" to R.drawable.court2,
            "luchtbal" to R.drawable.court3
        )

        db.collection("courts").whereEqualTo("id", match.courtId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val courtDocument = querySnapshot.documents[0] // Take the first document that matches
                    val court = courtDocument.toObject<Court>()
                    court?.let {
                        holder.textViewCourtName.text = it.name
                        val imageResId = drawableMap[court.name] ?: R.drawable.court1 // Fallback to a default image if name not found
                        holder.imageViewCourt.setImageResource(imageResId)
                    }
                } else {
                    Log.d("MatchAdapter", "No such court document with id: ${match.courtId}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MatchAdapter", "Error fetching court details", exception)
            }

        val totalCost = 10.0 // Total cost for the match
        val numberOfPlayers = match.playerList.size + 1 // Ensures we don't divide by zero
        val pricePerPlayer = totalCost / numberOfPlayers

        // Format the price to 2 decimal places and set it to the TextView
        val priceText = "prijs per speler: €" + String.format(Locale.getDefault(), "%.2f", pricePerPlayer)
        holder.textViewPricePerPlayer.text = priceText



        holder.joinButton.setOnClickListener{
            listener.onJoinMatchClicked(match.id)
        }

    }

    fun updateMatches(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }

}

