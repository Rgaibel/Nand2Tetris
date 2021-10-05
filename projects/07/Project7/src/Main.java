import java.io.IOException;

public class Main {
    public static void main(String[] args)  throws IOException {
        Parser prsr = new Parser(args[0]);
        CodeWriter cdw = new CodeWriter(args[0]);
        while (prsr.hasMoreLines()) {
            prsr.advance();

            switch (prsr.commandType()) {

                case C_ARITHMETIC:
                    cdw.writeArithmetic(prsr.arg1());
                    break;

                case C_PUSH:
                    cdw.writePushPop(Parser.commandType.C_PUSH, prsr.arg1(), prsr.arg2());
                    break;

                case C_POP:
                    cdw.writePushPop(Parser.commandType.C_POP, prsr.arg1(), prsr.arg2());
                    break;

                default:
                    return;
            }
        }
        cdw.close();
    }
}

