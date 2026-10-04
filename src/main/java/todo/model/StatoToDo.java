package todo.model;

/**
 * Stato di completamento di un ToDo.
 */
public enum StatoToDo {
    Completato,
    NonCompletato;

    @Override
    public String toString() {
        switch (this) {
            case Completato:
                return "Completato";

            case NonCompletato:
                return "Non Completato";

            default:
                throw new IllegalStateException();
        }
    }

    public static StatoToDo fromString(String s) {
        switch (s.trim()) {
            case "Completato":
                return Completato;

            case "Non Completato":
                return NonCompletato;

            default:
                throw new IllegalArgumentException("Valore non valido: " + s);
        }
    }
}