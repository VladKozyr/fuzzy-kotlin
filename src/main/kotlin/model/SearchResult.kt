package model

data class SearchResult<T>(
    val item: T,
    val matches: List<MatchScore> = listOf(),
    var score: Double = 0.0
) {
}