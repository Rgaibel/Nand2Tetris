import java.io.*;

public class CodeWriter {
    private String file;
    private BufferedWriter output;
    private int labelIndex;

    public CodeWriter(String input) throws IOException {
        labelIndex = 0;

        //change the ending of the file to an assembly file
        File outName = new File(input.replace(".vm", ".asm"));
        String tempFile = outName.getName();
        file = tempFile.substring(0, tempFile.lastIndexOf("."));
        this.output = new BufferedWriter(new FileWriter(outName));
        {
        }
    }

    public void writeArithmetic(String operation) throws IOException {
        output.newLine();
        switch (operation) {

            case "add":
                binOp1("+");
                break;

            case "sub":
                binOp1("-");
                break;

            case "neg":
                negOp();
                break;

            case "eq":
                binOp1("-");
                binOp2("=");
                break;

            case "gt":
                binOp1("-");
                binOp2(">");
                break;

            case "lt":
                binOp1("-");
                binOp2("<");
                break;

            case "and":
                binOp1("&");
                break;

            case "or":
                binOp1("|");
                break;

            case "not":
                notOp();
                break;

            default:
                throw new RuntimeException("not arithmetic");
        }
    }

    public void writePushPop(Parser.commandType commandType, String type, int val) throws IOException {

        output.newLine();

        if (commandType == Parser.commandType.C_PUSH) {
            switch (type) {
                case "constant":
                    constPush("SP", val);
                    break;

                case "local":
                    lattPush("LCL", val);
                    break;

                case "argument":
                    lattPush("ARG", val);
                    break;

                case "this":
                    lattPush("THIS", val);
                    break;

                case "that":
                    lattPush("THAT", val);
                    break;

                case "temp":
                    tempPush(val);
                    break;

                case "pointer":
                    pointerPush(val);
                    break;

                case "static":
                    staticPush(val);
                    break;
            }
        } else if (commandType == Parser.commandType.C_POP) {
            switch (type) {

                case "local":
                    lattPop("LCL", val);
                    break;

                case "argument":
                    lattPop("ARG", val);
                    break;

                case "this":
                    lattPop("THIS", val);
                    break;

                case "that":
                    lattPop("THAT", val);
                    break;

                case "temp":
                    tempPop(val);
                    break;

                case "pointer":
                    pointerPop(val);
                    break;

                case "static":
                    staticPop(val);
                    break;
            }
        }
    }

    private void binOp1(String operation) throws IOException {

        //SP--
        decSP();

        //D = *SP
        setDstarP("SP");

        //SP--
        decSP();

        //*SP = *SP + D
        output.write("A=M");
        output.newLine();
        output.write("M=M" + operation + "D");// ex: M + D, M - D
        output.newLine();

        //SP++
        incSP();

    }

    private void binOp2(String arithmetic) throws IOException {

        //true = 0, false = -1
        int r = 0;
        int o = 0;
        int i = 0;

        switch (arithmetic) {

            case "=":
                r = -1;
                break;

            case ">":
                o = -1;
                break;

            case "<":
                i = -1;
                break;
        }

        String label1 = "EQUAL" + labelIndex++;
        String label2 = "GREATER" + labelIndex++;
        String label3 = "LESS" + labelIndex++;
        String labelEnd = "END" + labelIndex++;

        //SP--
        decSP();

        //D = *SP
        setDstarP("SP");

        output.write("@" + label1);
        output.newLine();
        output.write("D;JEQ");
        output.newLine();
        output.write("@" + label2);
        output.newLine();
        output.write("D;JGT");
        output.newLine();
        output.write("@" + label3);
        output.newLine();
        output.write("D;JLT");
        output.newLine();

        output.write("(" + label1 + ")");
        output.newLine();
        setAvalP("SP");
        output.write("M=" + r);
        output.newLine();
        JMP(labelEnd);

        output.write("(" + label2 + ")");
        output.newLine();
        setAvalP("SP");
        output.write("M=" + o);
        output.newLine();
        JMP(labelEnd);

        output.write("(" + label3 + ")");
        output.newLine();
        setAvalP("SP");
        output.write("M=" + i);
        output.newLine();
        JMP(labelEnd);

        output.write("(" + labelEnd + ")");
        output.newLine();
        //SP++
        incSP();
    }

    private void notOp() throws IOException {
        //SP--
        decSP();

        //*SP
        setAvalP("SP");

        //*SP = ! *SP
        output.write("M=!M");
        output.newLine();

        //SP++
        incSP();

    }

    private void negOp() throws IOException {

        notOp();

        //SP--
        decSP();

        //*SP
        setAvalP("SP");

        //*SP = *SP + 1
        output.write("M=M+1");
        output.newLine();

        //SP++
        incSP();
    }

    private void JMP(String label) throws IOException {

        output.write("@" + label);
        output.newLine();
        output.write("0;JMP");
        output.newLine();
    }

    //*pointer
    private void setAvalP(String p) throws IOException {

        output.write("@" + p);// ex: p = "SP", stack[SP] = 257, @SP -> A = 0
        output.newLine();
        output.write("A=M");// A = 257
        output.newLine();
    }

    //D = *p
    private void setDstarP(String p) throws IOException {

        output.write("@" + p);// ex: stack[SP] = 257, @SP -> A = 0
        output.newLine();
        output.write("A=M"); // A = 257
        output.newLine();
        output.write("D=M"); // stack[257] = 5 -> D = 5
        output.newLine();
    }

    //*p = D
    private void starPeqD(String p) throws IOException {

        output.write("@" + p);
        output.newLine();
        output.write("A=M");
        output.newLine();
        output.write("M=D");
        output.newLine();
    }

    //D = *(pointer+i)
    private void setDstarPaddI(String p, int val) throws IOException {

        output.write("@" + p);
        output.newLine();
        output.write("D=M");
        output.newLine();
        output.write("@" + val);
        output.newLine();
        output.write("A=A+D");
        output.newLine();
        output.write("D=M");
        output.newLine();

    }

    //D = x. this allows me to make my d equal an number immediately
    private void setDtoX(int x) throws IOException {

        String strX = Integer.toString(x);  //convert out string to an integer
        output.write("@" + strX);
        output.newLine();
        output.write("D=A");//switched completely, switch back if doesn't work
        output.newLine();

    }

    private void setMtoD(String x) throws IOException {

        output.write("@" + x);

        output.newLine();

        output.write("M=D");

        output.newLine();
    }

    private void setDtoM(String addr) throws IOException {

        output.write("@" + addr);

        output.newLine();

        output.write("D=M");

        output.newLine();
    }

    //SP++
    private void incSP() throws IOException {

        output.write("@SP");

        output.newLine();

        output.write("M=M+1");

        output.newLine();
    }

    //SP--
    private void decSP() throws IOException {

        output.write("@SP"); //SP = ex: 258
        output.newLine();
        output.write("M=M-1"); //SP = 258 - 1 = 257
        output.newLine();
    }

    //*p = val
    private void constPush(String p, int val) throws IOException {

        //D = val
        setDtoX(val);
        //*p = D
        starPeqD(p);
        //SP++
        incSP();
    }

    //push lcl, arg, this, that
    private void lattPush(String p, int val) throws IOException {

        setDstarPaddI(p, val);
        //*SP = D
        starPeqD("SP");
        //SP++
        incSP();
    }

    //push temp
    private void tempPush(int val) throws IOException {

        //D = M[R(5+val)]
        setDtoM("R" + (val + 5));
        //*SP = D
        starPeqD("SP");
        //SP++
        incSP();
    }

    //push pointer
    private void pointerPush(int val) throws IOException {

        //D = THIS/THAT
        if (val == 0) {
            setDtoM("THIS");
        } else {
            setDtoM("THAT");
        }
        //*SP = D
        starPeqD("SP");
        //SP++
        incSP();
    }

    //push static
    private void staticPush(int val) throws IOException {

        //foo.i
        setDtoM(file + "." + val);
        //*SP = D
        starPeqD("SP");
        //SP++
        incSP();
    }

    //pop temp
    private void tempPop(int val) throws IOException {

        //SP--
        decSP();
        //D = *SP
        setDstarP("SP");
        setMtoD("R" + (5 + val));
    }

    //pop pointer
    private void pointerPop(int val) throws IOException {

        //SP--
        decSP();
        //D = *SP
        setDstarP("SP");
        //THIS/THAT = D
        if (val == 0) {
            setMtoD("THIS");
        } else {
            setMtoD("THAT");
        }
    }

    private void lattPop(String p, int val) throws IOException {
        //address = pointer + i
        output.write("@" + p);

        output.newLine();
        output.write("D=M");

        output.newLine();
        output.write("@" + val);

        output.newLine();
        output.write("D=D+A");

        output.newLine();
        output.write("@addr");

        output.newLine();
        output.write("M=D");

        output.newLine();

        //SP--
        decSP();
        //*addr = *SP

        //D = *SP
        setDstarP("SP");
        //*addr = D
        starPeqD("addr");
    }

    //pop static
    private void staticPop(int val) throws IOException {
        //SP--
        decSP();
        //D = *SP
        setDstarP("SP");
        //foo.i
        setMtoD(file + "." + val);

    }


    public void close() throws IOException {
        output.close();
    }
}
