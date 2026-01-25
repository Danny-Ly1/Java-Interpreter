package lox;

import java.util.ArrayList;
import java.util.List;

import static lox.TokenType.*;


public class Parser {
    // Sentinel class used to unwind the parser
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Expr expression() {
        return equality();
    }

    /**
     * The equality grammar rule which is expanded from the first rule, expression.
     * @return an expression.
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * The comparison grammar rule.
     * @return an expression.
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * The term grammar rule. (Addition and subtraction).
     * @return an expression.
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * The factor grammar rule. (Multiplication and division).
     * @return an expression.
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * The unary grammar rule (! or -).
     * @return an expression.
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
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
     * Consume the closing bracket if any.
     * @param type of token being consumed.
     * @param message for the error if it occurs.
     * @return the token that will be consumed.
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
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

    /**
     * Report an error.
     * @param token that is being reported.
     * @param message to show user if there's an error.
     * @return a ParseError object.
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Discards tokens until it has found a statement body (To be back in sync).
     */
    private void synchonrize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    /**
     * Parses the statement to create the program.
     * @return a statement object.
     */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    /**
     * Parse a print statement which gets converted into a Stmt type.
     * @return the value as a Stmt print type.
     */
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    /**
     * Parse an expression which gets converted into a Stmt type.
     * @return the wrapped expression in a Stmt type.
     */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }




}
