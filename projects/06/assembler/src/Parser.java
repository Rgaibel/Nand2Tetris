import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private BufferedReader file;  //to hold my file
    private String current;
    private String next;

    public Parser(File source) throws IOException {
        this.file = new BufferedReader(new FileReader(source)); //Buffered is a constructor that imports the source file. note that FileReader makes the code more efficient
        this.current = null;
        this.next = this.getNextLine(); // this methods reads the first line of the code
    }

    // A pointer that check whether the next line is null
    public boolean hasMoreLines() {
        return (this.next != null);
    }

    // this method will skip over any white spaces, or comments.
    // additionally it will remove any comments that sit on the end of the string
    private String getNextLine() throws IOException {
        String output=file.readLine();
        if (output == null) {//stopping condition
            return null;
        }
        while (output.trim().isEmpty() || output.trim().substring(0,2).equals("//")) { //trim removes all the empty space before and after a string
            output = this.file.readLine();
            if (output == null) {
                return null;
            }
        }
        int commentIndex = output.indexOf("//"); //to catch if there are any comments in the middle of a string (like this exactly)
        if (commentIndex != -1) {
            output = output.substring(0, commentIndex - 1);
        }
        return output;
    }

    // It's necessary to close the file after reading through it.
    public void close()throws IOException {
            this.file.close();
    }


    // Advance is a public method that moves to the next line. make sure to check i
    public void advance() throws IOException {
        this.current = this.next;
        this.next = this.getNextLine();
    }

    // Returns the type of the current command:
    // - A_COMMAND anything that starts with @
    // - C_COMMAND Anything from a jump to a D=M
    // - L_COMMAND any thing that starts a (. this will include (LOOP) or (END)
    public InstructionType instructionType() {
        String line = this.current.trim();

        if (line.substring(0,1).equals("(") && line.endsWith(")"))
            return InstructionType.L_COMMAND;

        if (line.substring(0,1).equals("@"))
            return InstructionType.A_COMMAND;

        return InstructionType.C_COMMAND;
    }

    // Returns the symbol: @symbol or (symbol).
    // Should be called only for instructionType() is A_COMMAND or L_COMMAND.
    public String symbol() {
        String line = this.current.trim();

        if (this.instructionType().equals(InstructionType.L_COMMAND))
            return line.substring(1, line.length() - 1); //remove the parenthesis

        if (this.instructionType().equals(InstructionType.A_COMMAND))
            return line.substring(1);//remove the @

        return null;
    }

    //taking the first variable before the equal sign. This can be eight different options as seen in the dest chart
    // check only for C_COMMAND.
    public String dest() {
        String line = this.current.trim();
        int index = line.indexOf("=");

        if (index == -1) {
            return null;
        } else {
            return line.substring(0, index);
        }
    }

    //taking the middle variables between the equal sign and possible collin. This can be twenty-eight different options as seen in the comp chart
    // check only for C_COMMAND.
    public String comp() {
        String line = this.current.trim();
        int equalsIndex = line.indexOf("="); //for a situation like D=D+M
        if (equalsIndex != -1) {
            line = line.substring(equalsIndex + 1);
        }
        int jumpIndex = line.indexOf(";");
        if (jumpIndex != -1)
            return line.substring(0, jumpIndex);
        return line;
    }

    //taking the last variable after the collin. This can be eight different options as seen in the jump chart
    // check only for C_COMMAND.
    public String jump() {
        String line = this.current.trim();
        int colIndex = line.indexOf(";");
        if (colIndex != -1)
            return line.substring(colIndex + 1);
        return null;
    }
}
