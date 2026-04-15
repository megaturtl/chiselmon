package cc.turtl.chiselmon.core.api.filter

/**
 * Parses a filter condition string into a [FilterCondition] tree.
 *
 * Syntax:
 * ```
 * expr     := or_expr
 * or_expr  := and_expr (OR and_expr)*
 * and_expr := not_expr (AND not_expr)*
 * not_expr := NOT not_expr | atom
 * atom     := '(' expr ')' | tag
 * tag      := any non-whitespace, non-paren token (e.g. "shiny", "type=fire")
 * ```
 *
 * Operator precedence (highest to lowest): NOT > AND > OR.
 * Keywords are case-insensitive. Parentheses can be used freely for grouping.
 *
 * Examples:
 * ```
 * shiny AND type=fire
 * shiny OR legendary
 * NOT legendary AND min_size=1.5
 * (shiny OR legendary) AND NOT species=magikarp
 * shiny AND (type=fire OR type=dragon)
 * ```
 */
object FilterConditionParser {

    fun parse(input: String?): FilterCondition {
        if (input.isNullOrBlank()) throw ParseException("Condition string is empty")
        val lexer = Lexer(input.trim())
        val result = parseOr(lexer)
        if (lexer.hasMore()) throw ParseException("Unexpected token: '${lexer.peek()}'")
        return result
    }

    // -------------------------------------------------------------------------
    // Grammar rules
    // -------------------------------------------------------------------------

    private fun parseOr(lexer: Lexer): FilterCondition {
        val terms = mutableListOf(parseAnd(lexer))
        while (lexer.peek("OR")) {
            lexer.consume()
            terms += parseAnd(lexer)
        }
        return if (terms.size == 1) terms[0] else FilterCondition.Or(terms)
    }

    private fun parseAnd(lexer: Lexer): FilterCondition {
        val terms = mutableListOf(parseNot(lexer))
        while (lexer.peek("AND")) {
            lexer.consume()
            terms += parseNot(lexer)
        }
        return if (terms.size == 1) terms[0] else FilterCondition.And(terms)
    }

    private fun parseNot(lexer: Lexer): FilterCondition {
        if (lexer.peek("NOT")) {
            lexer.consume()
            return FilterCondition.Not(parseNot(lexer)) // right-associative
        }
        return parseAtom(lexer)
    }

    private fun parseAtom(lexer: Lexer): FilterCondition {
        if (!lexer.hasMore()) throw ParseException("Unexpected end of input -- expected a condition or '('")

        if (lexer.peek("(")) {
            lexer.consume()
            val inner = parseOr(lexer)
            if (!lexer.peek(")")) throw ParseException("Missing closing ')'")
            lexer.consume()
            return inner
        }

        val token = lexer.next()
        if (token.equals("AND", ignoreCase = true) ||
            token.equals("OR", ignoreCase = true) ||
            token.equals("NOT", ignoreCase = true) ||
            token == ")"
        ) {
            throw ParseException("Expected a condition but got keyword: '$token'")
        }
        return FilterCondition.Tag(token.lowercase())
    }

    // -------------------------------------------------------------------------
    // Lexer: splits input into tokens (words, '(', ')')
    // -------------------------------------------------------------------------

    private class Lexer(input: String) {
        private val tokens: Array<String> = input
            .replace("(", " ( ")
            .replace(")", " ) ")
            .trim()
            .split("\\s+".toRegex())
            .toTypedArray()
        private var pos = 0

        fun hasMore(): Boolean = pos < tokens.size && tokens[pos].isNotEmpty()

        fun peek(expected: String): Boolean = hasMore() && tokens[pos].equals(expected, ignoreCase = true)

        fun peek(): String = if (hasMore()) tokens[pos] else ""

        fun next(): String = tokens[pos++]

        fun consume() {
            pos++
        }
    }

    class ParseException(message: String) : RuntimeException(message)
}