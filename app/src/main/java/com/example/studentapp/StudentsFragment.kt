package com.example.studentapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentsAdapter: StudentsAdapter
    private var studentList: MutableList<Student> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_students, container, false)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        studentsAdapter = StudentsAdapter(studentList)
        recyclerView.adapter = studentsAdapter

        loadStudents()

        return view
    }

    private fun loadStudents() {
        val db = Firebase.firestore
        val user = auth.currentUser

        user?.let {
            val userId = it.uid
            db.collection("students").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val groupNumber = document.getString("groupNumber")
                        if (groupNumber != null) {
                            db.collection("students")
                                .whereEqualTo("groupNumber", groupNumber)
                                .get()
                                .addOnSuccessListener { result ->
                                    studentList.clear()
                                    for (document in result) {
                                        val fullName = document.getString("fullName") ?: ""
                                        val student = Student(fullName, groupNumber)
                                        studentList.add(student)
                                    }
                                    studentsAdapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("StudentsFragment", "Error loading students", e)
                                }
                        }
                    } else {
                        Log.e("StudentsFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("StudentsFragment", "Error getting document", exception)
                }
        }
    }

    data class Student(val fullName: String, val groupNumber: String)

    class StudentsAdapter(private val students: List<Student>) :
        RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

        class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val fullNameTextView: TextView = view.findViewById(R.id.fullNameTextView)
            val groupNumberTextView: TextView = view.findViewById(R.id.groupNumberTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_student, parent, false)
            return StudentViewHolder(view)
        }

        override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
            val student = students[position]
            holder.fullNameTextView.text = student.fullName
            holder.groupNumberTextView.text = student.groupNumber
        }

        override fun getItemCount(): Int = students.size
    }
}
