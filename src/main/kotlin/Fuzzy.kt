import bitap.Bitap
import model.Index
import model.SearchResult
import utils.distinctWithCallback
import kotlin.math.pow

class Fuzzy<T>(
    private val list: List<T>,
    private val options: FuzzyOptions<T>
) {

    private val index = mutableListOf<Index>()

    init {
        list.forEachIndexed { idx, type ->
            options.keys.forEach { key ->
                index.add(
                    Index(text = key.getter.invoke(type), idx, key.weight)
                )
            }
        }
    }

    fun search(query: String, limit: Int = -1): List<SearchResult<T>> {

        var results = searchStringList(query)
            .distinctWithCallback(
                { it.item },
                { old, new -> old.score *= new.score }
            )

        if (options.shouldSort)
            results = results.sortedBy { it.score }

        if (limit > -1)
            results = results.slice(0..limit)

        return results
    }

    private fun searchStringList(query: String): List<SearchResult<T>> {
        val searcher = Bitap(pattern = query, options = options)
        val results = mutableListOf<SearchResult<T>>()

        index.forEach {
            val (score, isMatch) = searcher.searchIn(it.text)

            if (isMatch) {
                results.add(
                    SearchResult(
                        item = list[it.idx],
                        score = score.pow(it.weight)
                    )
                )
            }
        }

        return results
    }
}