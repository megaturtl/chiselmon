package cc.turtl.chiselmon.api.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a filter condition string into a {@link FilterCondition} tree.
 *
 * <p>Syntax:
 * <pre>
 * expr     := or_expr
 * or_expr  := and_expr (OR and_expr)*
 * and_expr := not_expr (AND not_expr)*
 * not_expr := NOT not_expr | atom
 * atom     := '(' expr ')' | tag
 * tag      := any non-whitespace, non-paren token (e.g. "shiny", "type=fire")
 * </pre>
 *
 * <p>Operator precedence (highest to lowest): NOT > AND > OR.
 * Keywords are case-insensitive. Parentheses can be used freely for grouping.
 *
 * <p>Examples:
 * <pre>
 * shiny AND type=fire
 * shiny OR legendary
 * NOT legendary AND min_size=1.5
 * (shiny OR legendary) AND NOT species=magikarp
 * shiny AND (type=fire OR type=dragon)
 * </pre>
 */
public class FilterConditionParser {

    public static FilterCondition parse(String input) {
        if (input == null || input.isBlank()) {
            throw new ParseException("Condition string is empty");
        }
        Lexer lexer = new Lexer(input.trim());
        FilterCondition result = parseOr(lexer);
        if (lexer.hasMore()) {
            throw new ParseException("Unexpected token: '" + lexer.peek() + "'");
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Grammar rules: collect all terms first, then create node once.
    // This avoids mutating record internals.
    // -------------------------------------------------------------------------

    private static FilterCondition parseOr(Lexer lexer) {
        List<FilterCondition> terms = new ArrayList<>();
        terms.add(parseAnd(lexer));
        while (lexer.peek("OR")) {
            lexer.consume();
            terms.add(parseAnd(lexer));
        }
        return terms.size() == 1 ? terms.getFirst() : new FilterCondition.Or(terms);
    }

    private static FilterCondition parseAnd(Lexer lexer) {
        List<FilterCondition> terms = new ArrayList<>();
        terms.add(parseNot(lexer));
        while (lexer.peek("AND")) {
            lexer.consume();
            terms.add(parseNot(lexer));
        }
        return terms.size() == 1 ? terms.getFirst() : new FilterCondition.And(terms);
    }

    private static FilterCondition parseNot(Lexer lexer) {
        if (lexer.peek("NOT")) {
            lexer.consume();
            return new FilterCondition.Not(parseNot(lexer)); // right-associative
        }
        return parseAtom(lexer);
    }

    private static FilterCondition parseAtom(Lexer lexer) {
        if (!lexer.hasMore()) {
            throw new ParseException("Unexpected end of input — expected a condition or '('");
        }

        if (lexer.peek("(")) {
            lexer.consume();
            FilterCondition inner = parseOr(lexer);
            if (!lexer.peek(")")) {
                throw new ParseException("Missing closing ')'");
            }
            lexer.consume();
            return inner;
        }

        String token = lexer.next();
        if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")
                || token.equalsIgnoreCase("NOT") || token.equals(")")) {
            throw new ParseException("Expected a condition but got keyword: '" + token + "'");
        }
        return new FilterCondition.Tag(token.toLowerCase());
    }

    // -------------------------------------------------------------------------
    // Lexer: splits input into tokens: words, '(', ')'
    // -------------------------------------------------------------------------

    private static class Lexer {
        private final String[] tokens;
        private int pos = 0;

        Lexer(String input) {
            this.tokens = input
                    .replace("(", " ( ")
                    .replace(")", " ) ")
                    .trim()
                    .split("\\s+");
        }

        boolean hasMore() {
            return pos < tokens.length && !tokens[pos].isEmpty();
        }

        boolean peek(String expected) {
            return hasMore() && tokens[pos].equalsIgnoreCase(expected);
        }

        String peek() {
            return hasMore() ? tokens[pos] : "";
        }

        String next() {
            return tokens[pos++];
        }

        void consume() {
            pos++;
        }
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}