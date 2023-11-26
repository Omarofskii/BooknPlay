package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.project.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.project.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.SignUpbutton.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val pass = binding.editTextTextPassword2.text.toString()
            val firstName = binding.editTextFirstName.text.toString().trim()
            val lastName = binding.editTextLastName.text.toString().trim()
            val district = binding.editTextDistrict.text.toString().trim()
            val country = binding.editTextCountry.text.toString().trim()
            val matches = binding.editTextMatches.text.toString().toIntOrNull() ?: 0
            val sport = binding.editTextSport.text.toString().trim()
            val level = binding.editTextLevel.text.toString().toIntOrNull() ?: 0
            val bestHand = binding.editTextBestHand.text.toString().trim()


            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isComplete) {

                        val newUser = User(email, firstName, lastName, district, country, matches, sport, level, bestHand)

                        val firestoreDb = FirebaseFirestore.getInstance()

                        val userId = firebaseAuth.currentUser?.uid

                        if (userId != null) {
                            // Store the new user details in Firestore
                            firestoreDb.collection("users").document(userId)
                                .set(newUser)
                                .addOnSuccessListener {
                                    // Here you can handle the successful saving of user details
                                    // For example, by navigating to the sign-in activity or home screen
                                    val intent = Intent(this, SignInActivity::class.java) // Replace NextActivity with the actual activity you want to navigate to
                                    startActivity(intent)
                                    finish() // Finish this activity so the user can't return to the registration screen
                                }
                                .addOnFailureListener { e ->
                                    // Handle the failure of saving user details
                                    Toast.makeText(this, "Failed to save user details: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Failed to get user ID", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}