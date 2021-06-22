package model

data class WeightedKey<T>(
    val getter: (T) -> String,
    var weight: Double = 1.0
)