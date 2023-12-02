package com.example.project

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide


class ProfileFragment : Fragment() {

    private lateinit var profileFirstNameTextView: TextView
    private lateinit var profileLastNameTextView: TextView
    private lateinit var profileLocationTextView: TextView
    private lateinit var profileBestHandTextView: TextView

    private lateinit var buttonEditPicture: Button
    private lateinit var profileImageView: ShapeableImageView

    private val pickImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                uploadImageToFirebase(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_menu, menu)
    }

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
        profileBestHandTextView = view.findViewById(R.id.profileBestHandTextView)

        buttonEditPicture = view.findViewById(R.id.button_edit_picture)
        profileImageView = view.findViewById(R.id.profileImageView)

        buttonEditPicture.setOnClickListener {
            chooseImage()
        }

        loadUserProfile()

        return view
    }
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageResultLauncher.launch(intent)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    chooseImage()
                } else {
                    Toast.makeText(context, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().getReference("profileImages/$uid")

        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                // Image uploaded successfully
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    // Get the download URL
                    val photoUrl = uri.toString()
                    // Update Firestore with the new URL
                    saveImageUrlToFirestore(photoUrl, uid)
                    // Update the ImageView with the new image
                    Glide.with(this@ProfileFragment)
                        .load(photoUrl)
                        .into(profileImageView)
                }
            }
            .addOnFailureListener {
                // Handle unsuccessful uploads
                Log.e("ProfileFragment", "Image upload failed", it)
            }
    }

    private fun saveImageUrlToFirestore(imageUrl: String, userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("ProfileFragment", "Image URL saved to Firestore.")
            }
            .addOnFailureListener {
                Log.e("ProfileFragment", "Failed to save image URL to Firestore.", it)
            }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1000
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
        val location = "Plays in ${user.district}, ${user.country}"
        profileLocationTextView.text = location
        profileBestHandTextView.text = user.bestHand

        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this@ProfileFragment)
                .load(user.profileImageUrl)
                .into(profileImageView)
        }

    }

    private fun clearUserProfile() {
        // Clear all the views
        profileFirstNameTextView.text = ""
        profileLastNameTextView.text = ""
        profileLocationTextView.text = ""
        profileBestHandTextView.text = ""
        profileImageView.setImageResource(R.drawable.ic_profile) // Set a default or placeholder image
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        clearUserProfile()
        val intent = Intent(activity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}