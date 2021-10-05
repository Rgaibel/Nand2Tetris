import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzer {
    static List<JackTokenizer> jackTokenizerList = new ArrayList<>();

    public static void main(String[] args) throws IOException{
        traverseDir(args[0]);
        for (JackTokenizer jackTokenizer : jackTokenizerList) {
            jackTokenizer.setPointer();
            new CompilationEngine(jackTokenizer);
        }
    }

    public static void traverseDir(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            //if single file
            if (file.getName().endsWith(".jack")) {
                JackTokenizer jackTokenizer = new JackTokenizer(path);
                jackTokenizerList.add(jackTokenizer);
                return;
            }
            //if directory
            File[] files = file.listFiles();
            if (files.length == 0) {
                System.err.println("folder is empty");
                return;
            } else {
                for (File item : files) {
                    String filePath = item.getAbsolutePath();
                    if (filePath.endsWith(".jack")) {
                        JackTokenizer jackTokenizer = new JackTokenizer(filePath);
                        jackTokenizerList.add(jackTokenizer);
                    }
                }
            }
        } else {
            System.err.println("jack file doesn't exist");
        }
    }
}
