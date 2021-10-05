import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static auxPack.VMGrammar.*;

public class VMWriter {
    private BufferedWriter output;//declare output file to be written into
    private String className;

    //create new output .vm file and prepare for writing
    public VMWriter(String input) throws IOException {
        File file = new File(input);
        className = file.getName().substring(0, file.getName().indexOf("."));
        input = input.replace(".jack", ".vm");
        output = new BufferedWriter(new FileWriter(input));
    }

    //write VM push command
    public void writePush(String segment, int index) throws IOException {
        write("push " + segment + " " + index);
    }

    //write VM pop command
    public void writePop(String segment, int index) throws IOException {
        write("pop " + segment + " " + index);
    }

    //write VM arithemtic-logical command
    public void writeArithmetic(String command) throws IOException {
        switch (command) {
            case "+":
                write(ADD);
                break;
            case "-":
                write(SUB);
                break;
            case "_":
                write(NEG);
                break;
            case "=":
                write(EQ);
                break;
            case ">":
                write(GT);
                break;
            case "<":
                write(LT);
                break;
            case "&":
                write(AND);
                break;
            case "|":
                write(OR);
                break;
            case "~":
                write(NOT);
                break;
        }
    }

    //write VM label command
    public void writeLabel(String label) throws IOException {
        write("label " + label);
    }

    //write VM goto command
    public void writeGoto(String label) throws IOException {
        write("goto " + label);
    }

    //write VM if command
    public void writeIf(String label) throws IOException {
        write("if-goto " + label);
    }

    //write VM call command
    public void writeCall(String name, int nArgs) throws IOException {
        write("call " + name + " " + nArgs);
    }

    //write VM function command
    public void writeFunction(String name, int nArgs) throws IOException {
        write("function " + className + "." + name + " " + nArgs);//
    }

    //write VM return command
    public void writeReturn() throws IOException {
        write("return");
    }

    //better write method
    private void write(String str) throws IOException {
            output.write(str);
            output.newLine();
        }

    //closes the output file
    public void close() throws IOException {
            output.close();
    }
}