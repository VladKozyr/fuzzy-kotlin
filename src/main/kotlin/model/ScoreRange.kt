package model

class ScoreRange(
    var score: Double,
    var ranges: List<IntRange>,
    var isMatch: Boolean = false
) {
    operator fun plusAssign(increment: ScoreRange) {
        score += increment.score
        ranges += increment.ranges
    }
}