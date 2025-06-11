import java.io.*;

/**
 * Concrete implementation of AbstractGenerate for SCC312 coursework.
 */
public class Generate extends AbstractGenerate {

    public Generate() {
        // You can add any initialization here if needed,
        // but no extra methods beyond what AbstractGenerate declares.
    }

    /**
     * Insert a terminal token (already confirmed by the parser).
     * This simply logs the event.
     */
    @Override
    public void insertTerminal(Token token) {
        // Example trace output
        String tt = Token.getName(token.symbol);
        if (token.symbol == Token.identifier 
            || token.symbol == Token.numberConstant 
            || token.symbol == Token.stringConstant) {
            tt += " '" + token.text + "'";
        }
        tt += " on line " + token.lineNumber;
        System.out.println("312TOKEN " + tt);
    }

    /**
     * Commence parsing a nonterminal node in the grammar.
     */
    @Override
    public void commenceNonterminal(String nonTerminalName) {
        System.out.println("312BEGIN " + nonTerminalName);
    }

    /**
     * Finish parsing a nonterminal node in the grammar.
     */
    @Override
    public void finishNonterminal(String nonTerminalName) {
        System.out.println("312END " + nonTerminalName);
    }

    /**
     * Called once parsing the entire file completes successfully.
     */
    @Override
    public void reportSuccess() {
        System.out.println("312SUCCESS");
    }

    /**
     * Report an error and throw a CompilationException immediately.
     * 
     * The parser stops at the first error it encounters.
     */
    @Override
    public void reportError(Token token, String explanatoryMessage) 
            throws CompilationException 
    {
        // Provide a descriptive error message, including the line number,
        // which token was found, and what we expected or were trying to parse.
        String errorMsg = "SYNTAX ERROR on line " + (token.lineNumber + 1)
                + ": " + explanatoryMessage
                + " (Encountered token: " + Token.getName(token.symbol)
                + " '" + token.text + "')";
        
        // Throw the exception, carrying the message. This is caught in parse() 
        // and rethrown up the chain, allowing each method to add context.
        throw new CompilationException(errorMsg);
    }
}
