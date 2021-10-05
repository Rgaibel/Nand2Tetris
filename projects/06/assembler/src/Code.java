import java.util.Hashtable;

public class Code {
    private Hashtable<String, String> destHash;
    private Hashtable<String, String> compHash;
    private Hashtable<String, String> jumpHash;

    public Code() {
        this.jumpHash = new Hashtable<String, String>();
        this.jumpMatch();
        this.compHash = new Hashtable<String, String>();
        this.compMatch();
        this.destHash = new Hashtable<String, String>();
        this.destMatch();
    }

    //fill all my HashTables using the put method.
    private void destMatch() {
        this.destHash.put("NULL", "000");
        this.destHash.put("M", "001");
        this.destHash.put("D", "010");
        this.destHash.put("MD", "011");
        this.destHash.put("A", "100");
        this.destHash.put("AM", "101");
        this.destHash.put("AD", "110");
        this.destHash.put("AMD", "111");
    }

    private void compMatch() {
        this.compHash.put("0", "0101010");
        this.compHash.put("1", "0111111");
        this.compHash.put("-1", "0111010");
        this.compHash.put("D", "0001100");
        this.compHash.put("A", "0110000");
        this.compHash.put("M", "1110000");
        this.compHash.put("!D", "0001101");
        this.compHash.put("!A", "0110001");
        this.compHash.put("!M", "1110001");
        this.compHash.put("-D", "0001111");
        this.compHash.put("-A", "0110011");
        this.compHash.put("-M", "1110011");
        this.compHash.put("D+1", "0011111");
        this.compHash.put("A+1", "0110111");
        this.compHash.put("M+1", "1110111");
        this.compHash.put("D-1", "0001110");
        this.compHash.put("A-1", "0110010");
        this.compHash.put("M-1", "1110010");
        this.compHash.put("D+A", "0000010");
        this.compHash.put("D+M", "1000010");
        this.compHash.put("D-A", "0010011");
        this.compHash.put("D-M", "1010011");
        this.compHash.put("A-D", "0000111");
        this.compHash.put("M-D", "1000111");
        this.compHash.put("D&A", "0000000");
        this.compHash.put("D&M", "1000000");
        this.compHash.put("D|A", "0010101");
        this.compHash.put("D|M", "1010101");
    }


    private void jumpMatch() {
        this.jumpHash.put("NULL", "000");
        this.jumpHash.put("JGT", "001");
        this.jumpHash.put("JEQ", "010");
        this.jumpHash.put("JGE", "011");
        this.jumpHash.put("JLT", "100");
        this.jumpHash.put("JNE", "101");
        this.jumpHash.put("JLE", "110");
        this.jumpHash.put("JMP", "111");
    }

    //  Returns the binary code of the destHash variable.
    public String dest(String variable) {
        if (variable == null || variable.isEmpty()) {
            variable = "NULL";
        }
        return this.destHash.get(variable);
    }

    //  Returns the binary code of the comp variable.
    public String comp(String variable) {
        return this.compHash.get(variable);
    }

    //  Returns the binary code of the jump variable.
    public String jump(String variable) {
        if (variable == null || variable.isEmpty()) {
            variable = "NULL";
        }
        return this.jumpHash.get(variable);
    }
}