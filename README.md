<p align="center">
  <h1>Fuzzy search</h1>
</p>

[![](https://jitpack.io/v/VladKozyr/fuzzy-kotlin.svg)](https://jitpack.io/VladKozyr/fuzzy-kotlin)

Fuzzy search in Kotlin.

This project is basically a code conversion, subset of [Fuse.js](https://github.com/krisk/Fuse).

## To get this dependency into your build:
**Step 1.** Add the JitPack repository to your build file
```
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
      }
  }
```
**Step 2.** Add the dependency
```
dependencies {
    implementation 'com.github.VladKozyr:fuzzy-kotlin:VERSION'
}
```
## How to use?
**Example 1:**
```kotlin
    val books = listOf(
        Book("Old Man's War", Author("John", "Scalzi")),
        Book("The Lock Artist", Author("Steve", "Hamilton")),
        Book("HTML5", Author("Remy", "Sharp")),
        Book("Right Ho Jeeves", Author("P.D", "Woodhouse"))
    )

    val fuzzy = Fuzzy(
        books,
        FuzzyOptions(
            keys =
            listOf(
                WeightedKey({ it.title }, 1.0),
                WeightedKey({ it.author.firstName }, 3.0)
            )
        )
    )
    
    println(fuzzy.search(query))
```
## Options
`fuzzy-kotlin` takes the following options:
- `isCaseSensitive`: Indicates whether comparisons should be case sensitive.
- `findAllMatches`: When true, the algorithm continues searching to the end of the input even if a perfect match is found before the end of the same input.
- `minMatchCharLength`: Minimum number of characters that must be matched before a result is considered a match.
- `includeMatches`: Whether the matches should be included in the result set. When `true`, each record in the result set will include the indices of the matched characters. These can consequently be used for highlighting purposes.
- `shouldSort`: Whether to sort the result list, by score.
## License
`fuzzy-kotlin` is available under the MIT license. See the LICENSE file for more info.
