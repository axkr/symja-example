package com.symja.editor

import android.content.Context
import androidx.core.content.ContextCompat
import com.symja.programming.R
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.FuzzyScoreOptions
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete.Identifiers
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.lang.completion.fuzzyScoreGracefulAggressive
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import org.matheclipse.core.convert.AST2Expr
import org.matheclipse.core.expression.BuiltinUsage

class SymjaAutoCompleteProvider(val context: Context) {
    private var keywords: Array<String>
    private var keywordMap: Map<String, Any>? = null

    init {
        AST2Expr.initialize()
        keywords = AST2Expr.PREDEFINED_SYMBOLS_MAP.values.toSet().toTypedArray()
        val map = HashMap<String, Any>()
        for (keyword in keywords) {
            map[keyword] = true
        }
        keywordMap = map
    }

    /**
     * Make completion items for the given arguments.
     * Provide the required arguments passed by [Language.requireAutoComplete]
     *
     * @param prefix The prefix to make completions for.
     */
    fun requireAutoComplete(
        reference: ContentReference, position: CharPosition,
        prefix: String, publisher: CompletionPublisher, userIdentifiers: Identifiers?
    ) {
        val completionItemList = createCompletionItemList(prefix, userIdentifiers)
        publisher.addItems(completionItemList)
        publisher.setComparator { o1: CompletionItem, o2: CompletionItem ->
            if (o1.label.length != o2.label.length) {
                return@setComparator o1.label.length.compareTo(o2.label.length)
            }
            return@setComparator o1.label.toString().compareTo(o2.label.toString())
        }
    }

    private fun createCompletionItemList(
        prefix: String, userIdentifiers: Identifiers?
    ): List<CompletionItem> {
        val prefixLength = prefix.length
        if (prefixLength == 0) {
            return emptyList()
        }
        val result = ArrayList<CompletionItem>()
        val keywordArray = keywords
        val keywordMap = keywordMap
        val match = prefix.lowercase()
        for (kw in keywordArray) {
            val fuzzyScore = fuzzyScoreGracefulAggressive(
                prefix,
                prefix.lowercase(),
                0, kw, kw.lowercase(), 0, FuzzyScoreOptions.default
            )
            val score = fuzzyScore?.score ?: -100
            if (kw.lowercase().startsWith(match) || score >= -20) {
                val summaryText = BuiltinUsage.summaryText(kw) ?: "Keyword"
                val completionItemKind =
                    if (kw.startsWith("$")) CompletionItemKind.Value else CompletionItemKind.Keyword
                val completionItem = SimpleCompletionItem(kw, summaryText, prefixLength, kw)
                completionItem.kind(completionItemKind)
                completionItem.icon(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.baseline_help_outline_24
                    )
                );
                result.add(completionItem)
            }
        }
        if (userIdentifiers != null) {
            val dest: List<String> = ArrayList()
            userIdentifiers.filterIdentifiers(prefix, dest)
            for (word in dest) {
                if (keywordMap == null || !keywordMap.containsKey(word)) result.add(
                    SimpleCompletionItem(word, "Identifier", prefixLength, word)
                        .kind(CompletionItemKind.Identifier)
                )
            }
        }
        return result
    }

}