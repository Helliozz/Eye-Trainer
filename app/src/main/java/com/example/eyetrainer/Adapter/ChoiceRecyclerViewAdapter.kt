package com.example.eyetrainer.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.databinding.ItemExerciseButtonBinding

class ChoiceRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ItemExerciseButtonBinding

    class ItemViewHolder(private val binding: ItemExerciseButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setExercise(item: ExerciseItemData) {
            binding.apply {
                firstExerciseIcon.setImageResource(item.firstImage)
                firstExerciseName.text = item.firstName
                secondExerciseIcon.setImageResource(item.secondImage)
                secondExerciseName.text = item.secondName
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding =
            ItemExerciseButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).setExercise(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    private val differCallback = object : DiffUtil.ItemCallback<ExerciseItemData>() {
        override fun areContentsTheSame(
            oldItem: ExerciseItemData, newItem: ExerciseItemData
        ): Boolean {
            return oldItem.firstName == newItem.firstName
        }

        @SuppressLint("DiffUtilEquals")
        override fun areItemsTheSame(
            oldItem: ExerciseItemData, newItem: ExerciseItemData
        ): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

}