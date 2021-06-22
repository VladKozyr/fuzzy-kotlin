import model.WeightedKey
import java.util.regex.Pattern

class FuzzyOptions<T>(

    // Approximately where in the text is the pattern expected to be found?
    val location: Int = 0,

    // Determines how close the match must be to the fuzzy location (specified above).
    // An exact letter match which is 'distance' characters away from the fuzzy location
    // would score as a complete mismatch. A distance of '0' requires the match be at
    // the exact location specified, a threshold of '1000' would require a perfect match
    // to be within 800 characters of the fuzzy location to be found using a 0.8 threshold.
    val distance: Int = 100,

    // At what point does the match algorithm give up. A threshold of '0.0' requires a perfect match
    // (of both letters and location), a threshold of '1.0' would match anything.
    val threshold: Double = 0.6,

    // Machine word size
    val maxPatternLength: Int = 32,

    // Indicates whether comparisons should be case sensitive.
    val isCaseSensitive: Boolean = false,

    // When true, the algorithm continues searching to the end of the input even if a perfect
    // match is found before the end of the same input.
    val findAllMatches: Boolean = false,

    // Minimum number of characters that must be matched before a result is considered a match
    val minMatchCharLength: Int = 1,

    // Whether the matches should be included in the result set. When `true`, each record in the result
    // set will include the indices of the matched characters.
    // These can consequently be used for highlighting purposes.
    val includeMatches: Boolean = false,

    // When `true`, search will ignore `location` and `distance`, so it won't matter
    // where in the string the pattern appears.
    // More info: https://fusejs.io/concepts/scoring-theory.html#fuzziness-score
    val ignoreLocation: Boolean = false,

    // List of weighted getters to properties that will be searched
    val keys: List<WeightedKey<T>> = mutableListOf(),

    // Whether to sort the result list, by score
    val shouldSort: Boolean = true
) {
    init {
        keys.normalizeWeights()
    }

    private fun List<WeightedKey<T>>.normalizeWeights() {
        val weightSum = sumByDouble { weight -> weight.weight }

        return forEach { it.weight /= weightSum }
    }
}
