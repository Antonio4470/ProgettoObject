package todo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Checklist opzionale associata a un {@link ToDo}.
 * Contiene una lista di {@link Attivita}; quando tutte le attività
 * sono completate, il ToDo associato viene automaticamente completato.
 */
public class Checklist {

    private int          id;
    private List<Attivita> attivita = new ArrayList<>();

    // ── Costruttori ──────────────────────────────────────────

    public Checklist() {}

    // ── Business logic ───────────────────────────────────────

    /**
     * Aggiunge una nuova attività alla checklist.
     *
     * @param a l'attività da aggiungere
     */
    public void aggiungiAttivita(Attivita a) {
        attivita.add(a);
    }

    /**
     * Rimuove un'attività dalla checklist.
     *
     * @param a l'attività da rimuovere
     */
    public void rimuoviAttivita(Attivita a) {
        attivita.remove(a);
    }

    /**
     * Verifica se tutte le attività della checklist sono completate.
     *
     * @return {@code true} se la lista è non vuota e ogni attività è completata
     */
    public boolean tutteCompletate() {
        if (attivita.isEmpty()) return false;
        return attivita.stream().allMatch(Attivita::isCompletata);
    }

    /** @return il numero totale di attività */
    public int totale()     { return attivita.size(); }

    /** @return il numero di attività completate */
    public int completate() {
        return (int) attivita.stream().filter(Attivita::isCompletata).count();
    }

    // ── Getter / Setter ──────────────────────────────────────

    /** @return l'id della checklist */
    public int getId()                          { return id; }

    /** @param id l'id da impostare */
    public void setId(int id)                   { this.id = id; }

    /** @return la lista delle attività */
    public List<Attivita> getAttivita()         { return attivita; }

    /** @param attivita la lista da impostare */
    public void setAttivita(List<Attivita> attivita) { this.attivita = attivita; }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) 
        	return true;
        if (!(o instanceof Checklist)) 
        	return false;
        Checklist c = (Checklist)o;
        return id == c.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Checklist{id=" + id + ", " + completate() + "/" + totale() + "}";
    }
}
