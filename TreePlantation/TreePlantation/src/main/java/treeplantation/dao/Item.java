package treeplantation.dao;

/** An id + label pair used to fill drop-down lists. */
public record Item(int id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
