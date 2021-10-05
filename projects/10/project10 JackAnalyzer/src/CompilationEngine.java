import auxPack.TokenType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static auxPack.Grammar.*;

public class CompilationEngine {
    private BufferedWriter output;
    private JackTokenizer jackTokenizer;//jackTokenizer to tokenize given jack file
    //tags (dec = declaration)

    private final String CLASSVAR_DEC_TAG = "classVarDec";
    private final String VAR_DEC_TAG = "varDec";
    private final String TAG_TOKENS = "tokens";
    private final String CLASS_TAG = "class";
    private final String SUBROUTINE_DEC_TAG = "subroutineDec";
    private final String PARAMETERLIST_TAG = "parameterList";
    private final String SUBROUTINEBODY_TAG = "subroutineBody";
    private final String STATEMENTS_TAG = "statements";
    private final String LET_TAG = "letStatement";
    private final String IF_TAG = "ifStatement";
    private final String WHILE_TAG = "whileStatement";
    private final String DO_TAG = "doStatement";
    private final String RETURN_TAG = "returnStatement";
    private final String EXPRESSION_TAG = "expression";
    private final String TERM_TAG = "term";
    private final String EXPRESSIONLIST_TAG = "expressionList";

    public CompilationEngine(JackTokenizer jackTokenizer) throws IOException{
        String outputPath = jackTokenizer.getFilePath();//returns filepath that was initially tokenized
        outputPath = outputPath.replace(".jack", ".xml");//replace jack designation with .xml designation
        output = new BufferedWriter(new FileWriter(outputPath));//output is our file to write into
        this.jackTokenizer = jackTokenizer;//assign jackTokenizer to input jackTokenizer
        while (jackTokenizer.hasMoreTokens()) {//go along jackTokenizer's tokens and compiiiile!
            jackTokenizer.advance();//point at next token
            compileClass();//start wrappping
        }
        output.close();
    }

    private void compileClass() throws IOException {

        write(openTag(CLASS_TAG));//open wrap class "<class>"
        write(keyWordTag(jackTokenizer.keyWord()));//wrap class keyWord, "<keyWord> class </keyworkd>"

        //className
        advance();//advance jackTokenizer
        write(identifierTag(jackTokenizer.identifier()));//wrap identifier, ex: "<identifier> 'Point' </identifier>"

        //'{'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));//wrap appropriate symbol tag

        advance();//advance jackTokenizer
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals('}')) {//if token is symbol type and '}'
            //'}'
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol
            write(closeTag(CLASS_TAG));//end wrap class "</class>"
            return;
        }

        if (jackTokenizer.isClassVar()) {//if field or static
            //varClassDec*
            while (jackTokenizer.isClassVar()) {
                compileClassVarDec();//compile var declaraion -> varDec -> advance...
            }
        }

        if (jackTokenizer.isSubroutKeyword()) {//if constructor, function, or method
            //subroutineDec*
            compileSubroutine();//compiles subroutine (function, method, or constructor) of class
        }

        //'}'
        write(symbolTag(jackTokenizer.symbol()));//wrap symbol for closing '}'
        write(closeTag(CLASS_TAG));//close wrap class
    }

    private void compileClassVarDec() throws IOException {
        write(openTag(CLASSVAR_DEC_TAG));//open wrap nonterminal class var
        varDec();//wrap key words and identifiers for declared variables
        write(closeTag(CLASSVAR_DEC_TAG));//close wrap nonterminal class var
    }

    private void compileVarDec() throws IOException {
        write(openTag(VAR_DEC_TAG));//open wrap nonterminal var
        varDec();//wrap key words and identifiers for declared variables
        write(closeTag(VAR_DEC_TAG));//close wrap nonterminal var
    }

    private void varDec() throws IOException {//wrap key words and identifiers for declared variables
        //'static' | 'field' | 'var'
        write(keyWordTag(jackTokenizer.keyWord()));//wrap keyWord appropriately
        advance();//advance jackTokenizer
        //primitiveType

        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {//wrap constructor identifier
            write(identifierTag(jackTokenizer.identifier()));
        } else if (jackTokenizer.isPrimType()) {//else wrap primitive type
            write(keyWordTag(jackTokenizer.keyWord()));
        }

        //varName
        advance();//advance jackTokenizer
        write(identifierTag(jackTokenizer.identifier()));//wrap name of var (identifier)

        //(',',varName)*
        advance();//advance jackTokenizer
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {//while you're not at the end of the statement (not reached ';')
            //','
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag for ','
            //varName
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//wrap identifier for var name
            advance();//advance jackTokenizer
        }
        //';'
        write(symbolTag(jackTokenizer.symbol()));//end statement with symbol wrapping for ';'
        advance();//advance jackTokenizer
    }

    private void compileSubroutine() throws IOException {//compile subroutine of class by wrapping fuctions, keywords, parameters, etc.
        while (jackTokenizer.isSubroutKeyword()) {//while constructor, function, or method
            write(openTag(SUBROUTINE_DEC_TAG));//wrap open subroutine declaration

            //Function type
            write(keyWordTag(jackTokenizer.keyWord()));//wrap keyWord
            advance();//advance jackTokenizer

            if (jackTokenizer.isPrimType() || jackTokenizer.getToken().equals(VOID_KEYWORD)) {//if primitive type or void
                write(keyWordTag(jackTokenizer.keyWord()));//wrap keyWord tag
            } else {
                write(identifierTag(jackTokenizer.identifier()));//otherwise it's an identifier (constructor type) and we wrap with identifier tag
            }

            //Function name
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//wrap function name (which is an identifier) in identifier tag

            // '('
            advance();//advance jackTokenizer
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag in preparation...

            advance();//advance jackTokenizer
            //parameterlist
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {//if token is ')' symbol aka no parameters
                write(openTag(PARAMETERLIST_TAG));//open wrap paremeterlist tag
                write(closeTag(PARAMETERLIST_TAG));//close wrap parameterlist tag immediately after cause no parameters
            } else if (jackTokenizer.isPrimType()) {//else if token is primitive type
                compileParameterList();//wrap contents of parameter list (keywords, identifiers, commas, etc.)
                //After returning, the current token must be')'
            }
            // ')'
            write(symbolTag(jackTokenizer.symbol()));//finished param list so wrap symbol tag for ')'

            //Function body (including "{}")
            advance();//advance jackTokenizer
            compileSubroutineBody();//
            write(closeTag(SUBROUTINE_DEC_TAG));//close wrap subroutine tag
        }

    }

    private void compileParameterList() throws IOException {//compile parameter list of subroutine call
        write(openTag(PARAMETERLIST_TAG));//open wrap parameterlist tag
        //arg type
        if (jackTokenizer.tokenType() == TokenType.KEYWORD) {//if token is keyWord type
            write(keyWordTag(jackTokenizer.keyWord()));//wrap keyWord tag
        } else {
            write(identifierTag(jackTokenizer.identifier()));//else identifier token (constructor type) so wrap identifier tag
        }

        //identifier
        advance();//advance jackTokenizer
        write(identifierTag(jackTokenizer.identifier()));//identifier wrapper for args
        advance();//advance jackTokenizer
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol().equals(")"))) {//while not closing ')' aka still giving args
            //','
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol cause it's a comma next
            //primitive type
            advance();//advance jackTokenizer
            if (jackTokenizer.tokenType() == TokenType.KEYWORD) {//if token is keyWord type
                write(keyWordTag(jackTokenizer.keyWord()));//wrap keyWord tag
            } else {
                write(identifierTag(jackTokenizer.identifier()));//else identifier token (constructor type) so wrap identifier tag
            }

            //identifier
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//wrap identifier (around name of arg)
            advance();//advance jackTokenizer
        }
        write(closeTag(PARAMETERLIST_TAG));//close wrap parameterlist
    }

    private void compileSubroutineBody() throws IOException {//compile body of subroutine (after parameters are given)
        write(openTag(SUBROUTINEBODY_TAG));//wrap open tag for subroutine body

        //'{'
        write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag for '{'

        advance();//advance jackTokenizer
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals('}')) {//if end of subroutine body aka '}'
            //'}'
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag
            write(closeTag(SUBROUTINEBODY_TAG));//close wrap subroutine body tag after '}'
            advance();//advance jackTokenizer
            return;
        }

        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.keyWord().equals(VAR_KEYWORD)) {//if token is var keyWord
            //varDec*
            while ((jackTokenizer.tokenType() == TokenType.KEYWORD &&
                    jackTokenizer.keyWord().equals(VAR_KEYWORD))) {//declare subroutine variable
                compileVarDec();//compile subroutine variable declaration aka wrappin' taaaaags
            }
        }

        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.isStatement()) {//token is statement type (let, if, while, do, return)
            //statements
            compileStatements();//compile those statements with the wrapping and the tags and teh good timez
        }
        //'}'
        write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag for closing '}' end of subroutine body

        write(closeTag(SUBROUTINEBODY_TAG));//close wrap subroutine body tag
        advance();//advance jackTokenizer
    }

    private void compileStatements() throws IOException {
        write(openTag(STATEMENTS_TAG));
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("}"))) {
            switch (jackTokenizer.keyWord()) {//compile statement based on opening statement
                case LET_KEYWORD:
                    compileLet();
                    break;
                case IF_KEYWORD:
                    compileIf();
                    break;
                case WHILE_KEYWORD:
                    compileWhile();
                    break;
                case DO_KEYWORD:
                    compileDo();
                    break;
                case RETURN_KEYWORD:
                    compileReturn();
                    break;
            }
        }
        write(closeTag(STATEMENTS_TAG));//close wrap statements tag
    }

    private void compileLet() throws IOException {
        write(openTag(LET_TAG));
        //'let'
        write(keyWordTag(jackTokenizer.keyWord()));
        //varName
        advance();
        write(identifierTag(jackTokenizer.identifier()));
        //('['expression']')?
        advance();
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol() == "[") {//for possible array index declaration
            //'['
            write(symbolTag(jackTokenizer.symbol()));//open bracket
            advance();
            compileExpression();
            //']'
            write(symbolTag(jackTokenizer.symbol()));//close bracket
            advance();
        }

        //'='
        write(symbolTag(jackTokenizer.symbol()));

        //expression
        advance();//advance jackTokenizer
        compileExpression();

        //';'
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(LET_TAG));
        advance();//advance jackTokenizer
    }

    //After calling this method, the parent program does not need advance
    private void compileDo() throws IOException {
        write(openTag(DO_TAG));
        //'do'
        write(keyWordTag(jackTokenizer.keyWord()));
        //subroutineCall
        advance();//advance jackTokenizer
        //subroutineName | className |varName
        write(identifierTag(jackTokenizer.identifier()));

        //'(' | '.'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));

        if (jackTokenizer.symbol().equals("(")) {
            //expressionList
            advance();//advance jackTokenizer
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(openTag(EXPRESSIONLIST_TAG));
                write(closeTag(EXPRESSIONLIST_TAG));
            } else {
                compileExpressionList();
            }
        } else if (jackTokenizer.symbol().equals(".")) {
            //subroutineName
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));
            //'('
            advance();//advance jackTokenizer
            write(symbolTag(jackTokenizer.symbol()));
            //expressionList
            advance();//advance jackTokenizer
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(openTag(EXPRESSIONLIST_TAG));
                write(closeTag(EXPRESSIONLIST_TAG));
            } else {
                compileExpressionList();
            }
        }

        //')'
        write(symbolTag(jackTokenizer.symbol()));
        //';'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(DO_TAG));
        advance();//advance jackTokenizer
    }

    private void compileExpressionList() throws IOException {
        write(openTag(EXPRESSIONLIST_TAG));
        //expression
        compileExpression();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")"))) {
            //','
            write(symbolTag(jackTokenizer.symbol()));
            //expression
            advance();//advance jackTokenizer
            compileExpression();
        }
        write(closeTag(EXPRESSIONLIST_TAG));
    }

    //After calling this method, the parent program does not need advance
    private void compileWhile() throws IOException {
        write(openTag(WHILE_TAG));
        //'while'
        write(keyWordTag(jackTokenizer.keyWord()));
        //'('
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //expression
        advance();//advance jackTokenizer
        compileExpression();
        //')'
        write(symbolTag(jackTokenizer.symbol()));
        //'{'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //statements
        advance();//advance jackTokenizer
        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.isStatement()) {
            compileStatements();
        }
        //'}'
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(WHILE_TAG));
        advance();//advance jackTokenizer
    }

    private void compileReturn() throws IOException {
        write(openTag(RETURN_TAG));
        //'return'
        write(keyWordTag(jackTokenizer.keyWord()));
        //expression?
        advance();//advance jackTokenizer
        if (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {
            compileExpression();
        }
        //';'
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(RETURN_TAG));
        advance();//advance jackTokenizer
    }

    //After calling this method, the parent program does not need advance
    private void compileIf() throws IOException {
        write(openTag(IF_TAG));
        //'if'
        write(keyWordTag(jackTokenizer.keyWord()));
        //'('
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //expression
        advance();//advance jackTokenizer
        compileExpression();
        //')'
        write(symbolTag(jackTokenizer.symbol()));
        //'{'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //statements
        advance();//advance jackTokenizer
        if (jackTokenizer.isStatement()) {
            compileStatements();
        }
        //'}'
        write(symbolTag(jackTokenizer.symbol()));

        advance();//advance jackTokenizer
        if (jackTokenizer.tokenType()==TokenType.KEYWORD && jackTokenizer.keyWord().equals("else")) {
            //'else'
            write(keyWordTag(jackTokenizer.keyWord()));
            //'{'
            advance();//advance jackTokenizer
            write(symbolTag(jackTokenizer.symbol()));
            //statements
            advance();//advance jackTokenizer
            compileStatements();
            //'}'
            write(symbolTag(jackTokenizer.symbol()));
            advance();//advance jackTokenizer
        }
        write(closeTag(IF_TAG));
    }

    //After calling this method, the parent program does not need advance
    private void compileExpression() throws IOException {
        write(openTag(EXPRESSION_TAG));//open wrap expression tag
        compileTerm();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                (jackTokenizer.symbol().equals(";") ||
                        jackTokenizer.symbol().equals(")") ||
                        jackTokenizer.symbol().equals("]") ||
                        jackTokenizer.symbol().equals(",")))) {
            //operator symbol
            write(symbolTag(jackTokenizer.symbol()));
            //term
            advance();//advance jackTokenizer
            compileTerm();
        }
        write(closeTag(EXPRESSION_TAG));
    }

    private void compileTerm() throws IOException {
        write(openTag(TERM_TAG));//open wrap term tag

        switch (jackTokenizer.tokenType()) {//wrap appropriate tag
            case KEYWORD:
                write(keyWordTag(jackTokenizer.keyWord()));
                advance();
                break;
            case INT_CONST:
                write(intConstTag(String.valueOf(jackTokenizer.intVal())));
                advance();//advance jackTokenizer
                break;
            case STRING_CONST:
                write(strConstTag(jackTokenizer.stringVal()));
                advance();//advance jackTokenizer
                break;
            case SYMBOL:
                if (jackTokenizer.isUnOp()) {
                    //'unaryOp'
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                    compileTerm();
                } else {
                    //"("
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                    compileExpression();
                    //")"
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                }
                break;
            case IDENTIFIER:
                write(identifierTag(jackTokenizer.identifier()));
                advance();
                if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                        jackTokenizer.symbol().equals(".") || jackTokenizer.symbol().equals("(")) {

                    write(symbolTag(jackTokenizer.symbol()));

                    if (jackTokenizer.symbol().equals("(")) {
                        //expressionList
                        advance();//advance jackTokenizer
                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(openTag(EXPRESSIONLIST_TAG));
                            write(closeTag(EXPRESSIONLIST_TAG));
                            advance();//advance jackTokenizer
                        } else {
                            compileExpressionList();
                        }
                    } else if (jackTokenizer.symbol().equals(".")) {
                        //subroutineName
                        advance();//advance jackTokenizer
                        write(identifierTag(jackTokenizer.identifier()));
                        //'('
                        advance();//advance jackTokenizer
                        write(symbolTag(jackTokenizer.symbol()));
                        //expressionList
                        advance();//advance jackTokenizer
                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(openTag(EXPRESSIONLIST_TAG));
                            write(closeTag(EXPRESSIONLIST_TAG));
                        } else {
                            compileExpressionList();
                        }
                        //')'
                        write(symbolTag(jackTokenizer.symbol()));
                        advance();
                    }
                } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("[")) {
                    //'['expression']'
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                    compileExpression();
                    //']'
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                }
                break;
        }

        write(closeTag(TERM_TAG));
    }

    private void advance() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
        } else {
            throw new RuntimeException("there are no more tokens");
        }
    }

    private void write(String str) throws IOException {

        output.write(str);
        output.newLine();

    }

    public static String parenthesesWrap(String str) {
        return "(" + str + ")";
    }
    public static String QuoteWrap(String str) {
        return "\"" + str + "\"";
    }

    public static String keyWordTag(String str) { return tag(str, "keyword"); }

    public static String symbolTag(String str) {
        return tag(str, "symbol");
    }

    public static String identifierTag(String str) {
        return tag(str, "identifier");
    }

    public static String intConstTag(String str) {
        return tag(str, "integerConstant");
    }

    public static String strConstTag(String str) {
        return tag(str, "stringConstant");
    }

    public static String tag(String str, String tag) {
        return openTag(tag) + str + closeTag(tag);
    }

    public static String openTag(String str) {
        return "<" + str + "> ";
    }

    public static String closeTag(String str) {
        return " </" + str + ">";
    }
}