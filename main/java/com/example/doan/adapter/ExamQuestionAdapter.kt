package com.example.doan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.ExamQuestionUi

class ExamQuestionAdapter(
    private val questions: List<ExamQuestionUi>,
    private val selectedAnswers: MutableMap<String, String>
) : RecyclerView.Adapter<ExamQuestionAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val radioGroup: RadioGroup = view.findViewById(R.id.rgAnswers)
        val rbA: RadioButton = view.findViewById(R.id.rbA)
        val rbB: RadioButton = view.findViewById(R.id.rbB)
        val rbC: RadioButton = view.findViewById(R.id.rbC)
        val rbD: RadioButton = view.findViewById(R.id.rbD)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.tvQuestion.text = "Câu ${position + 1}: ${question.text}"
        holder.rbA.text = "A. ${question.optionA}"
        holder.rbB.text = "B. ${question.optionB}"
        holder.rbC.text = "C. ${question.optionC}"
        holder.rbD.text = "D. ${question.optionD}"

        // Gỡ listener trước khi khôi phục lựa chọn để RecyclerView không ghi đè đáp án.
        holder.radioGroup.setOnCheckedChangeListener(null)
        holder.radioGroup.clearCheck()
        val checkedId = when (selectedAnswers[question.id]) {
            "A" -> R.id.rbA
            "B" -> R.id.rbB
            "C" -> R.id.rbC
            "D" -> R.id.rbD
            else -> View.NO_ID
        }
        if (checkedId != View.NO_ID) holder.radioGroup.check(checkedId)

        holder.radioGroup.setOnCheckedChangeListener { _, id ->
            val answer = when (id) {
                R.id.rbA -> "A"
                R.id.rbB -> "B"
                R.id.rbC -> "C"
                R.id.rbD -> "D"
                else -> null
            }
            if (answer == null) selectedAnswers.remove(question.id)
            else selectedAnswers[question.id] = answer
        }
    }

    override fun getItemCount(): Int = questions.size
}
