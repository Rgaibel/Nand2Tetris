import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    //stores .vm files from directory as parser objects in prsrlist during traverseDir()
    static List<Parser> prsrList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        //traverses the given directory in order to create parser objects from the .vm files within and add to prsrList
        //(just adds single .vm file if input is such)
        traverseDir(args[0]);
        //creates .asm file to be filled with .vm file(')s(') instructions into (translated into assembly)
        CodeWriter cdw = new CodeWriter(args[0]);
        //generate bootstrap code for inputs containing sys.vm file
        for(Parser prsr : prsrList) {
            if (prsr.getFileName().equals("Sys")) {
                cdw.writeInit();
                break;
            }
        }
        //goes through parsed .vm files in prsrList (after added by traverseDir)
        for (Parser prsr : prsrList) {
            cdw.setFileName(prsr.getFileName());
            //run through given parser object in prsrList
            while (prsr.hasMoreLines()) {
                //get next relevant line
                prsr.advance();
                //check which commandType the line is and write appropriate assembly code in new .asm file
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
                    case C_LABEL:
                        cdw.writeLabel(prsr.arg1());
                        break;
                    case C_GOTO:
                        cdw.writeGoto(prsr.arg1());
                        break;
                    case C_IF:
                        cdw.writeIf(prsr.arg1());
                        break;
                    case C_FUNCTION:
                        cdw.writeFunction(prsr.arg1(), prsr.arg2());
                        break;
                    case C_RETURN:
                        cdw.writeReturn();
                        break;
                    case C_CALL:
                        cdw.writeCall(prsr.arg1(), prsr.arg2());
                        break;
                }
            }
        }
        cdw.close();
    }

    public static void traverseDir(String path) throws IOException {
        File file = new File(path);
        //if we're dealing with single file
        if (file.getName().endsWith(".vm")) {
            Parser parser = new Parser(path);
            prsrList.add(parser);
            return;
        }
        //if we're dealing with directory
        File[] files = file.listFiles();
        if (files.length == 0) {
            System.out.println("directory is empty!");
            return;
        } else {
            //add every file from directory that ends with .vm into psrsList
            for (File item : files) {
                String filePath = item.getAbsolutePath();
                if (filePath.endsWith(".vm")) {
                    Parser parser = new Parser(filePath);
                    prsrList.add(parser);
                }
            }
        }
    }
}
