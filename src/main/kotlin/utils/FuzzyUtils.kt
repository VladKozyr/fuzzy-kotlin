package utils

import kotlin.math.abs

fun String.getPatternAlphabet(): Map<Char, Int> {
    val mask = mutableMapOf<Char, Int>()
    for (i in 0 until length) {
        val char = this[i]
        mask[char] = (mask[char] ?: 0) or (1 shl (length - i - 1))
    }
    return mask
}

inline fun <T, K> List<T>.distinctWithCallback(selector: (T) -> K, observer: (t1: T, t2: T) -> Unit): List<T> {
    val map = HashMap<K, T>()
    for (e in this) {
        val key = selector(e)
        if (map.containsKey(key)) {
            observer.invoke(map[key]!!, e)
        } else {
            map[key] = e
        }
    }
    return map.values.toList()
}

object FuzzyUtils {

    // Computes the score for a match with `e` errors and `x` location.
    //
    // - Parameter patternLength: Length of pattern being sought.
    // - Parameter e: Number of errors in match.
    // - Parameter x: Location of match.
    // - Parameter loc: Expected location of match.
    // - Parameter scoreTextLength: Coerced version of text's length.
    // - Returns: Overall score for match (0.0 = good, 1.0 = bad).
    fun computeScore(
        pattern: String,
        errors: Int = 0,
        currentLocation: Int = 0,
        expectedLocation: Int = 0,
        distance: Int,
        ignoreLocation: Boolean
    ): Double {

        val accuracy = errors.toDouble() / pattern.length

        if (ignoreLocation) return accuracy

        val proximity = abs(expectedLocation - currentLocation)

        if (distance == 0) {
            return when (proximity != 0) {
                true -> 1.0
                false -> accuracy
            }
        }

        return accuracy + proximity.toDouble() / distance
    }

    fun convertMaskToIndices(mask: IntArray, minMatchCharLength: Int): List<IntRange> {
        val indices = mutableListOf<IntRange>()
        var start = -1
        var end: Int

        for (i in mask.indices) {
            val match = mask[i]
            if (match == 1 && start == -1) {
                start = i
            } else if (match == 0 && start != -1) {
                end = i - 1
                if ((end - start) + 1 >= minMatchCharLength) {
                    indices.add(start..end)
                }
                start = -1
            }
        }

        if (mask.last() != 0 && (mask.size - start) >= minMatchCharLength) {
            indices.add(start until mask.size)
        }

        return indices
    }
}