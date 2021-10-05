import java.util.Hashtable;

public class SymbolTable {
    private Hashtable<String, Integer> symbolHash;
    private int programAddress;
    private int dataAddress;

    public SymbolTable() {
        this.initializeHashTable();
        this.programAddress =0;
        this.dataAddress = 16;
    }

    private void initializeHashTable() {
        this.symbolHash = new Hashtable<String, Integer>();
        this.symbolHash.put("SP", Integer.valueOf(0)); //note that these first five are part of the Hack Language specification code
        this.symbolHash.put("LCL", Integer.valueOf(1));
        this.symbolHash.put("ARG", Integer.valueOf(2));
        this.symbolHash.put("THIS", Integer.valueOf(3));
        this.symbolHash.put("THAT", Integer.valueOf(4));
        this.symbolHash.put("R0", Integer.valueOf(0));
        this.symbolHash.put("R1", Integer.valueOf(1));
        this.symbolHash.put("R2", Integer.valueOf(2));
        this.symbolHash.put("R3", Integer.valueOf(3));
        this.symbolHash.put("R4", Integer.valueOf(4));
        this.symbolHash.put("R5", Integer.valueOf(5));
        this.symbolHash.put("R6", Integer.valueOf(6));
        this.symbolHash.put("R7", Integer.valueOf(7));
        this.symbolHash.put("R8", Integer.valueOf(8));
        this.symbolHash.put("R9", Integer.valueOf(9));
        this.symbolHash.put("R10", Integer.valueOf(10));
        this.symbolHash.put("R11", Integer.valueOf(11));
        this.symbolHash.put("R12", Integer.valueOf(12));
        this.symbolHash.put("R13", Integer.valueOf(13));
        this.symbolHash.put("R14", Integer.valueOf(14));
        this.symbolHash.put("R15", Integer.valueOf(15));
        this.symbolHash.put("SCREEN", Integer.valueOf(16384));
        this.symbolHash.put("KBD", Integer.valueOf(24576));
    }

    // Adds the pair (symbol, address) to the table.
    public void addEntry(String symbol, int address) {
        this.symbolHash.put(symbol, Integer.valueOf(address));
    }

    // Does the symbol table contain the given symbol?
    public boolean contains(String symbol) {
        return this.symbolHash.containsKey(symbol);
    }

    //  Returns the address associated with the symbol.
    public int getAddress(String symbol) {
        return this.symbolHash.get(symbol);
    }
    //this is very important so that you always have an open spot to fill a variable. the open slots are between 0 and 15
    public void incrementProgramAddress() {
        this.programAddress++;
    }
    //likewise. the open slots are between 16 and keyboard
    public void incrementDataAddress() {
        this.dataAddress++;
    }

    public int getProgramAddress() {
        return this.programAddress;
    }

    public int getDataAddress() {
        return this.dataAddress;
    }
}