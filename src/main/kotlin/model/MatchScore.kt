package model

data class MatchScore(
    val score: Double,
    var isMatch: Boolean = true,
    var indices: List<IntRange>? = null
)