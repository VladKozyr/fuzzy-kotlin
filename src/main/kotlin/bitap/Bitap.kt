package bitap

import FuzzyOptions
import model.MatchScore
import utils.FuzzyConstants
import utils.FuzzyUtils
import utils.getPatternAlphabet
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class Bitap<T>(
    val pattern: String,
    val options: FuzzyOptions<T>
) {

    private val chunks = mutableListOf<Chunk>()

    init {
        fillChunks()
    }

    fun searchIn(searchText: String): MatchScore {

        var text = searchText

        if (!options.isCaseSensitive) {
            text = text.lowercase()
        }

        if (pattern == searchText) {
            return MatchScore(
                score = 0.0,
                isMatch = true,
                indices = listOf(text.indices)
            )
        }

        val allIndices = mutableListOf<IntRange>()
        var totalScore = 0.0
        var hasMatches = false

        chunks.forEach { (pattern, alphabet, startIndex) ->
            val (score, isMatch, indices) = search(
                text = text,
                pattern = pattern,
                patternAlphabet = alphabet,
                location = options.location + startIndex,
                distance = options.distance,
                threshold = options.threshold,
                findAllMatches = options.findAllMatches,
                minMatchCharLength = options.minMatchCharLength,
                includeMatches = options.includeMatches,
                ignoreLocations = options.ignoreLocation
            )

            if (isMatch) {
                hasMatches = true
                indices?.let { allIndices += it }
            }

            totalScore += score
        }

        val result = MatchScore(
            isMatch = hasMatches,
            score = if (hasMatches) totalScore / chunks.size else 1.0
        )

        if (hasMatches && options.includeMatches) {
            result.indices = allIndices
        }

        return result
    }

    private fun fillChunks() {
        //if(pattern.isBlank()) return
        val len = pattern.length

        if (len > FuzzyConstants.MAX_BITS) {
            var i = 0
            val remainder = len % FuzzyConstants.MAX_BITS
            val end = len - remainder

            while (i < end) {
                addChunk(pattern.substring(i, FuzzyConstants.MAX_BITS), i)
                i += FuzzyConstants.MAX_BITS
            }

            if (remainder == 0) {
                val startIndex = len - FuzzyConstants.MAX_BITS
                addChunk(pattern.substring(startIndex), startIndex)
            }
        } else {
            addChunk(pattern, 0)
        }
    }

    private fun addChunk(pattern: String, startIndex: Int) {
        chunks.add(
            Chunk(
                pattern = pattern,
                alphabet = pattern.getPatternAlphabet(),
                startIndex = startIndex
            )
        )
    }
}

internal fun search(
    text: String,
    pattern: String,
    patternAlphabet: Map<Char, Int>,
    location: Int = 0,
    distance: Int = 100,
    threshold: Double = 0.6,
    findAllMatches: Boolean = false,
    minMatchCharLength: Int = 1,
    includeMatches: Boolean = false,
    ignoreLocations: Boolean = false
): MatchScore {

    if (pattern.length > FuzzyConstants.MAX_BITS) error("Pattern length too large!")

    // Handle the case when location > text.length
    val expectedLocation = max(0, min(location, text.length))
    // Highest score beyond which we give up.
    var currentThreshold = threshold
    // Is there a nearby exact match? (speedup)
    var bestLocation = expectedLocation

    // Performance: only computer matches when the minMatchCharLength > 1
    // OR if `includeMatches` is true.
    val computeMatches = minMatchCharLength > 1 || includeMatches
    // A mask of the matches, used for building the indices
    val matchMask = if (computeMatches) IntArray(text.length) else IntArray(0)

    var index = text.indexOf(pattern, bestLocation)
    // Get all exact matches, here for speed up
    while (index > -1) {
        val score = FuzzyUtils.computeScore(
            pattern = pattern,
            expectedLocation = expectedLocation,
            currentLocation = index,
            distance = distance,
            ignoreLocation = ignoreLocations
        )

        currentThreshold = min(score, currentThreshold)
        bestLocation = index + pattern.length

        if (computeMatches) {
            var i = 0
            while (i < pattern.length) {
                matchMask[index + i] = 1
                i++
            }
        }

        index = text.indexOf(pattern, bestLocation)
    }

    // Reset the best location
    bestLocation = -1

    var lastBitList = IntArray(0)
    var finalScore = 1.0
    var binMax = pattern.length + text.length

    val mask = 1 shl pattern.length.dec()

    for (i in pattern.indices) {

        // Scan for the best match; each iteration allows for one more error.
        // Run a binary search to determine how far from the match location we can stray
        // at this error level.
        var binMin = 0
        var binMid = binMax

        while (binMin < binMid) {
            val score = FuzzyUtils.computeScore(
                pattern = pattern,
                errors = i,
                currentLocation = expectedLocation + binMid,
                expectedLocation = expectedLocation,
                distance = distance,
                ignoreLocation = ignoreLocations
            )

            if (score <= currentThreshold) {
                binMin = binMid
            } else {
                binMax = binMid
            }

            binMid = floor((binMax - binMin).toDouble() / 2 + binMin).toInt()
        }

        // Use the result from this iteration as the maximum for the next.
        binMax = binMid

        var start = max(1, expectedLocation - binMid + 1)
        val finish = if (findAllMatches)
            text.length
        else
            min(expectedLocation + binMid, text.length) + pattern.length

        // Initialize the bit array
        val bitArr = IntArray(finish + 2)

        bitArr[finish + 1] = (1 shl i) - 1

        var j = finish
        while (j >= start) {
            val currentLocation = j - 1
            val charMatch = if (currentLocation < text.length) patternAlphabet[text[currentLocation]] ?: 0 else 0

            if (computeMatches) {
                matchMask[currentLocation] = charMatch
            }

            // First pass: exact match
            bitArr[j] = ((bitArr[j + 1] shl 1) or 1) and charMatch

            if (i != 0) {
                bitArr[j] = bitArr[j] or (((lastBitList[j + 1] or lastBitList[j]) shl 1) or 1 or lastBitList[j + 1])
            }

            if ((bitArr[j] and mask) != 0) {
                finalScore = FuzzyUtils.computeScore(
                    pattern = pattern,
                    errors = i,
                    currentLocation = currentLocation,
                    expectedLocation = expectedLocation,
                    distance = distance,
                    ignoreLocation = ignoreLocations
                )
                if (finalScore <= currentThreshold) {
                    currentThreshold = finalScore
                    bestLocation = currentLocation

                    if (bestLocation <= expectedLocation) {
                        break
                    }

                    start = max(1, 2 * expectedLocation - bestLocation)
                }
            }
            j--
        }

        val score = FuzzyUtils.computeScore(
            pattern = pattern,
            errors = i + 1,
            currentLocation = expectedLocation,
            expectedLocation = expectedLocation,
            distance = distance,
            ignoreLocation = ignoreLocations
        )

        if (score > currentThreshold) {
            break
        }

        lastBitList = bitArr
    }

    val result = MatchScore(
        isMatch = bestLocation >= 0,
        score = max(0.001, finalScore)
    )

    if (computeMatches) {
        val indices = FuzzyUtils.convertMaskToIndices(matchMask, minMatchCharLength)
        if (indices.isEmpty().not()) {
            result.isMatch = false
        } else if (includeMatches) {
            result.indices = indices
        }
    }

    return result
}
