package com.example.eyetrainer.Adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R
import com.example.eyetrainer.Utils.Utils.applyImageFromExerciseItem
import com.example.eyetrainer.databinding.ItemExerciseButtonBinding

class ChoiceRecyclerViewAdapter(
    val itemFunc: (SingleExercise) -> Unit, val createFunc: ()->Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ItemExerciseButtonBinding

    class ItemViewHolder(
        private val binding: ItemExerciseButtonBinding,
        private val recycler: ChoiceRecyclerViewAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setExercise(item: ExerciseItemData) {
            binding.apply {
                setupExercise(firstExercise, firstExerciseIcon, firstExerciseName, item.first)

                if (item.second != null) {
                    setupExercise(secondExercise, secondExerciseIcon, secondExerciseName, item.second)
                } else {
                    secondExercise.visibility = View.GONE
                }
            }
        }

        private fun setupExercise(exerciseRoot: View, exerciseIcon: ImageView, exerciseText: TextView, exerciseData: SingleExercise) {
            applyImageFromExerciseItem(exerciseIcon, exerciseData)
            exerciseText.text = exerciseData.name
            if (exerciseData.image == R.drawable.icon_addition) {
                exerciseRoot.setOnClickListener {
                    recycler.createFunc()
                }
            } else {
                exerciseRoot.setOnClickListener {
                    recycler.itemFunc(exerciseData)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ItemExerciseButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        override fun areItemsTheSame(
            oldItem: ExerciseItemData, newItem: ExerciseItemData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ExerciseItemData, newItem: ExerciseItemData
        ): Boolean {
            return true // oldItem.first.name == newItem.first.name && oldItem.first.points == newItem.first.points && oldItem.first.shouldDrawArrow == newItem.first.shouldDrawArrow && oldItem.first.mirroring == newItem.first.mirroring && oldItem.second?.name == newItem.second?.name && oldItem.second?.points == newItem.second?.points && oldItem.second?.shouldDrawArrow == newItem.second?.shouldDrawArrow && oldItem.second?.mirroring == newItem.second?.mirroring
        }
    }
    val differ = AsyncListDiffer(this, differCallback)
}