package auxPack;

//for getting attributes from contents stored in class and subroutine level symbol tables
public class Symbol {
    private String name;
    private String type;
    private Kind kind;
    private int index;

    public Symbol(String name, String type, Kind kind, int index) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

}