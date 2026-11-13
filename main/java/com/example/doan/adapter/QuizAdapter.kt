package com.example.doan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.Quiz

class QuizAdapter (
    private val quizzes: List <Quiz>,
    private val onStartClick: (Quiz) -> Unit
) : RecyclerView.Adapter <QuizAdapter.QuizViewHolder> () {

    class QuizViewHolder (itemView: View) :
        RecyclerView.ViewHolder (itemView) {

        val tvTitle: TextView =
            itemView.findViewById (R.id.tvQuizTitle)

        val tvSubject: TextView =
            itemView.findViewById (R.id.tvSubject)

        val tvInfo: TextView =
            itemView.findViewById (R.id.tvQuizInfo)

        val tvStatus: TextView =
            itemView.findViewById (R.id.tvStatus)

        val btnStart: Button =
            itemView.findViewById (R.id.btnStartQuiz)
    }

    override fun onCreateViewHolder (
        parent: ViewGroup,
        viewType: Int
    ): QuizViewHolder {
        val view = LayoutInflater.from (parent.context)
            .inflate (R.layout.item_quiz, parent, false)

        return QuizViewHolder (view)
    }

    override fun onBindViewHolder (
        holder: QuizViewHolder,
        position: Int
    ) {
        val quiz = quizzes [position]

        holder.tvTitle.text = quiz.title
        holder.tvSubject.text = quiz.subject

        holder.tvInfo.text =
            "${quiz.durationMinutes} phút • ${quiz.questionCount} câu hỏi"

        holder.tvStatus.text = quiz.status

        holder.tvStatus.setTextColor (
            when (quiz.status) {
                "Live" -> android.graphics.Color.rgb (24, 134, 75)
                "Upcoming" -> android.graphics.Color.rgb (190, 133, 20)
                "Completed" -> android.graphics.Color.rgb (75, 105, 140)
                else -> android.graphics.Color.DKGRAY
            }
        )

        holder.btnStart.text = when (quiz.status) {
            "Live" -> "Start Quiz"
            "Completed" -> "View Results"
            else -> "View Details"
        }

        holder.btnStart.setOnClickListener {
            onStartClick (quiz)
        }
    }

    override fun getItemCount (): Int = quizzes.size
}