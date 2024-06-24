package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val user = auth.currentUser

        val fullNameTextView: TextView = view.findViewById(R.id.fullNameTextView)
        val groupNumberTextView: TextView = view.findViewById(R.id.groupNumberTextView)
        val logoutButton: Button = view.findViewById(R.id.logoutButton)

        logoutButton.setOnClickListener {
            signOut()
        }

        user?.let {
            val userId = it.uid
            db.collection("students").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fullName = document.getString("fullName")
                        val groupNumber = document.getString("groupNumber")

                        fullNameTextView.text = fullName
                        groupNumberTextView.text = groupNumber
                    }
                }
        }

        return view
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
