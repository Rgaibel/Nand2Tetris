import java.io.*;

public class CodeWriter {
    private String file;
    private BufferedWriter output;
    private int labelIndex;
    private int returnIndex;
    private String function;
    private File outName;

    public CodeWriter(String input) throws IOException {
        this.labelIndex = 0;
        this.returnIndex = 0;
        this.function = "";//added


        //change the ending of the file to an assembly file
        if(input.endsWith(".vm")) {
            outName = new File(input.replace(".vm", ".asm"));
            this.output = new BufferedWriter(new FileWriter(outName));
        }
        //create new .asm file with same name as directory, inside directory
        else{
            outName = new File(input);//added
            input = input + "\\" + outName.getName() + ".asm";//added
            this.output = new BufferedWriter(new FileWriter(input));
        }

    }
    //set file's name to it's actual name without *\ before
    public void setFileName(String fileName){
        this.file = fileName;
    }
    //writes arithmetic operation as assembly into .asm file
    public void writeArithmetic(String operation) throws IOException {
        output.newLine();
        switch (operation) {

            case "add":
                output.write("//add");
                output.newLine();
                binOp1("+");
                break;

            case "sub":
                output.write("//subtract");
                output.newLine();
                binOp1("-");
                break;

            case "neg":
                output.write("//negate");
                output.newLine();
                negOp();
                break;

            case "eq":
                output.write("//equal");
                output.newLine();
                binOp1("-");
                binOp2("=");
                break;

            case "gt":
                output.write("//greater than");
                output.newLine();
                binOp1("-");
                binOp2(">");
                break;

            case "lt":
                output.write("//less than");
                output.newLine();
                binOp1("-");
                binOp2("<");
                break;

            case "and":
                output.write("//and");
                output.newLine();
                binOp1("&");
                break;

            case "or":
                output.write("//or");
                output.newLine();
                binOp1("|");
                break;

            case "not":
                output.write("//not");
                output.newLine();
                notOp();
                break;

            default:
                throw new RuntimeException("not arithmetic");
        }
    }
    //writes push/pop operation into .asm file
    public void writePushPop(Parser.commandType commandType, String type, int val) throws IOException {

        output.newLine();

        if (commandType == Parser.commandType.C_PUSH) {

            output.write("//push " + type + " " + val);
            output.newLine();
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
            output.write("//pop " + type + " " + val);
            output.newLine();
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
    //writeInit convention
    public void writeInit() throws IOException{

        output.write("//init");
        output.newLine();
        //initialize stack pointer to 0x0100
        setDtoX(256);
        output.write("@SP");
        output.newLine();
        output.write("M=D");
        output.newLine();
        //call Sys.init
        writeCall("Sys.init", 0);
    }
    //labels current location in the fucntion's code to be potentially jumped to from other parts of program
    public void writeLabel(String label) throws IOException {

        output.newLine();
        output.write("//label " + label);
        output.newLine();
        output.write("(" + this.function + ":" + label + ")");//change this later maybe?
        output.newLine();
    }

    //goto operation causes execution to continue from the location marked by label
    public void writeGoto(String label) throws IOException {

        output.newLine();
        output.write("//goto " + label);
        output.newLine();
        JMP(this.function + ":" + label);
    }

    //conditional goto operation. The stack's topmost value is popped; if zero, execution continues from
    //location marked by label, otherwise continues from next command in program
    public void writeIf(String label) throws IOException {

        output.newLine();
        output.write("//if-goto " + label);
        output.newLine();
        decSP();
        setDstarP("SP");
        output.write("@" + this.function + ":" + label);
        output.newLine();
        output.write("D;JNE");
        output.newLine();
    }
    //label function and initialize n (numVars) local variables of the called function to zero
    public void writeFunction(String functionName, int numVars) throws IOException {

        output.newLine();
        output.write("//function " + functionName + " " + numVars);
        output.newLine();
        this.function = functionName;
        //declare label for function entry
        output.write("(" + functionName + ")");
        output.newLine();
        output.write("D=0");
        output.newLine();
        //initialized numVars local variables of function to zero
        for(int i = 0; i < numVars; i++){
            starPeqD("SP");
            incSP();
        }
    }

    //write @addressOfPointer
    private void addrPush(String p) throws IOException {

        output.write("@" + p);
        output.newLine();
        output.write("D=M");
        output.newLine();
        starPeqD("SP");
        incSP();
    }
    //call function 'functionName' stating that numArgs arguments have already been pushed onto the stack
    public void writeCall(String functionName, int numArgs) throws IOException{

        output.newLine();
        output.write("//call " + functionName + " " + numArgs);
        output.newLine();
        //push return-address
        output.write("@return_address_" + returnIndex);
        output.newLine();
        output.write("D=A");
        output.newLine();
        starPeqD("SP");
        incSP();
        //push LCL
        addrPush("LCL");
        //push ARG
        addrPush("ARG");
        //push THIS
        addrPush("THIS");
        //push THAT
        addrPush("THAT");
        setDtoM("SP");
        //reposition ARG (numArgs=number of args)
        output.write("@" + (numArgs + 5));
        output.newLine();
        output.write("D=D-A");
        output.newLine();
        setMtoD("ARG");
        setDtoM("SP");
        //reposition LCL
        setMtoD("LCL");
        //transfer control to function
        JMP(functionName);
        //label for the return address
        output.write("(" + "return_address_" + returnIndex + ")");
        output.newLine();
        returnIndex++;
    }
    //return the calling function
    public void writeReturn() throws IOException{

        output.newLine();
        output.write("//return");
        output.newLine();
        //FRAME=LCL (FRAME is a temporary variable)
        setDtoM("LCL");
        setMtoD("FRAME");
        //RET=*(FRAME-5) save return address in a temp. var
        setDstarPsubI("FRAME", 5);
        setMtoD("RET");
        //SP moves to the ARG location to reclaim function space
        //*ARG=pop() reposition return value for caller
        //*ARG = *SP
        decSP();
        setDstarP("SP");
        starPeqD("ARG");
        //SP=ARG+1 restore SP for caller
        setDtoM("ARG");
        setMtoD("SP");
        incSP();
        //THAT=*(FRAME-1) restore THAT of calling function
        setDstarPsubI("FRAME", 1);
        setMtoD("THAT");
        //THIS=*(FRAME-2) restore THIS of calling function
        setDstarPsubI("FRAME", 2);
        setMtoD("THIS");
        //ARG=*(FRAME-3) restore ARG of calling function
        setDstarPsubI("FRAME", 3);
        setMtoD("ARG");
        //LCL=*(FRAME-4) restore LCL of calling function
        setDstarPsubI("FRAME", 4);
        setMtoD("LCL");
        //goto RET GOTO the reeturn address
        output.write("@RET");
        output.newLine();
        output.write("A=M");
        output.newLine();
        output.write("0;JMP");
        output.newLine();
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
        output.write("A=D+A");
        output.newLine();
        output.write("D=M");
        output.newLine();

    }
    //D = *(pointer-i)
    private void setDstarPsubI(String p, int val) throws IOException{
        output.write("@" + p);
        output.newLine();
        output.write("D=M");
        output.newLine();
        output.write("@" + val);
        output.newLine();
        output.write("A=D-A");
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
