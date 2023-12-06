package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivitySignUpBinding
import com.example.project.databinding.ActivitySigninBinding
import com.example.project.databinding.FragmentPlayBinding


class PlayFragment : Fragment() {
    private lateinit var binding: FragmentPlayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playMatch.setOnClickListener{
            Log.d("PlayFragment", "Play Match card clicked")
            val intent = Intent(requireActivity(), PlayMatchActivity::class.java)
            startActivity(intent)
        }
        binding.bookCourt.setOnClickListener{
            val intent = Intent(requireActivity(), CourtsActivity::class.java)
            startActivity(intent)
        }
    }


}