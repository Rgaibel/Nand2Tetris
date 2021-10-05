import auxPack.Kind;
import auxPack.Symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private Map<String, Symbol> classTable = new HashMap<>();
    private Map<String, Symbol> subroutineTable = new HashMap<>();
    private Map<String, Symbol> currentScope;
    //keep count of variables
    private int staticCount;
    private int fieldCount;
    private int argCount;
    private int varCount;

    public SymbolTable() {
        this.staticCount = 0;
        this.fieldCount = 0;
        this.argCount = 0;
        this.varCount = 0;
    }

    public void pointClassScope() {
        currentScope = classTable;
    }

    public void pointSubroutineScope() {
        currentScope = subroutineTable;
    }

    //starts new subroutine scope
    public void startSubroutine() {
        subroutineTable.clear();//erase all names in previous subroutine's scope
        argCount = 0;//arguments fall under subroutine scope
        varCount = 0;//vars fall under subroutine scope
    }

    //Defines new identifier of a given 'name', 'type', and 'kind' and assigns it a running index (adds to symbol table)
    public void define(String name, String type, String kind) {
        switch (kind) {
            case "static"://static identifier has class scope
                classTable.put(name, new Symbol(name, type, Kind.STATIC, staticCount++));
                break;
            case "field"://field identifier has class scope
                classTable.put(name, new Symbol(name, type, Kind.FIELD, fieldCount++));
                break;
            case "arg"://arg identifier has subroutine scope
                subroutineTable.put(name, new Symbol(name, type, Kind.ARG, argCount++));
                break;
            case "var"://var identifier has subroutine scope
                subroutineTable.put(name, new Symbol(name, type, Kind.VAR, varCount++));
                break;
        }
    }

    //returns the 'kind' of the named identifier in the current scope.
    public Kind kindOf(String name) {
        //Returns 'None' if the identifier is unknown in the current scope
        if(currentScope.get(name) == null) {
            return Kind.NONE;
        }
        return currentScope.get(name).getKind();
    }

    //returns the 'type' of the named identifier in the current scope
    public String typeOf(String name) {
        return currentScope.get(name).getType();
    }

    //returns the index assigned to named identifier
    public int indexOf(String name) {
        return currentScope.get(name).getIndex();
    }

    //Returns the number of variables of the given 'kind' already defined in the current scope
    public int variableTotal(Kind kind) {
        switch (kind) {
            case STATIC:
                return staticCount;
            case FIELD:
                return fieldCount;
            case ARG:
                return argCount;
            case VAR:
                return varCount;
            default:
                return 0;
        }
    }
}