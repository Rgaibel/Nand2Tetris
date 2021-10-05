import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class Parser {

    enum commandType {

        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }

    private BufferedReader file;

    private String curr;

    private String next;

    private ArrayList<String> arithmetics = new ArrayList<String>();

    private commandType cmdType;

    private String arg1;

    private int arg2;

    //Opens the input file/stream and gets ready to parse it
    public Parser(String source) throws IOException {

        arithmetics.add("add");
        arithmetics.add("sub");
        arithmetics.add("neg");
        arithmetics.add("eq");
        arithmetics.add("gt");
        arithmetics.add("lt");
        arithmetics.add("and");
        arithmetics.add("or");
        arithmetics.add("not");

        this.file = new BufferedReader(new FileReader(source));

        this.curr = null;

        this.next = this.getNextLine();
    }

    private String getNextLine() throws IOException {

        String output = this.file.readLine();

        if (output == null) {//if program contains one line of comment/whitespace

            return null;
        }

        while (output.trim().isEmpty() || output.trim().substring(0, 2).equals("//")) {//checks for empty lines and comments

            output = this.file.readLine();

            if (output == null) {//readLine() returns null instead of empty line if the empty line is at the end of a file.

                return null;
            }
        }

        int commentIndex = output.indexOf("//");

        if (commentIndex != -1) {//if comment exists on same line, read line up to comment

            output = output.substring(0, commentIndex - 1);
        }

        return output;
    }

    // close file
    public void close() throws IOException {

        this.file.close();

    }

    //Are there more lines in the input?
    public boolean hasMoreLines() {

        return (this.next != null);
    }

    //Reads the next instruction from the input, and makes it the current instruction.
    //called only if hasMoreLines is true.
    public void advance() throws IOException {

        this.curr = this.next;

        this.next = this.getNextLine();

        arg1 = "";

        arg2 = -1;

        String[] words = this.curr.split(" ");//the words that make up the command split up into separate indices

        if (arithmetics.contains(words[0])) {

            cmdType = commandType.C_ARITHMETIC;

            arg1 = words[0];
        } else if (words[0].equals("push")) {

            cmdType = commandType.C_PUSH;
        } else if (words[0].equals("pop")) {

            cmdType = commandType.C_POP;
        } else if (words[0].equals("label")) {

            cmdType = commandType.C_LABEL;
        } else if (words[0].equals("if")) {

            cmdType = commandType.C_IF;
        } else if (words[0].equals("goto")) {

            cmdType = commandType.C_GOTO;
        } else if (words[0].equals("function")) {

            cmdType = commandType.C_FUNCTION;
        } else if (words[0].equals("call")) {

            cmdType = commandType.C_CALL;
        }

        if (cmdType == commandType.C_PUSH || cmdType == commandType.C_POP || cmdType == commandType.C_FUNCTION ||
                cmdType == commandType.C_CALL) {

            arg2 = Integer.parseInt(words[2]);
        }
    }

    public String arg1() throws IOException{

        if(cmdType != commandType.C_RETURN) {
            if(cmdType == commandType.C_ARITHMETIC){
                return curr.trim(); //return the string with no white spaces on either side (ex: add)
            }
            else{
                return curr.split(" ")[1];//split the current line into an array of strings, and we return the middle term
            }
        }
        throw new IllegalAccessError("no arg1");
    }

    public int arg2() throws IOException {

        if (cmdType == commandType.C_PUSH || cmdType == commandType.C_POP || cmdType == commandType.C_FUNCTION ||
                cmdType == commandType.C_CALL) {

            return arg2;
        }
        throw new IllegalAccessError("no arg2");
    }

    public commandType commandType() {
        if (arithmetics.contains(curr.trim())) {
            return commandType.C_ARITHMETIC;
        } else if (cmdType == commandType.C_PUSH) {
            return commandType.C_PUSH;
        } else if (cmdType== commandType.C_POP) {
            return commandType.C_POP;
        }
        return null;
    }
}
