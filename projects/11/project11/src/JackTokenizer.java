import auxPack.TokenType;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static auxPack.XMLGrammar.*;
import static auxPack.XMLGrammar.FIELD_KEYWORD;
import static auxPack.XMLGrammar.STATIC_KEYWORD;
import static auxPack.TokenType.*;


public class JackTokenizer {

    private List<String> tokenList;//a list of accumulated tokenList
    private int pointer;
    private String token;//token that pointer is pointing at (in 'tokenList' List)
    private String fileName;
    private String filePath;


    public JackTokenizer(String filePath) throws IOException {
        setPointer();
        tokenList = new ArrayList();
        String curr;
        BufferedReader input = new BufferedReader(new FileReader(filePath));//read file denoted 'input'
        this.filePath = filePath;
        File file = new File(filePath);
        String tempName = file.getName();
        this.fileName = tempName.substring(0, tempName.lastIndexOf('.'));
        curr = input.readLine();//first line in file
        boolean passLine = false; //idicates which lines to pass over
        while (curr != null) {//while line isn't null
            curr = curr.trim();//trims white space off either side of line

            //Delete comment
            if (curr.startsWith("/*") && !curr.endsWith("*/")) {//if line begins with comment and comment doesn't end on same line
                passLine = true;//pass over line
                curr = input.readLine();//read next line
                continue;//goto next loop iteration
            } else if (curr.endsWith("*/") || curr.startsWith("*/")) {//if a comment ends on this line or line starts with comment
                passLine = false;//change passLine to false so we read the next line
                curr = input.readLine();//read next line
                continue;//go to next loop iteration
            } else if (curr.startsWith("/*") && curr.endsWith("*/")) {//if a comment starts and ends on this line
                curr = input.readLine();//skip current line and read next line
                continue;//goto next loop iteration
            }

            if (curr.equals("") || passLine || curr.startsWith("//")) {//if line is empty or passLine is true or is a 1 line comment
                curr = input.readLine();//skip current line and read next line
                continue;//goto next loop iteration
            }
            //

            String[] line = curr.split("//")[0].trim().split("\"");//creates array 'line' which is 1 empty index if line is comment otherwise returns trimmed line split at STRING_CONST token
            boolean str = true;//if declaring string in current line then we split away the quotes and need to add them later
            for (String segment : line) {//for each 'segment' in line
                if (str) {//
                    String[] words = segment.split(" ");//create words array whos indices contain tokens of line ex:(if, x, (, <) (could contain more than one token per index)
                    for (String word : words) {//go through each token and
                        List<String> wordTokens = new ArrayList<>();//first make a list to fill with tokens from words' current index
                        splitToToken(word, wordTokens);//split the value in words' current index into atomic tokens
                        tokenList.addAll(wordTokens);//dump tokens in to 'tokenList' ArrayList
                    }
                    str = false;//next token will be string so need to return quotes and add to tokenList
                } else {
                    tokenList.add(CompilationEngine.QuoteWrap(segment));//adds statement, wrapped by " on both sides, to 'tokenList' ArrayList (entire string is one token)
                    str = true;//next token will not be string (might be '+' or nothing)
                }
            }
            curr = input.readLine();//read next line
        }
        System.out.println();//add space
        input.close();//close file
    }

    //recursively split word into tokens in wordTokens
    private void splitToToken(String word, List<String> wordTokens) {
        if (word == null || word.isEmpty()) {//if word input is empty return nothing
            return;
        }
        if (word.length() == 1) {//base case: if word is of length one then it can only be 1 token
            wordTokens.add(word);//so add the word to input tokenList list
            return;
        }
        boolean containsSymbol = false;//default assumption no symbols in word in boolean form

        for (int i = 0; i < symbols.size(); i++) {//iterate through symbols in XMLGrammar symbol list
            String symbol = symbols.get(i);//gets symbol at index i of symbol list
            if (word.contains(symbol)) {//checks if the symbol is anywhere in word input
                containsSymbol = true;//if so then change boolean to true
                int symbInd = word.indexOf(symbol);//gets first index of the symbol in the word input
                splitToToken(word.substring(0, symbInd), wordTokens);//recursively continue splitToToken method on word input up to just before symbol
                wordTokens.add(symbol);//after hitting base case of recursion start adding symbols
                if (symbInd + 1 < word.length()) {
                    splitToToken(word.substring(symbInd + 1, word.length()), wordTokens);//recursively run splitToToken method on word input starting just after symbol index
                }
                break;
            }
        }
        if (!containsSymbol) {//if word doesn't contain any symbol add the word to tokenList list input
            wordTokens.add(word);
        }
    }

    public void advance() {
        pointer++;//incrememnt pointer
        this.token = tokenList.get(pointer);//get next token
    }


    public Boolean hasMoreTokens() {
        return pointer < tokenList.size() - 1;
    }//returns false if pointer is at end of 'tokenList' ArrayList, false otherwise

    //returns what type the token is
    public TokenType tokenType() {
        if (keywords.contains(token)) {
            return KEYWORD;
        } else if (symbols.contains(token)) {
            return SYMBOL;
        } else if (isNumeric(token)) {
            return INT_CONST;
        } else if (token.startsWith("\"") && token.endsWith("\"")) {
            return STRING_CONST;
        } else {
            return IDENTIFIER;
        }
    }

    //return token if keyword
    public String keyword() {
        if (tokenType() != KEYWORD) {
            throw new RuntimeException("This method should be called only if tokenType is KEYWORD.");
        }
        return getToken();
    }
    //Returns the character which is the current token. (symbol)
    public String symbol() {
        if (tokenType() != SYMBOL) {
            throw new RuntimeException("Should be called only if tokenType is SYMBOL.");
        }
        String token = this.token;
        switch (this.token) {//assign proper symbol for xml
            case ">":
                token = "&gt;";
                break;
            case "<":
                token = "&lt;";
                break;
            case "&":
                token = "&amp;";
                break;
        }
        return token;
    }
    //Returns the string which is the current token. (identifier)
    public String identifier() {
        if (tokenType() != IDENTIFIER) {
            throw new RuntimeException("Should be called only if tokenType is IDENTIFIER.");
        }
        return getToken();
    }
    //Returns the integer value of the current token.
    public int intVal() {
        if (tokenType() != INT_CONST) {
            throw new RuntimeException("Should be called only if tokenType is INT_CONST.");
        }
        return Integer.parseInt(getToken());
    }
    //Returns the string value of the current token, without the opening and closing double quotes.
    public String stringVal() {
        if (tokenType() != STRING_CONST) {
            throw new RuntimeException("Should be called only if tokenType is STRING_CONST.");
        }
        return getToken().replace("\"", "");
    }
    //intialize pointer at -1
    public void setPointer() {
        pointer = -1;
    }
    //returns token -
    public String getToken() {
        return token;
    }
    //returns filename -
    public String getFileName() {
        return fileName;
    }
    //returns file path -
    public String getFilePath() {
        return filePath;
    }

    //if token is constructor, function, or method, return true
    public boolean isSubroutKeyword() {
        if (token.equals(CONSTRUCTOR_KEYWORD) ||
                token.equals(FUNCTION_KEYWORD) ||
                token.equals(METHOD_KEYWORD)) {
            return true;
        }
        return false;
    }
    //if token is int, char, or boolean, return true
    public boolean isPrimType() {
        if (token.equals(INT_KEYWORD) ||
                token.equals(CHAR_KEYWORD) ||
                token.equals(BOOLEAN_KEYWORD)) {
            return true;
        }
        return false;
    }
    //if token type is keyword and token is let, if, while, do, or return, return true
    public boolean isStatement() {
        if (tokenType() == TokenType.KEYWORD &&
                (token.equals(LET_KEYWORD) ||
                        token.equals(IF_KEYWORD) ||
                        token.equals(WHILE_KEYWORD) ||
                        token.equals(DO_KEYWORD) ||
                        token.equals(RETURN_KEYWORD))) {
            return true;
        }
        return false;
    }
    //if token is field or static return true
    public boolean isClassVar() {
        if (token.equals(FIELD_KEYWORD) ||
                token.equals(STATIC_KEYWORD)) {
            return true;
        }
        return false;
    }

    public boolean isOp() {
        if (tokenType() == TokenType.SYMBOL) {
            switch (token) {
                case "+":
                case "-":
                case "*":
                case "/":
                case "&":
                case "|":
                case "<":
                case ">":
                case "=":
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }
    //if token is unary arithemtic operator return true
    public boolean isUnOp() {
        if (tokenType() == TokenType.SYMBOL &&
                (token.equals("-") || token.equals("~"))) {
            return true;
        }
        return false;
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

}