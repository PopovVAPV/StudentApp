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

class ExamsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var examsAdapter: ExamsAdapter
    private var examList: MutableList<Exam> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exams, container, false)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        examsAdapter = ExamsAdapter(examList)
        recyclerView.adapter = examsAdapter

        loadExams()

        return view
    }

    private fun loadExams() {
        val db = Firebase.firestore
        val user = auth.currentUser

        user?.let {
            val userId = it.uid
            db.collection("students").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val groupNumber = documentSnapshot.getString("groupNumber")
                        Log.d("ExamsFragment", "groupNumber: $groupNumber")

                        groupNumber?.let { groupNum ->
                            db.collection("examSchedules").document(groupNum)
                                .get()
                                .addOnSuccessListener { examScheduleSnapshot ->
                                    if (examScheduleSnapshot.exists()) {
                                        examList.clear()
                                        val exams = examScheduleSnapshot.get("exams") as? List<*>
                                        Log.d("ExamsFragment", "exams: $exams")

                                        if (exams != null) {
                                            for (i in exams.indices step 5) {
                                                val subject = exams.getOrNull(i) as? String ?: ""
                                                val date = exams.getOrNull(i + 1) as? String ?: ""
                                                val startTime = exams.getOrNull(i + 2) as? String ?: ""
                                                val durationMinutes = (exams.getOrNull(i + 3) as? Long)?.toInt() ?: 0
                                                val teacherFullName = exams.getOrNull(i + 4) as? String ?: ""

                                                val exam = Exam(subject, date, startTime, durationMinutes, teacherFullName)
                                                examList.add(exam)
                                            }
                                            examsAdapter.notifyDataSetChanged()
                                        } else {
                                            Log.d("ExamsFragment", "No exams found for group: $groupNum")
                                        }
                                    } else {
                                        Log.d("ExamsFragment", "No exam schedule found for group: $groupNum")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("ExamsFragment", "Error getting exam schedule", e)
                                }
                        } ?: run {
                            Log.d("ExamsFragment", "Group number is null for user: $userId")
                        }
                    } else {
                        Log.d("ExamsFragment", "No student document found for user: $userId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("ExamsFragment", "Error getting student document", e)
                }
        } ?: run {
            Log.d("ExamsFragment", "User is null")
        }
    }

    data class Exam(
        val subject: String,
        val date: String,
        val startTime: String,
        val durationMinutes: Int,
        val teacherFullName: String
    )

    class ExamsAdapter(private val exams: List<Exam>) :
        RecyclerView.Adapter<ExamsAdapter.ExamViewHolder>() {

        class ExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val subjectTextView: TextView = view.findViewById(R.id.subjectTextView)
            val dateTextView: TextView = view.findViewById(R.id.dateTextView)
            val timeTextView: TextView = view.findViewById(R.id.timeTextView)
            // val durationTextView: TextView = view.findViewById(R.id.durationTextView)
            val teacherTextView: TextView = view.findViewById(R.id.teacherTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exam, parent, false)
            return ExamViewHolder(view)
        }

        override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
            val exam = exams[position]
            holder.subjectTextView.text = exam.subject
            holder.dateTextView.text = exam.date
            holder.timeTextView.text = exam.startTime
            // holder.durationTextView.text = exam.durationMinutes.toString()
            holder.teacherTextView.text = exam.teacherFullName
        }

        override fun getItemCount(): Int = exams.size
    }
}
