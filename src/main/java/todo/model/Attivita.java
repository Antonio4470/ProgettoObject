package todo.model;

import java.util.Objects;

/**
 * Singola attività all'interno di una {@link Checklist}.
 * Può essere marcata come completata o non completata.
 */
public class Attivita {

    private int           id;
    private String        nome;
    private StatoAttivita stato;

    // ── Costruttori ──────────────────────────────────────────

    public Attivita() {
        this.stato = StatoAttivita.NonCompletato;
    }

    /**
     * @param nome  nome dell'attività
     */
    public Attivita(String nome) {
        this.nome  = nome;
        this.stato = StatoAttivita.NonCompletato;
    }

    // ── Business logic ───────────────────────────────────────

    /** @return {@code true} se l'attività è completata */
    public boolean isCompletata() {
        return stato == StatoAttivita.Completato;
    }

    /** Segna l'attività come completata. */
    public void completa() { this.stato = StatoAttivita.Completato; }

    /** Riporta l'attività a non completata. */
    public void riapri()   { this.stato = StatoAttivita.NonCompletato; }

    // ── Getter / Setter ──────────────────────────────────────

    /** @return l'id dell'attività */
    public int getId()                              { return id; }

    /** @param id l'id da impostare */
    public void setId(int id)                       { this.id = id; }

    /** @return il nome dell'attività */
    public String getNome()                         { return nome; }

    /** @param nome il nome da impostare */
    public void setNome(String nome)                { this.nome = nome; }

    /** @return lo stato dell'attività */
    public StatoAttivita getStato()                 { return stato; }

    /** @param stato lo stato da impostare */
    public void setStato(StatoAttivita stato)       { this.stato = stato; }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) 
        	return true;
        if (!(o instanceof Attivita)) 
        	return false;
        Attivita a = (Attivita)o;
        return id == a.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Attivita{id=" + id + ", nome='" + nome + "', stato=" + stato + "}";
    }
}
