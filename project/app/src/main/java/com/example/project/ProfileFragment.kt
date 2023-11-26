package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.project.User
import com.google.firebase.firestore.toObject


class ProfileFragment : Fragment() {

    private lateinit var profileFirstNameTextView: TextView
    private lateinit var profileLastNameTextView: TextView
    private lateinit var profileLocationTextView: TextView
    private lateinit var profileMatchesTextView: TextView
    private lateinit var profileSportTextView: TextView
    private lateinit var profileLevelTextView: TextView
    private lateinit var profileBestHandTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profileFirstNameTextView = view.findViewById(R.id.profileFirstNameTextView)
        profileLastNameTextView = view.findViewById(R.id.profileLastNameTextView)
        profileLocationTextView = view.findViewById(R.id.profileLocationTextView)
        profileMatchesTextView = view.findViewById(R.id.matchesTextView)
        profileSportTextView = view.findViewById(R.id.profileSportTextView)
        profileLevelTextView = view.findViewById(R.id.profileLevelTextView)
        profileBestHandTextView = view.findViewById(R.id.profileBestHandTextView)

        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return // Return if UID is null

        db.collection("users").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                user?.let { updateUserProfileViews(it) }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileFragment", "Error loading user profile", e)
            }
    }

    private fun updateUserProfileViews(user: User) {
        // Populate user data into views
        profileFirstNameTextView.text = user.firstName
        profileLastNameTextView.text = user.lastName
        val location = "${user.district}, ${user.country}"
        profileLocationTextView.text = location
        profileMatchesTextView.text = user.matches.toString()
        profileSportTextView.text = user.sport
        profileLevelTextView.text = user.level.toString()
        profileBestHandTextView.text = user.bestHand

    }

}