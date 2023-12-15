package com.example.project

import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class MatchesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _matches = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> get() = _matches

    fun fetchMatches(){
        db.collection("Matches")
            .get()
            .addOnSuccessListener { documents ->
                val matchesList = documents.mapNotNull { snapshot ->
                    val match = snapshot.toObject(Match::class.java)
                    match?.id=snapshot.id
                    match

                }
                    _matches.value = matchesList
                }
            .addOnFailureListener{exception ->
                Log.w("Error getting matches: ", exception)

            }
    }

    fun joinMatch(matchId: String, userId: String){
        if (userId == null || userId.isEmpty()){
            Log.w("ERROR","User ID is null")
            return
        }
        if (matchId.isEmpty()) {
            Log.w( "ERROR", "Match ID is null or empty")
            return
        }

        val matchRef = db.collection("Matches").document(matchId)

        db.runTransaction{transaction->
            val snapshot = transaction.get(matchRef)
            val match = snapshot.toObject(Match::class.java)
            match?.let {
                if (userId !in it.playerList){
                    val updatePlayerList = it.playerList + userId
                    transaction.update(matchRef, "playerList", updatePlayerList)
                }
            }
        }.addOnSuccessListener {
            Log.d("succes", "user successfully joined match")
        }.addOnFailureListener{e->
            Log.w("failed", "Error joining match", e)
        }
    }
}