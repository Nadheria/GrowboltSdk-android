package com.growbolt.sdk.offerwall.ui

import android.content.Context
import android.widget.HorizontalScrollView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.growbolt.sdk.R

internal object CategoryChipHelper {

    fun populate(
        context: Context,
        chipGroup: ChipGroup,
        categories: List<String>,
        onCategorySelected: (String?) -> Unit
    ) {
        chipGroup.removeAllViews()

        // "All" chip
        val allChip = buildChip(context, context.getString(R.string.growbolt_filter_all))
        allChip.isChecked = true
        allChip.setOnCheckedChangeListener { _, checked ->
            if (checked) onCategorySelected(null)
        }
        chipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = buildChip(context, category)
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) onCategorySelected(category)
            }
            chipGroup.addView(chip)
        }
    }

    private fun buildChip(context: Context, label: String): Chip {
        return Chip(context, null, R.attr.growboltFilterChipStyle).apply {
            text = label
            isCheckable = true
            isClickable = true
        }
    }
}
