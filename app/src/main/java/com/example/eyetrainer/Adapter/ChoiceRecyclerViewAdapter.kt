package com.example.eyetrainer.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.databinding.ItemExerciseButtonBinding

class ChoiceRecyclerViewAdapter(
    val itemFun: (SingleExercise) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ItemExerciseButtonBinding

    class ItemViewHolder(
        private val binding: ItemExerciseButtonBinding,
        private val recycler: ChoiceRecyclerViewAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setExercise(item: ExerciseItemData) {
            binding.apply {
                firstExerciseIcon.setImageResource(item.first.image)
                firstExerciseName.text = item.first.name
                secondExerciseIcon.setImageResource(item.second.image)
                secondExerciseName.text = item.second.name

                firstExercise.setOnClickListener {
                    recycler.itemFun(item.first)
                }
                secondExercise.setOnClickListener {
                    recycler.itemFun(item.second)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding =
            ItemExerciseButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, this)
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
            return oldItem.first.name == newItem.first.name && oldItem.first.image == newItem.first.image && oldItem.second.name == newItem.second.name && oldItem.second.image == newItem.second.image
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