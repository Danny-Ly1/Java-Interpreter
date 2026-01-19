package lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;


    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * One way of running code. If we get a file path, it will read and execute it.
     * @param path The path to the file that is going to be executed.
     * @throws IOException Throws an error when the file fails.
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
    }

    /**
     * One way of running code. Allows the user to enter prompts to execute code one line at a time.
     * @throws IOException Throws an error if there is an error with the user prompt.
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    /**
     * A wrapper for the prompt and file runner.
     * @param source The source from the user.
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /**
     * Helps tell user about some syntax error given on a line.
     * @param line The line at which the error occurred.
     * @param message The type of error that occurred.
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Helper function for the error handling method. Returns a user friendly message for the error.
     * @param line The line at which the error occurred.
     * @param where Where in the line the error occurred.
     * @param message The type of error that occurred.
     */
    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    /**
     * Shows the error to the user.
     * @param token that is causing the error.
     * @param message to show to the user.
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }



}
