package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.Category
import com.example.data.model.TaskPriority
import com.example.ui.theme.*

object CategoryUtils {

    val defaultCategories = listOf(
        "All",
        "General",
        "Work",
        "Personal",
        "Study",
        "Health",
        "Finance",
        "Shopping"
    )

    // Vibrant Glass Colors Palette for Custom Categories
    val customColorPalette = listOf(
        "#3B82F6", // Blue
        "#EC4899", // Pink
        "#8B5CF6", // Purple
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#06B6D4", // Cyan
        "#EF4444", // Red
        "#6366F1", // Indigo
        "#84CC16", // Lime
        "#14B8A6", // Teal
        "#F97316", // Orange
        "#A855F7"  // Violet
    )

    // Clean Material Vector Icons for Custom Categories (No Emojis)
    val availableMaterialIcons = mapOf(
        "Star" to Icons.Default.Star,
        "Code" to Icons.Default.Code,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Flight" to Icons.Default.Flight,
        "Restaurant" to Icons.Default.Restaurant,
        "Brush" to Icons.Default.Brush,
        "Pets" to Icons.Default.Pets,
        "MusicNote" to Icons.Default.MusicNote,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "Camera" to Icons.Default.CameraAlt,
        "Lightbulb" to Icons.Default.Lightbulb,
        "LocalOffer" to Icons.Default.LocalOffer,
        "Build" to Icons.Default.Build,
        "Sports" to Icons.Default.SportsBasketball,
        "Home" to Icons.Default.Home,
        "Bookmark" to Icons.Default.Bookmark
    )

    private val customCategoryMap = mutableMapOf<String, Category>()

    fun registerCustomCategories(categories: List<Category>) {
        customCategoryMap.clear()
        categories.forEach {
            customCategoryMap[it.name.lowercase()] = it
        }
    }

    fun parseHexColor(hex: String, fallback: Color): Color {
        return runCatching {
            val cleanHex = hex.removePrefix("#")
            val colorInt = cleanHex.toLong(16) or 0xFF000000
            Color(colorInt)
        }.getOrDefault(fallback)
    }

    fun getCategoryColor(categoryName: String): Color {
        val lower = categoryName.lowercase()
        val custom = customCategoryMap[lower]
        if (custom != null) {
            return parseHexColor(custom.colorHex, GlassLightPrimary)
        }

        return when (lower) {
            "work" -> CategoryWork
            "personal" -> CategoryPersonal
            "study" -> CategoryStudy
            "health" -> CategoryHealth
            "finance" -> CategoryFinance
            "shopping" -> CategoryShopping
            else -> GlassLightPrimary
        }
    }

    fun getCategoryIcon(categoryName: String): ImageVector {
        val lower = categoryName.lowercase()
        val custom = customCategoryMap[lower]
        if (custom != null) {
            val mappedIcon = availableMaterialIcons[custom.iconName]
            if (mappedIcon != null) return mappedIcon
        }

        return when (lower) {
            "work" -> Icons.Default.Work
            "personal" -> Icons.Default.Person
            "study" -> Icons.Default.School
            "health" -> Icons.Default.Favorite
            "finance" -> Icons.Default.AccountBalanceWallet
            "shopping" -> Icons.Default.ShoppingCart
            else -> Icons.Default.Folder
        }
    }

    fun getPriorityColor(priority: TaskPriority): Color {
        return when (priority) {
            TaskPriority.HIGH -> PriorityHigh
            TaskPriority.MEDIUM -> PriorityMedium
            TaskPriority.LOW -> PriorityLow
        }
    }

    fun getPriorityIcon(priority: TaskPriority): ImageVector {
        return when (priority) {
            TaskPriority.HIGH -> Icons.Default.LocalFireDepartment
            TaskPriority.MEDIUM -> Icons.Default.FlashOn
            TaskPriority.LOW -> Icons.Default.Spa
        }
    }
}
