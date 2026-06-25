package com.growbolt.sdk.offerwall.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.growbolt.sdk.R

internal object CategoryChipHelper {

    private val GREEN       = Color.parseColor("#10B981")
    private val WHITE       = Color.WHITE
    private val TRANSPARENT = Color.TRANSPARENT
    private val BORDER      = Color.parseColor("#BBBBBB")
    private val TEXT_DARK   = Color.parseColor("#1A1A1A")

    fun populate(
        context: Context,
        chipGroup: ChipGroup,
        categories: List<String>,
        onCategorySelected: (String?) -> Unit
    ) {
        chipGroup.removeAllViews()



        val allChip = buildChip(context, context.getString(R.string.growbolt_filter_all))
        setSelected(allChip, true)
        allChip.setOnClickListener {
            deselectAll(chipGroup)
            setSelected(allChip, true)
            onCategorySelected(null)
        }
        chipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = buildChip(context, category)
            setSelected(chip, false)
            chip.setOnClickListener {
                deselectAll(chipGroup)
                setSelected(chip, true)
                onCategorySelected(category)
            }
            chipGroup.addView(chip)
        }
    }

    private fun deselectAll(chipGroup: ChipGroup) {
        for (i in 0 until chipGroup.childCount) {
            (chipGroup.getChildAt(i) as? Chip)?.let { setSelected(it, false) }
        }
    }

    private fun setSelected(chip: Chip, selected: Boolean) {
        if (selected) {
            chip.chipBackgroundColor = ColorStateList.valueOf(GREEN)
            chip.setTextColor(WHITE)
            chip.chipStrokeColor = ColorStateList.valueOf(GREEN)
        } else {
            chip.chipBackgroundColor = ColorStateList.valueOf(TRANSPARENT)
            chip.setTextColor(TEXT_DARK)
            chip.chipStrokeColor = ColorStateList.valueOf(BORDER)

        }
    }

    private fun buildChip(context: Context, label: String): Chip {
        // Use AppCompat-compatible Chip constructor with no style override
        return Chip(context).apply {
            text = label
            isCheckable = false
            isClickable = true
            isCheckedIconVisible = false
            isCloseIconVisible = false

            // Full pill shape
            chipCornerRadius = 50.dp(context)

            // Height — match reference image (~40dp)
            chipMinHeight = 30.dp(context)

            // Stroke
            chipStrokeWidth = 3f
            chipStrokeColor = ColorStateList.valueOf(BORDER)

            // Background
            chipBackgroundColor = ColorStateList.valueOf(TRANSPARENT)

            // Text
            setTextColor(TEXT_DARK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)

            // Horizontal padding inside chip
            chipStartPadding = 16.dp(context)
            chipEndPadding = 16.dp(context)
            textStartPadding = 0f
            textEndPadding = 0f

            // No icon padding
            iconStartPadding = 0f
            iconEndPadding = 0f

            // No elevation / shadow
            elevation = 0f
            stateListAnimator = null
        }
    }

    private fun Int.dp(context: Context): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        )
}