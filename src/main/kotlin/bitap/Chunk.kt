package bitap

data class Chunk(
    val pattern: String,
    val alphabet: Map<Char, Int>,
    val startIndex: Int
)