import auxPack.TokenType;

import auxPack.Kind;
import auxPack.VMGrammar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import static auxPack.XMLGrammar.*;

public class CompilationEngine {
    private BufferedWriter output;
    private JackTokenizer jackTokenizer;//jackTokenizer to tokenize given jack file
    //tags (dec = declaration)
    private SymbolTable symbolTable;//declare symbol table to put in subroutine-level or class-level symbols during runtime
    private VMWriter vmWriter;//declare vmWriter to output parsed vm code
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
    private final String WHILE_START = "WHILE_EXP";
    private final String WHILE_END = "WHILE_END";
    private final String IF_LABEL = "IF_LABEL";

    private int ifCounter;//keep track of if labels
    private int whileCounter;//keep track of while labels
    private String subName;//will be set to token representing subroutine name
    private String subType;//will be set to token representing subroutine type
    private String className;//will be set to token representing name of class


    public CompilationEngine(JackTokenizer jackTokenizer) throws IOException{
        String outputPath = jackTokenizer.getFilePath();//returns filepath that was initially tokenized
        outputPath = outputPath.replace(".jack", ".xml");//replace jack designation with .xml designation
        output = new BufferedWriter(new FileWriter(outputPath));//output is our file to write into
        ifCounter = 0;//init if counter
        whileCounter = 0;//init while counter
        vmWriter = new VMWriter(jackTokenizer.getFilePath());//init vmWriter
        this.jackTokenizer = jackTokenizer;//assign jackTokenizer to input jackTokenizer
        while (jackTokenizer.hasMoreTokens()) {//go along jackTokenizer's tokens and compiiiile!
            jackTokenizer.advance();//point at next token
            compileClass();//start wrappping
        }
        output.close();

        vmWriter.close();
    }

    private void compileClass() throws IOException {
        //create symbol table
        symbolTable = new SymbolTable();//(re)init empty symbol table
        symbolTable.pointClassScope();//point to class symbol table

        write(openTag(CLASS_TAG));//open wrap class "<class>"
        write(keyWordTag(jackTokenizer.keyword()));//wrap class keyword, "<keyword> class </keyworkd>"

        //className
        advance();//advance jackTokenizer
        write(identifierTag(jackTokenizer.identifier()));//wrap identifier, ex: "<identifier> 'Point' </identifier>"
        this.className = jackTokenizer.identifier();//currently pointing at class name token (which is identifier) so set 'className' to it
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
                compileClassVarDec();//compile var declaration -> varDec -> advance...
            }
        }

        if (jackTokenizer.isSubroutKeyword()) {//if constructor, function, or method
            //subroutineDec*
            symbolTable.pointSubroutineScope();//point to subroutine scope
            compileSubroutine();//compiles subroutine (function, method, or constructor) of class
            symbolTable.pointClassScope();//point to class symbol table
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
        String name, type, kind;//declare statement attributes
        //static or field or var
        kind = jackTokenizer.keyword();//set 'kind' (field, etc.)
        write(keyWordTag(jackTokenizer.keyword()));//wrap keyword appropriately
        advance();//advance jackTokenizer
        //primitiveType
        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {//wrap constructor identifier
            type = jackTokenizer.identifier();//if constructor type
            write(identifierTag(jackTokenizer.identifier()));//if object type rather than primitive etc.
        } else {//else wrap primitive type
            type = jackTokenizer.keyword();//if primitive type
            write(keyWordTag(jackTokenizer.keyword()));//if primitive type
        }

        //varName
        advance();//advance jackTokenizer
        write(identifierTag(jackTokenizer.identifier()));//wrap name of var (identifier)
        name = jackTokenizer.identifier();//set 'name' to current identifier token

        //put in symbolTable
        symbolTable.define(name, type, kind);

        //(',',varName)*
        advance();//advance jackTokenizer
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {//while you're not at the end of the statement (not reached ';')
            //','
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag for ','
            //varName
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//wrap identifier for var name

            name = jackTokenizer.identifier();//set 'name' to current identifier token
            symbolTable.define(name, type, kind);//put in symbolTable

            advance();//advance jackTokenizer
        }
        //';'
        write(symbolTag(jackTokenizer.symbol()));//end statement with symbol wrapping for ';'
        advance();//advance jackTokenizer
    }

    private void compileSubroutine() throws IOException {//compile subroutine of class by wrapping fuctions, keywords, parameters, etc.
        while (jackTokenizer.isSubroutKeyword()) {//while constructor, function, or method
            symbolTable.startSubroutine();//clear subroutine table
            symbolTable.pointSubroutineScope();//point to newly cleared subroutine table

            write(openTag(SUBROUTINE_DEC_TAG));//wrap open subroutine declaration

            //Function type
            write(keyWordTag(jackTokenizer.keyword()));//wrap keyword
            this.subType = jackTokenizer.keyword();//set 'subType' to current keyword token

            //add this, className to sub table if method call
            if (this.subType.equals(METHOD_KEYWORD)) {
                symbolTable.define(VMGrammar.THIS, this.className, "arg");
            }
            advance();//advance jackTokenizer

            if (jackTokenizer.isPrimType() || jackTokenizer.getToken().equals(VOID_KEYWORD)) {//if primitive type or void
                write(keyWordTag(jackTokenizer.keyword()));//wrap keyword tag
            } else {
                write(identifierTag(jackTokenizer.identifier()));//otherwise it's an identifier (constructor type) and we wrap with identifier tag
            }

            //Function name
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//wrap function name (which is an identifier) in identifier tag
            this.subName = jackTokenizer.identifier();//set 'subName' to current identifier token

            // '('
            advance();//advance jackTokenizer
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag in preparation...

            advance();//advance jackTokenizer
            //parameterlist
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {//if token is ')' symbol aka no parameters
                write(openTag(PARAMETERLIST_TAG));//open wrap paremeterlist tag
                write(closeTag(PARAMETERLIST_TAG));//close wrap parameterlist tag immediately after cause no parameters
            } else if (jackTokenizer.isPrimType() || jackTokenizer.tokenType()==TokenType.IDENTIFIER) {//else if token is primitive type or constructor type
                compileParameterList();//wrap contents of parameter list (keywords, identifiers, commas, etc.)
                //After returning, the current token must be')'
            }
            // ')'
            write(symbolTag(jackTokenizer.symbol()));//finished param list so wrap symbol tag for ')'

            //Function body (including '{}')
            advance();//advance jackTokenizer
            compileSubroutineBody();//compiles the contents of the current subroutine
            write(closeTag(SUBROUTINE_DEC_TAG));//close wrap subroutine tag
        }

    }

    private void compileParameterList() throws IOException {//compile parameter list of subroutine call
        write(openTag(PARAMETERLIST_TAG));//open wrap parameterlist tag
        String firstType;//declare first parameter type
        //arg type
        if (jackTokenizer.tokenType() == TokenType.KEYWORD) {//if token is keyword type
            write(keyWordTag(jackTokenizer.keyword()));//wrap keyword tag
            firstType = jackTokenizer.keyword();//if current token is keyword type
        } else {
            write(identifierTag(jackTokenizer.identifier()));//else identifier token (constructor type) so wrap identifier tag
            firstType = jackTokenizer.identifier();//if identifier type
        }

        //identifier
        advance();//advance jackTokenizer
        write(identifierTag(jackTokenizer.identifier()));//identifier wrapper for args

        symbolTable.define(jackTokenizer.identifier(), firstType, "arg");//add argument to subroutine table

        advance();//advance jackTokenizer
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol().equals(")"))) {//while not closing ')' aka still giving args
            //','
            write(symbolTag(jackTokenizer.symbol()));//wrap symbol cause it's a comma next
            //primitive type
            advance();//advance jackTokenizer
            String currType;//declare type of following parameter
            if (jackTokenizer.tokenType() == TokenType.KEYWORD) {//if token is keyword type
                write(keyWordTag(jackTokenizer.keyword()));//wrap keyword tag
                currType = jackTokenizer.keyword();//init current parameter type if keyword
            } else {
                write(identifierTag(jackTokenizer.identifier()));//else identifier token (constructor type) so wrap identifier tag
                currType = jackTokenizer.identifier();//int current paremeter type if identifier
            }

            //identifier
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//wrap identifier (around name of arg)

            symbolTable.define(jackTokenizer.identifier(), currType, "arg");//add current parameter arg to subroutine table

            advance();//advance jackTokenizer
        }
        write(closeTag(PARAMETERLIST_TAG));//close wrap parameterlist
    }

    private void compileSubroutineBody() throws IOException {//compile body of subroutine (after parameters are given)
        write(openTag(SUBROUTINEBODY_TAG));//wrap open tag for subroutine body

        write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag for '{'

        advance();//advance jackTokenizer
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals('}')) {//if end of subroutine body aka '}'

            write(symbolTag(jackTokenizer.symbol()));//wrap symbol tag
            write(closeTag(SUBROUTINEBODY_TAG));//close wrap subroutine body tag after '}'
            advance();//advance jackTokenizer
            return;
        }

        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.keyword().equals(VAR_KEYWORD)) {//if token is var keyword
            //varDec*
            while ((jackTokenizer.tokenType() == TokenType.KEYWORD &&
                    jackTokenizer.keyword().equals(VAR_KEYWORD))) {//declare subroutine variable
                compileVarDec();//compile subroutine variable declaration aka wrappin' taaaaags
            }
        }

        vmWriter.writeFunction(this.subName, symbolTable.variableTotal(Kind.VAR));//write function to VM file in form 'function x.y variableTotal'
        if (this.subType.equals(CONSTRUCTOR_KEYWORD)) {//check if constructor method

            //look up class level symbol table in order to count fields for object creation
            symbolTable.pointClassScope();//point to class symbol table

            //53:00
            vmWriter.writePush(VMGrammar.CONST, symbolTable.variableTotal(Kind.FIELD));//push number of fields 'push constant 'total''
            vmWriter.writeCall("Memory.alloc", 1);//allocate memory to new constructor object. We have on stack the base address of the object.
            vmWriter.writePop(VMGrammar.POINTER, 0);//we pop it into pointer zero. we set 'this' to the address of the object we just created

            symbolTable.pointSubroutineScope();//return to subroutine symbol table

        } else if (this.subType.equals(METHOD_KEYWORD)) {
            vmWriter.writePush(VMGrammar.ARG, 0);//push object
            vmWriter.writePop(VMGrammar.POINTER, 0);//retrieve from stack THIS = argument 0

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
            switch (jackTokenizer.keyword()) {//compile statement based on opening statement
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
        write(keyWordTag(jackTokenizer.keyword()));
        //varName
        advance();
        write(identifierTag(jackTokenizer.identifier()));
        String name = jackTokenizer.identifier();//assign name identifier to be assigned expression
        //('['expression']')?
        boolean isArr = false;//default not array
        advance();
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol() == "[") {//for possible array index declaration
            isArr = true;//dealing with array in this conditional
            varPush(name);//push array
            //'['
            write(symbolTag(jackTokenizer.symbol()));//open bracket
            advance();
            //push content at index
            compileExpression();
            //']'
            write(symbolTag(jackTokenizer.symbol()));//close bracket
            advance();
            vmWriter.writeArithmetic("+");//add aka get to address of index of arr
        }

        //'='
        write(symbolTag(jackTokenizer.symbol()));

        //expression
        advance();//advance jackTokenizer
        compileExpression();//compile expression on ride side of '=' sign

        //';'
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(LET_TAG));
        advance();//advance jackTokenizer

        //write up current 'let' statement in vm code
        switch (symbolTable.kindOf(name)) {
            case ARG:
                if (!isArr) {
                    vmWriter.writePop(VMGrammar.ARG, symbolTable.indexOf(name));
                } else {
                    accessArray();
                }
                break;
            case VAR:
                if (!isArr) {
                    vmWriter.writePop(VMGrammar.LOCAL, symbolTable.indexOf(name));
                } else {
                    accessArray();
                }
                break;
            case NONE:
                symbolTable.pointClassScope();//point to class symbol table
                switch (symbolTable.kindOf(name)) {
                    case STATIC:
                        vmWriter.writePop(VMGrammar.STATIC, symbolTable.indexOf(name));
                        break;
                    case FIELD:
                        if (!isArr) {
                            vmWriter.writePop(VMGrammar.THIS, symbolTable.indexOf(name));
                        } else {
                            accessArray();
                        }
                        break;
                    case NONE:
                        throw new RuntimeException("unknown pop kind");
                }
                symbolTable.pointSubroutineScope();//point back to subroutine level table
                break;
        }
    }

    private void compileDo() throws IOException {
        write(openTag(DO_TAG));
        //'do'
        write(keyWordTag(jackTokenizer.keyword()));
        //subroutineCall
        advance();//advance jackTokenizer
        //subroutineName or className or varName
        write(identifierTag(jackTokenizer.identifier()));
        String name0 = jackTokenizer.identifier();//name of method or name of class/constructor (ex: 'do moveBall();' | 'do bat.move();'

        //'(' or '.'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));

        //check if '(' -
        if (jackTokenizer.symbol().equals("(")) {
            vmWriter.writePush(VMGrammar.POINTER, 0);//push result of do into 'this'

            int nArgs = 0;//number of args given to method being do'd
            //expressionList
            advance();
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {//end expression in method
                write(openTag(EXPRESSIONLIST_TAG));
                write(closeTag(EXPRESSIONLIST_TAG));
            } else {
                nArgs = compileExpressionList();
            }

            //if not "function"
            if (!this.subType.equals(FUNCTION_KEYWORD)) {
                vmWriter.writeCall(this.className + "." + name0, nArgs + 1);
            }
        } else if (jackTokenizer.symbol().equals(".")) {//ex: 'do bat.move()';
            advance();//advance jackTokenizer
            write(identifierTag(jackTokenizer.identifier()));//subroutine name
            String name1 = jackTokenizer.identifier();//ex: .'move' from ex above
            varPush(name0);//push variable to stack
            //'('
            advance();//advance jackTokenizer
            write(symbolTag(jackTokenizer.symbol()));
            advance();//advance jackTokenizer
            int nArgs = 0;
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {//if no args
                write(openTag(EXPRESSIONLIST_TAG));
                write(closeTag(EXPRESSIONLIST_TAG));
            } else {
                nArgs = compileExpressionList();//get num of args
            }

            call(name0, name1, nArgs);//get num of args
        }

        //')'
        write(symbolTag(jackTokenizer.symbol()));
        //';'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(DO_TAG));
        advance();//advance jackTokenizer

        vmWriter.writePop(VMGrammar.TEMP, 0);//finished do statement, pop return value
    }

    private int compileExpressionList() throws IOException {
        int nArgs = 1;//init number of args at 1

        write(openTag(EXPRESSIONLIST_TAG));
        //expression
        compileExpression();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")"))) {
            //','
            write(symbolTag(jackTokenizer.symbol()));
            //expression
            advance();//advance jackTokenizer
            compileExpression();
            nArgs++;//add to nArgs every time expression is compiled from expression list
        }
        write(closeTag(EXPRESSIONLIST_TAG));
        return nArgs;
    }

    private void compileWhile() throws IOException {
        String whileLabel1 = WHILE_START + whileCounter;//initialize first label
        String whileLabel2 = WHILE_END + whileCounter++;//init second label

        vmWriter.writeLabel(whileLabel1);//write label

        write(openTag(WHILE_TAG));
        //'while'
        write(keyWordTag(jackTokenizer.keyword()));
        //'('
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //expression
        advance();//advance jackTokenizer
        compileExpression();

        vmWriter.writeArithmetic("~");//write not to check if while condition is false
        vmWriter.writeIf(whileLabel2);//write while end label to jump to if condition is false

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

        vmWriter.writeGoto(whileLabel1);//go back to beginning of while loop
        //'}'
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(WHILE_TAG));
        advance();//advance jackTokenizer

        vmWriter.writeLabel(whileLabel2);//write end label to jump to
    }

    private void compileReturn() throws IOException {
        write(openTag(RETURN_TAG));
        //'return'
        write(keyWordTag(jackTokenizer.keyword()));
        advance();//advance jackTokenizer
        if (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {//compile expression if not isolated 'return;'
            compileExpression();
        }
        else {
            vmWriter.writePush(VMGrammar.CONST, 0);//push const 0 in case of 'return;'
        }

        vmWriter.writeReturn();//write return :)
        //';'
        write(symbolTag(jackTokenizer.symbol()));
        write(closeTag(RETURN_TAG));
        advance();//advance jackTokenizer
    }

    //After calling this method, the parent program does not need advance
    private void compileIf() throws IOException {
        write(openTag(IF_TAG));
        //'if'
        write(keyWordTag(jackTokenizer.keyword()));
        //'('
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //expression
        advance();//advance jackTokenizer
        compileExpression();
        //')'
        write(symbolTag(jackTokenizer.symbol()));

        vmWriter.writeArithmetic("~");//write not so that we can check if 'if' condition is false
        String ifLabel1 = IF_LABEL + ifCounter++;//label to jump to end of if | else statement
        vmWriter.writeIf(ifLabel1);//write else/end of conditional label

        //'{'
        advance();//advance jackTokenizer
        write(symbolTag(jackTokenizer.symbol()));
        //statements in 'if' body
        advance();//advance jackTokenizer
        if (jackTokenizer.isStatement()) {
            compileStatements();
        }

        String ifLabel2 = IF_LABEL + ifCounter++;//label for end of current conditional block
        vmWriter.writeGoto(ifLabel2);//goto end of current conditional block

        //'}'
        write(symbolTag(jackTokenizer.symbol()));

        advance();//advance jackTokenizer
        boolean Else = false;//check if else
        if (jackTokenizer.tokenType()==TokenType.KEYWORD && jackTokenizer.keyword().equals("else")) {
            Else = true;//in else block
            write(keyWordTag(jackTokenizer.keyword()));
            //'{'
            advance();//advance jackTokenizer
            write(symbolTag(jackTokenizer.symbol()));

            vmWriter.writeLabel(ifLabel1);//label to jump to else block
            advance();//advance jackTokenizer
            compileStatements();
            //'}'
            write(symbolTag(jackTokenizer.symbol()));
            advance();//advance jackTokenizer
        }
        if (!Else) {
            vmWriter.writeLabel(ifLabel1);//end of entire conditional label
        }
        write(closeTag(IF_TAG));

        vmWriter.writeLabel(ifLabel2);//end of conditional
    }

    private void compileExpression() throws IOException {
        write(openTag(EXPRESSION_TAG));//open wrap expression tag
        compileTerm();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                (jackTokenizer.symbol().equals(";") ||
                        jackTokenizer.symbol().equals(")") ||
                        jackTokenizer.symbol().equals("]") ||
                        jackTokenizer.symbol().equals(",")))) {
            String op = jackTokenizer.symbol();//op symbol
            write(symbolTag(jackTokenizer.symbol()));//operator following previously compiled term
            advance();//advance jackTokenizer
            compileTerm();//next term
            //possible operators
            if (op.equals("*")) {
                vmWriter.writeCall("Math.multiply", 2);
            } else if (op.equals("/")) {
                vmWriter.writeCall("Math.divide", 2);
            } else {
                switch (op) {
                    case "&gt;":
                        vmWriter.writeArithmetic(">");
                        break;
                    case "&lt;":
                        vmWriter.writeArithmetic("<");
                        break;
                    case "&amp;":
                        vmWriter.writeArithmetic("&");
                        break;
                    default:
                        vmWriter.writeArithmetic(op);
                        break;
                }
            }
        }
        write(closeTag(EXPRESSION_TAG));
    }

    private void compileTerm() throws IOException {
        write(openTag(TERM_TAG));//open wrap term tag

        switch (jackTokenizer.tokenType()) {//wrap appropriate tag
            case KEYWORD:
                write(keyWordTag(jackTokenizer.keyword()));
                switch (jackTokenizer.keyword()) {
                    case "null":
                    case "false":
                        vmWriter.writePush(VMGrammar.CONST, 0);//null/false = 0
                        break;
                    case "true":
                        vmWriter.writePush(VMGrammar.CONST, 1);//write 1 then negate
                        vmWriter.writeArithmetic("_");//true = -1
                        break;
                    case "this":
                        vmWriter.writePush(VMGrammar.POINTER, 0);//push pointer 0 for 'this'
                        break;
                }
                advance();
                break;
            case INT_CONST:
                write(intConstTag(String.valueOf(jackTokenizer.intVal())));
                vmWriter.writePush(VMGrammar.CONST, jackTokenizer.intVal());//push constant 'int'
                advance();//advance jackTokenizer
                break;
            case STRING_CONST:

                write(strConstTag(jackTokenizer.stringVal()));
                String stringConstant = jackTokenizer.stringVal();//get string

                vmWriter.writePush(VMGrammar.CONST, stringConstant.length());//push constant 'len of string'
                vmWriter.writeCall("String.new", 1);//call string.new with 1 arg
                //write string using appendChar method
                for (int i = 0; i < stringConstant.length(); i++) {
                    char c = stringConstant.charAt(i);
                    vmWriter.writePush(VMGrammar.CONST,c);
                    vmWriter.writeCall("String.appendChar", 2);
                }

                advance();
                break;
            case SYMBOL:
                if (jackTokenizer.isUnOp()) {
                    //if unaryOp is '-' or '~'
                    write(symbolTag(jackTokenizer.symbol()));
                    String unaryOp = jackTokenizer.symbol();//set unaryOp to current token
                    advance();//advance jackTokenizer
                    compileTerm();//compile term following operation
                    if (unaryOp.equals("-")) {
                        vmWriter.writeArithmetic("_");//write neg
                    } else {
                        vmWriter.writeArithmetic(unaryOp);//write sub
                    }
                } else {
                    //'('
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                    compileExpression();
                    //')'
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                }
                break;
            case IDENTIFIER:
                String name0 = jackTokenizer.identifier();
                write(identifierTag(jackTokenizer.identifier()));
                boolean soloVariable = true;//variable rollin' solo?
                advance();
                if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                        jackTokenizer.symbol().equals(".") || jackTokenizer.symbol().equals("(")) {//ex: bat.move() || move()
                    soloVariable = false;//variable's got some friends!
                    write(symbolTag(jackTokenizer.symbol()));

                    if (jackTokenizer.symbol().equals("(")) {//ex: move()
                        if (this.subType.equals(CONSTRUCTOR_KEYWORD)) {
                            vmWriter.writePush(VMGrammar.POINTER, 0);//'this' in stack cause constructor
                        } else if (subType.equals(METHOD_KEYWORD)) {
                            vmWriter.writePush(VMGrammar.ARG, 0);//push arg 0 cause method
                        }

                        int nArgs = 0;//set num of args to 0
                        advance();//advance jackTokenizer
                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {//end expressionList
                            write(openTag(EXPRESSIONLIST_TAG));
                            write(closeTag(EXPRESSIONLIST_TAG));
                            advance();//advance jackTokenizer
                        } else {
                            nArgs = compileExpressionList();//get num of args
                        }

                        //if function
                        if (!this.subType.equals(FUNCTION_KEYWORD)) {
                            vmWriter.writeCall(this.className + "." + name0, nArgs + 1);
                        }
                    } else if (jackTokenizer.symbol().equals(".")) {//ex: bat.move()
                        advance();//advance jackTokenizer
                        write(identifierTag(jackTokenizer.identifier()));
                        String name1 = jackTokenizer.identifier();//name of subroutine

                        //'('
                        advance();//advance jackTokenizer
                        write(symbolTag(jackTokenizer.symbol()));
                        advance();//advance jackTokenizer
                        int nArgs = 0;//set args to 0 for new expressionList
                        varPush(name0);//push variable to stack

                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {//end expressionList
                            write(openTag(EXPRESSIONLIST_TAG));
                            write(closeTag(EXPRESSIONLIST_TAG));
                        } else {
                            nArgs = compileExpressionList();//get num of args
                        }
                        //')'
                        write(symbolTag(jackTokenizer.symbol()));
                        advance();

                        call(name0, name1, nArgs);//get num of args
                    }
                } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("[")) {//array!
                    soloVariable = false;//homeys!
                    varPush(name0);//push variable to stack

                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer
                    compileExpression();//expression in '[]'
                    //']'
                    write(symbolTag(jackTokenizer.symbol()));
                    advance();//advance jackTokenizer

                    vmWriter.writeArithmetic("+");//add
                    vmWriter.writePop(VMGrammar.POINTER, 1);//pop pointer 1
                    vmWriter.writePush(VMGrammar.THAT, 0);//push that 0 (cause we're dealing with arrays so not using 'this')
                }

                if (soloVariable) {//lonely var to push in stack
                    //subroutine level
                    switch (symbolTable.kindOf(name0)) {
                        case ARG:
                            vmWriter.writePush(VMGrammar.ARG, symbolTable.indexOf(name0));
                            break;
                        case VAR:
                            vmWriter.writePush(VMGrammar.LOCAL, symbolTable.indexOf(name0));
                            break;
                        case NONE:
                            //class level
                            symbolTable.pointClassScope();//point to class symbol table
                            switch (symbolTable.kindOf(name0)) {
                                case STATIC:
                                    vmWriter.writePush(VMGrammar.STATIC, symbolTable.indexOf(name0));
                                    break;
                                case FIELD:
                                    vmWriter.writePush(VMGrammar.THIS, symbolTable.indexOf(name0));
                                    break;
                            }
                            symbolTable.pointSubroutineScope();
                            break;
                    }
                }
                break;
        }

        write(closeTag(TERM_TAG));
    }

    //push variable to stack
    private void varPush(String name) throws IOException {
        if (symbolTable.kindOf(name) != Kind.NONE) {//if subroutine level
            if (symbolTable.kindOf(name)==Kind.ARG) {//if arg kind
                vmWriter.writePush(VMGrammar.ARG, symbolTable.indexOf(name));//write: push argument 'index'
            } else if (symbolTable.kindOf(name) == Kind.VAR) {
                vmWriter.writePush(VMGrammar.LOCAL, symbolTable.indexOf(name));//write: push local 'index'
            }
        } else {
            symbolTable.pointClassScope();//point to class symbol table
            if (symbolTable.kindOf(name) != Kind.NONE) {
                vmWriter.writePush(VMGrammar.THIS, symbolTable.indexOf(name));//push this 'index'
            }
            symbolTable.pointSubroutineScope();//point back to subroutine level
        }
    }

    private void advance() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
        } else {
            throw new RuntimeException("there are no more tokens");
        }
    }

    //better write method
    private void write(String str) throws IOException {

        output.write(str);
        output.newLine();

    }

    //call
    private void call(String name0,String name1,int nArgs) throws IOException {
        if (symbolTable.kindOf(name0) != Kind.NONE) {
            vmWriter.writeCall(symbolTable.typeOf(name0) + "." + name1, nArgs + 1);//call subroutine level variable.method
        } else {
            symbolTable.pointClassScope();//point to class symbol table
            if (symbolTable.kindOf(name0) != Kind.NONE) {
                vmWriter.writeCall(symbolTable.typeOf(name0) + "." + name1, nArgs + 1);//call class level variable.method
            } else {
                vmWriter.writeCall(name0 + "." + name1, nArgs);
            }
            symbolTable.pointSubroutineScope();
        }
    }

    //1:28
    private void accessArray() throws IOException {
        /**handle arr**/
        vmWriter.writePop(VMGrammar.TEMP, 0);//push address (+ offset) to stack
        vmWriter.writePop(VMGrammar.POINTER,1);//pop addrress to pointer 1 (that)
        vmWriter.writePush(VMGrammar.TEMP,0);//push value to stack
        vmWriter.writePop(VMGrammar.THAT, 0);//pop value to *pointer (to the desired address)
    }

    public static String QuoteWrap(String str) {
        return "\"" + str + "\"";
    }

    public static String keyWordTag(String str) { return tag(str, "keyword");
    }

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