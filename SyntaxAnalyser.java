import java.io.*;

/**
 * Concrete syntax analyzer using a recursive descent parser
 * for the SCC312 coursework grammar.
 */
public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    /**
     * Constructor: create a LexicalAnalyser for the given filename 
     * and store it in this instance. 
     */
    public SyntaxAnalyser(String fileName) throws IOException {
        lex = new LexicalAnalyser(fileName);
    }

    /**
     * Accept a terminal of the specified symbol, or throw an error if it doesn't match.
     */
    @Override
    public void acceptTerminal(int symbol) 
            throws IOException, CompilationException 
    {
        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        } else {
            String expected = Token.getName(symbol);
            String found = Token.getName(nextToken.symbol);
            myGenerate.reportError(
                nextToken,
                "Expected token " + expected + " but found " + found 
            );
        }
    }

    /**
     * The parse entry point in AbstractSyntaxAnalyser calls this method
     * after retrieving the first token. We handle the top-level nonterminal here.
     */
    @Override
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<StatementPart>");
        try {
            // Grammar rule: <StatementPart> ::= begin <StatementList> end
            acceptTerminal(Token.beginSymbol);
            statementList();
            acceptTerminal(Token.endSymbol);
        } catch (CompilationException e) {
            // Wrap the exception with context for this nonterminal
            throw new CompilationException("Error while parsing <StatementPart>", e);
        }
        myGenerate.finishNonterminal("<StatementPart>");
    }

    /**
     * Parse a <StatementList>, which can be:
     * <Statement>
     * or
     * <StatementList> ; <Statement>
     */
    private void statementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<StatementList>");
        try {
            // First parse a single statement
            statement();

            // Then, while we see a semicolon, consume it and parse another statement
            while (nextToken.symbol == Token.semicolonSymbol) {
                acceptTerminal(Token.semicolonSymbol);
                statement();
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <StatementList>", e);
        }
        myGenerate.finishNonterminal("<StatementList>");
    }

    /**
     * Parse a <Statement>, which can be one of:
     * <AssignmentStatement> | <IfStatement> | <WhileStatement> 
     * | <ProcedureStatement> | <UntilStatement> | <ForStatement>
     */
    private void statement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Statement>");
        try {
            switch (nextToken.symbol) {
                case Token.identifier:
                    assignmentStatement();
                    break;
                case Token.ifSymbol:
                    ifStatement();
                    break;
                case Token.whileSymbol:
                    whileStatement();
                    break;
                case Token.callSymbol:
                    procedureStatement();
                    break;
                case Token.doSymbol:
                    untilStatement();
                    break;
                case Token.forSymbol:
                    forStatement();
                    break;
                default:
                    myGenerate.reportError(nextToken, 
                        "Invalid start of <Statement>; expecting identifier, if, while, call, do, or for");
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <Statement>", e);
        }
        myGenerate.finishNonterminal("<Statement>");
    }

    /**
     * <AssignmentStatement> ::= identifier := <Expression> 
     *                        | identifier := stringConstant
     */
    private void assignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<AssignmentStatement>");
        try {
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.becomesSymbol); // :=

            if (nextToken.symbol == Token.stringConstant) {
                acceptTerminal(Token.stringConstant);
            } else {
                expression();
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <AssignmentStatement>", e);
        }
        myGenerate.finishNonterminal("<AssignmentStatement>");
    }

    /**
     * <IfStatement> ::= 
     *      if <Condition> then <StatementList> end if
     *    | if <Condition> then <StatementList> else <StatementList> end if
     */
    private void ifStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<IfStatement>");
        try {
            acceptTerminal(Token.ifSymbol);
            condition();
            acceptTerminal(Token.thenSymbol);
            statementList();

            // Optional 'else' part
            if (nextToken.symbol == Token.elseSymbol) {
                acceptTerminal(Token.elseSymbol);
                statementList();
            }
            acceptTerminal(Token.endSymbol); // end
            acceptTerminal(Token.ifSymbol);  // if
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <IfStatement>", e);
        }
        myGenerate.finishNonterminal("<IfStatement>");
    }

    /**
     * <WhileStatement> ::= while <Condition> loop <StatementList> end loop
     */
    private void whileStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<WhileStatement>");
        try {
            acceptTerminal(Token.whileSymbol);
            condition();
            acceptTerminal(Token.loopSymbol);
            statementList();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol); // end loop
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <WhileStatement>", e);
        }
        myGenerate.finishNonterminal("<WhileStatement>");
    }

    /**
     * <ProcedureStatement> ::= call identifier ( <ArgumentList> )
     */
    private void procedureStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ProcedureStatement>");
        try {
            acceptTerminal(Token.callSymbol);
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.leftParenthesis);
            argumentList();
            acceptTerminal(Token.rightParenthesis);
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <ProcedureStatement>", e);
        }
        myGenerate.finishNonterminal("<ProcedureStatement>");
    }

    /**
     * <UntilStatement> ::= do <StatementList> until <Condition>
     */
    private void untilStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<UntilStatement>");
        try {
            acceptTerminal(Token.doSymbol);
            statementList();
            acceptTerminal(Token.untilSymbol);
            condition();
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <UntilStatement>", e);
        }
        myGenerate.finishNonterminal("<UntilStatement>");
    }

    /**
     * <ForStatement> ::= for ( <AssignmentStatement> ; <Condition> ;
     *                         <AssignmentStatement> ) do <StatementList> end loop
     */
    private void forStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ForStatement>");
        try {
            acceptTerminal(Token.forSymbol);
            acceptTerminal(Token.leftParenthesis);
            assignmentStatement();
            acceptTerminal(Token.semicolonSymbol);
            condition();
            acceptTerminal(Token.semicolonSymbol);
            assignmentStatement();
            acceptTerminal(Token.rightParenthesis);
            acceptTerminal(Token.doSymbol);
            statementList();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol); // end loop
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <ForStatement>", e);
        }
        myGenerate.finishNonterminal("<ForStatement>");
    }

    /**
     * <ArgumentList> ::= identifier | <ArgumentList> , identifier
     * 
     * Since <ArgumentList> must have at least one identifier, parse one first,
     * then loop while there's a comma.
     */
    private void argumentList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ArgumentList>");
        try {
            acceptTerminal(Token.identifier);
            while (nextToken.symbol == Token.commaSymbol) {
                acceptTerminal(Token.commaSymbol);
                acceptTerminal(Token.identifier);
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <ArgumentList>", e);
        }
        myGenerate.finishNonterminal("<ArgumentList>");
    }

    /**
     * <Condition> ::=
     *    identifier <ConditionalOperator> identifier 
     *  | identifier <ConditionalOperator> numberConstant
     *  | identifier <ConditionalOperator> stringConstant
     */
    private void condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Condition>");
        try {
            acceptTerminal(Token.identifier);
            conditionalOperator();
            switch (nextToken.symbol) {
                case Token.identifier:
                case Token.numberConstant:
                case Token.stringConstant:
                    acceptTerminal(nextToken.symbol);
                    break;
                default:
                    myGenerate.reportError(nextToken, 
                        "Expected identifier, numberConstant, or stringConstant in <Condition>");
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <Condition>", e);
        }
        myGenerate.finishNonterminal("<Condition>");
    }

    /**
     * <ConditionalOperator> ::= > | >= | = | != | < | <=
     */
    private void conditionalOperator() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ConditionalOperator>");
        try {
            switch (nextToken.symbol) {
                case Token.greaterThanSymbol:
                case Token.greaterEqualSymbol:
                case Token.equalSymbol:
                case Token.notEqualSymbol:
                case Token.lessThanSymbol:
                case Token.lessEqualSymbol:
                    acceptTerminal(nextToken.symbol);
                    break;
                default:
                    myGenerate.reportError(nextToken, 
                        "Expected a conditional operator (> >= = != < <=)");
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <ConditionalOperator>", e);
        }
        myGenerate.finishNonterminal("<ConditionalOperator>");
    }

    /**
     * <Expression> ::= <Term> 
     *                | <Expression> + <Term> 
     *                | <Expression> - <Term>
     */
    private void expression() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Expression>");
        try {
            term();
            while (nextToken.symbol == Token.plusSymbol 
                    || nextToken.symbol == Token.minusSymbol) {
                acceptTerminal(nextToken.symbol);
                term();
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <Expression>", e);
        }
        myGenerate.finishNonterminal("<Expression>");
    }

    /**
     * <Term> ::= <Factor> 
     *          | <Term> * <Factor> 
     *          | <Term> / <Factor> 
     *          | <Term> % <Factor>
     */
    private void term() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Term>");
        try {
            factor();
            while (nextToken.symbol == Token.timesSymbol 
                    || nextToken.symbol == Token.divideSymbol
                    || nextToken.symbol == Token.modSymbol) {
                acceptTerminal(nextToken.symbol);
                factor();
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <Term>", e);
        }
        myGenerate.finishNonterminal("<Term>");
    }

    /**
     * <Factor> ::= identifier | numberConstant | ( <Expression> )
     */
    private void factor() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Factor>");
        try {
            switch (nextToken.symbol) {
                case Token.identifier:
                    acceptTerminal(Token.identifier);
                    break;
                case Token.numberConstant:
                    acceptTerminal(Token.numberConstant);
                    break;
                case Token.leftParenthesis:
                    acceptTerminal(Token.leftParenthesis);
                    expression();
                    acceptTerminal(Token.rightParenthesis);
                    break;
                default:
                    myGenerate.reportError(nextToken, 
                        "Expected identifier, numberConstant, or '(' in <Factor>");
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error while parsing <Factor>", e);
        }
        myGenerate.finishNonterminal("<Factor>");
    }
}
