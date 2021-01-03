package sample;

/**
 * Type
 *
 * Description: Intended to represent records of the types table
 *              Transactions all have a Type
 */
public class Type {

    private int id;
    private String description;

    public Type(String description) {
        this.description = description;
    }

    public Type(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
