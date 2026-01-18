package lox;

import java.util.List;

import static lox.TokenType.*;


public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
    }

    /**
     * The equality rule which is expanded from the first rule, expression.
     * @return an expression.
     */
    private Expr equality() {
        Expr expr = comparision();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparision();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Checks to see if the current token has any of the given types.
     * @param types of token.
     * @return true if current token has any given types and consumes it. False otherwise and leaves the token alone.
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the current token is the same as the given type without consuming it.
     * @param type of token being compared against.
     * @return true if current token is of the given type.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    /**
     * Consumes the current token and returns it.
     * @return the consumed token.
     */
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    /**
     * Checks if we've run out of tokens to parse.
     * @return true if out of tokens. False otherwise.
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Looks at the current token that hasn't been consumed yet.
     * @return token.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the most recently consumed token.
     * @return a token.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }


}
