package todo.model;

/**
 * Enumerazione dei possibili titoli per una {@link Bacheca}.
 * Ogni utente possiede esattamente una bacheca per ogni valore di questo enum.
 */
public enum TitoloBacheca {
    Universita,
    Lavoro,
    TempoLibero;

    /** Restituisce la label leggibile (usata anche nel DB). */
    @Override
    public String toString() {
        switch (this) {
        case Universita:
            return "Universita";

        case Lavoro:
            return "Lavoro";

        case TempoLibero:
            return "Tempo Libero";
            
        default:
            throw new IllegalStateException();
        }
    }

    /** Parsing da stringa (tollerante alle varianti). */
    public static TitoloBacheca fromString(String s) {
        switch (s.trim()) {
        case "Universita":
            return Universita;

        case "Lavoro":
            return Lavoro;

        case "TempoLibero":
            return TempoLibero;
            
        default:
            throw new IllegalArgumentException("Valore non valido: " + s);
        }    	
    }
}
