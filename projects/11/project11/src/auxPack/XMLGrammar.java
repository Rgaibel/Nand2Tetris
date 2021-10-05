
package auxPack;

import java.util.Arrays;
import java.util.List;

public class XMLGrammar {
    public static List<String> keywords;
    public static List<String> symbols;

    public static final String CLASS_KEYWORD = "class";
    public static final String CONSTRUCTOR_KEYWORD = "constructor";
    public static final String FUNCTION_KEYWORD = "function";
    public static final String METHOD_KEYWORD = "method";
    public static final String FIELD_KEYWORD = "field";
    public static final String STATIC_KEYWORD = "static";
    public static final String VAR_KEYWORD = "var";
    public static final String INT_KEYWORD = "int";
    public static final String CHAR_KEYWORD = "char";
    public static final String BOOLEAN_KEYWORD = "boolean";
    public static final String VOID_KEYWORD = "void";
    public static final String TRUE_KEYWORD = "true";
    public static final String FALSE_KEYWORD = "false";
    public static final String NULL_KEYWORD = "null";
    public static final String THIS_KEYWORD = "this";
    public static final String LET_KEYWORD = "let";
    public static final String DO_KEYWORD = "do";
    public static final String IF_KEYWORD = "if";
    public static final String ELSE_KEYWORD = "else";
    public static final String WHILE_KEYWORD = "while";
    public static final String RETURN_KEYWORD = "return";


    static {
        keywords = Arrays.asList(CLASS_KEYWORD,
                CONSTRUCTOR_KEYWORD,
                FUNCTION_KEYWORD,
                METHOD_KEYWORD,
                FIELD_KEYWORD,
                STATIC_KEYWORD,
                VAR_KEYWORD,
                INT_KEYWORD,
                CHAR_KEYWORD,
                BOOLEAN_KEYWORD,
                VOID_KEYWORD,
                TRUE_KEYWORD,
                FALSE_KEYWORD,
                NULL_KEYWORD,
                THIS_KEYWORD,
                LET_KEYWORD,
                DO_KEYWORD,
                IF_KEYWORD,
                ELSE_KEYWORD,
                WHILE_KEYWORD,
                RETURN_KEYWORD
        );

        symbols = Arrays.asList(
                "{", "}",
                "[", "]",
                "(", ")",
                ".",
                ",",
                ";",
                "+", "-", "*", "/",
                "&", "|", "~",
                "<", "=", ">"
        );

    }
}