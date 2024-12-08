// Import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Driver {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        ANTLRInputStream input = new ANTLRInputStream(System.in);

        // create a lexer that feeds off of input CharStream
        LittleLexer lexer = new LittleLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        LittleParser parser = new LittleParser(tokens);

        // Remove the default console error listener
        parser.removeErrorListeners();

        VerboseListener listener = new VerboseListener();
        parser.addErrorListener(listener);

        // Start the parsing process (i.e., "program" is the start symbol) ; begin parsing at prog rule
        ParseTree tree = parser.program();
        
        // Create a generic parse tree walker that can trigger callbacks
        ParseTreeWalker walker = new ParseTreeWalker();
        
        // Create a symbol table object stb
        SimpleTableBuilder stb = new SimpleTableBuilder();
        
        walker.walk(stb, tree);

        // Create AST
        AST ast = new AST(stb);
        walker.walk(ast, tree);

        // Generate code
        Generator.newNode(ast.getRoot(), ast.getVarRoot(), stb);
    }
}

class VerboseListener extends BaseErrorListener {

    private boolean hasErrors = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        hasErrors = true;
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}