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
    //declare variables needed to parse .vm file
    private BufferedReader file;
    private String fileName;
    private String curr;
    private String next;
    private ArrayList<String> arithmetics = new ArrayList<String>();
    private commandType cmdType;
    //returns first argument of current command. In case of C_ARITHMETIC, the whole line (trimmed)
    //(add, sub, etc.). However, Shouldn't be called if the current command is C_RETURN
    private String arg1;
    //returns second argument of current command. Should be called only if the current command is
    //C_PUSH, C_POP, C_FUNCTION, C_CALL
    private int arg2;

    //Opens the input file/stream and gets ready to parse it
    public Parser(String source) throws IOException {
        //adds arithemtic strings to arethmetics list
        arithmetics.add("add");
        arithmetics.add("sub");
        arithmetics.add("neg");
        arithmetics.add("eq");
        arithmetics.add("gt");
        arithmetics.add("lt");
        arithmetics.add("and");
        arithmetics.add("or");
        arithmetics.add("not");
        //assigns file to be BufferedReader Object reading FileReader object reading source
        this.file = new BufferedReader(new FileReader(source));
        //get name of file
        File tempFile = new File(source);
        String tempFileName = tempFile.getName();
        this.fileName = tempFileName.substring(0, tempFileName.lastIndexOf('.'));
        //current line being read
        this.curr = null;
        //next line to be read
        this.next = this.getNextLine();
    }

    private String getNextLine() throws IOException {
        //creates string 'output' of current line
        String output = this.file.readLine();
        //if program contains one line of comment/whitespace
        if (output == null) {

            return null;
        }

        while (output.trim().isEmpty() || output.trim().substring(0, 2).equals("//")) {//checks for empty lines and comments

            output = this.file.readLine();

            if (output == null) {//readLine() returns null instead of empty line if the empty line is at the end of a file.

                return null;
            }
        }

        int commentIndex0 = output.indexOf("//");
        int commentIndex1 = output.indexOf("\t");

        if (commentIndex0 != -1) {//if comment exists on same line, read line up to comment
            output = output.substring(0, commentIndex0 - 1);
        }
        if (commentIndex1 != -1) {
            output = output.substring(0, commentIndex1);
        }


        return output;
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
        //assign arg1
        arg1 = "";
        //assign arg2
        arg2 = -1;
        //the words that make up the command split up into separate indices
        String[] words = this.curr.split(" ");
        //checks arthmetics list for first index of words. if it's add or sub, etc. than we have arithmetic command
        //(and so on down the line)
        if (arithmetics.contains(words[0])) {

            cmdType = commandType.C_ARITHMETIC;

            arg1 = words[0];
        } else if (words[0].equals("push")) {

            cmdType = commandType.C_PUSH;
        } else if (words[0].equals("pop")) {

            cmdType = commandType.C_POP;
        } else if (words[0].equals("label")) {

            cmdType = commandType.C_LABEL;
        } else if (words[0].startsWith("if")) {

            cmdType = commandType.C_IF;
        } else if (words[0].equals("goto")) {

            cmdType = commandType.C_GOTO;
        } else if (words[0].equals("function")) {

            cmdType = commandType.C_FUNCTION;
        } else if (words[0].equals("call")) {

            cmdType = commandType.C_CALL;
        } else if (words[0].equals("return")){

            cmdType = commandType.C_RETURN;
        }

        if (cmdType == commandType.C_PUSH || cmdType == commandType.C_POP || cmdType == commandType.C_FUNCTION ||
                cmdType == commandType.C_CALL) {
            //if one of the cmd types above then we know arg2 is the 3rd index of words (the value being used)
            arg2 = Integer.parseInt(words[2]);
        }
    }

    public String arg1() throws IOException{

        if(cmdType != commandType.C_RETURN) {
            if(cmdType == commandType.C_ARITHMETIC){
                return curr.trim(); //return the string with no white spaces on either side (ex: add)
            }
            else{
                return curr.split(" ")[1];//split the current line into an array of strings, and we return the middle term (cause we know it's 3 terms based on conditional)
            }
        }
        throw new IllegalAccessError("no arg1");
    }
    //returns arg2 given the appropriate commandType
    public int arg2() throws IOException {

        if (cmdType == commandType.C_PUSH || cmdType == commandType.C_POP || cmdType == commandType.C_FUNCTION ||
                cmdType == commandType.C_CALL) {

            return arg2;
        }
        throw new IllegalAccessError("no arg2");
    }
    //return commandType of given line
    public commandType commandType() {
        if (arithmetics.contains(curr.trim())) {
            return commandType.C_ARITHMETIC;
        }
        else if (cmdType == commandType.C_PUSH) {
            return commandType.C_PUSH;
        }
        else if (cmdType == commandType.C_POP) {
            return commandType.C_POP;
        }
        else if (cmdType == commandType.C_LABEL) {
            return commandType.C_LABEL;
        }
        else if (cmdType == commandType.C_GOTO) {
            return commandType.C_GOTO;
        }
        else if (cmdType == commandType.C_IF) {
            return commandType.C_IF;
        }
        else if (cmdType == commandType.C_FUNCTION) {
            return commandType.C_FUNCTION;
        }
        else if (cmdType == commandType.C_CALL) {
            return commandType.C_CALL;
        }
        else if (cmdType == commandType.C_RETURN) {
            return commandType.C_RETURN;
        }
        return null;
    }

    public String getFileName() {
        return fileName;
    }
}
