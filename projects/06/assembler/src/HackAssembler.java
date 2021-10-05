import java.io.*;

public class HackAssembler {


    public static void main(String[] args) throws IOException {
        String outputPath = args[0];
        File file = new File(outputPath);
        outputPath = outputPath.replace(".asm", ".hack");
        BufferedWriter output = new BufferedWriter(new FileWriter(outputPath));
        translate(file,output);
    }


    // Translate assembly file to machine language.
    public static void translate(File file, BufferedWriter output ) throws IOException {
        Code code = new Code();
        SymbolTable symbolTable = new SymbolTable();
        FirstPass(symbolTable, file);
        SecondPass(symbolTable, code,file, output);
    }

    private static void FirstPass(SymbolTable symbolTable, File file) throws IOException {
        Parser parser = new Parser(file);
        while (parser.hasMoreLines()) {
            parser.advance();
            InstructionType instructionType = parser.instructionType();

            if (instructionType.equals(InstructionType.L_COMMAND)) {
                String symbol = parser.symbol();
                int address = symbolTable.getProgramAddress();
                symbolTable.addEntry(symbol, address);
            } else {
                symbolTable.incrementProgramAddress();
            }
        }
        parser.close();
    }

    private static void SecondPass(SymbolTable symbolTable, Code code, File file, BufferedWriter output) throws IOException {
        Parser parser = new Parser(file);
        while (parser.hasMoreLines()) {
            parser.advance();

            InstructionType InstructionType = parser.instructionType();
            String instruction = null;

            if (InstructionType.equals(InstructionType.A_COMMAND)) {
                //I want to find what comes after the @
                String symbol = parser.symbol();

                String address;
                //If it's a Character, for example @i or @sum, I want to return an address at the place I am storing the value
                if (!Character.isDigit(symbol.charAt(0))) {
                    //note it's possible my character has't been initialized yet, at which point I create a space for it and Increment the free address
                    if (!symbolTable.contains(symbol)) {
                        int dataAddress = symbolTable.getDataAddress();
                        symbolTable.addEntry(symbol, dataAddress);
                        symbolTable.incrementDataAddress();
                    }

                    // It exists already, and I just want to access it
                    address = Integer.toString(symbolTable.getAddress(symbol));
                } else {
                    //if it's a number I want to simply return that number. for example @5=0000000000000101
                    address = symbol;
                }
                instruction = buildA(address);

            } else if (InstructionType.equals(InstructionType.C_COMMAND)) {
                String comp = parser.comp();
                String dest = parser.dest();
                String jump = parser.jump();
                instruction = buildC(comp, dest, jump, code);
            }

            if (!InstructionType.equals(InstructionType.L_COMMAND)) {
                // Write binary instruction to file.
                output.write(instruction);
                output.newLine();
            }
        }

        // Release resources.
        parser.close();
        output.flush();
        output.close();
    }

    // Machine-format an A-Instruction.
    private static String buildA(String address) {
        int val = Integer.parseInt(address);
        String binVal = Integer.toBinaryString(val);
        int padVal = 16 - binVal.length();
        String zero = "0";
        int count = 1;
        while (count < padVal) {
            zero = zero + "0";
            count++;
        }
        binVal = zero + binVal;
        return binVal;
    }

    // Machine-format a C-Instruction.
    private static String buildC(String comp, String dest, String jump, Code code) {
        StringWriter binVal = new StringWriter();
        binVal.append("111");
        binVal.append(code.comp(comp));
        binVal.append(code.dest(dest));
        binVal.append(code.jump(jump));
        return binVal.toString();
    }
}
