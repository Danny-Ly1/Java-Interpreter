package lox;

// Allows us to handle the runtime error and allows us to track the token that caused it
public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
