package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlayMatchActivity : AppCompatActivity(), MatchInteractionListener {

    private lateinit var recyclerView: RecyclerView
    private val viewModel by viewModels<MatchesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_match)

        val buttonGoToCreateMatch: Button = findViewById(R.id.buttonGoToCreateMatch)
        buttonGoToCreateMatch.setOnClickListener {
            val intent = Intent(this, CreateMatchActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView2).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PlayMatchActivity)
            adapter = MatchAdapter(emptyList(), this@PlayMatchActivity)
        }

        viewModel.matches.observe(this, {matches ->
            (recyclerView.adapter as MatchAdapter).updateMatches(matches)
        })

        viewModel.fetchMatches()
    }

    override fun onResume() {
        super.onResume()
        // Fetch the matches every time the activity resumes
        viewModel.fetchMatches()
    }



    override fun onJoinMatchClicked(matchId: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            viewModel.joinMatch(matchId, userId)
        }
    }
}