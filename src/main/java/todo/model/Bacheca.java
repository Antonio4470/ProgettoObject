package todo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta una bacheca che raggruppa i {@link ToDo} di un {@link Utente}.
 * Ogni utente ha esattamente tre bacheche (una per ogni {@link TitoloBacheca}).
 * I ToDo al suo interno sono mantenuti ordinati per il campo {@code posizione}.
 */
public class Bacheca {

    private int            id;
    private TitoloBacheca  titolo;
    private String         descrizione;
    private Utente         utente;

    /**
     * Lista ordinata dei ToDo contenuti nella bacheca.
     * L'ordine riflette il campo {@code posizione} di ciascun ToDo.
     */
    private List<ToDo> todos = new ArrayList<>();

    // ── Costruttori ──────────────────────────────────────────

    public Bacheca() {}

    /**
     * @param titolo      titolo della bacheca
     * @param descrizione descrizione facoltativa
     * @param utente      proprietario della bacheca
     */
    public Bacheca(TitoloBacheca titolo, String descrizione, Utente utente) {
        this.titolo      = titolo;
        this.descrizione = descrizione;
        this.utente      = utente;
    }

    // ── Gestione ToDo ────────────────────────────────────────

    /**
     * Aggiunge un ToDo in coda alla bacheca, aggiornandone la posizione.
     *
     * @param todo il ToDo da aggiungere
     */
    public void aggiungiToDo(ToDo todo) {
        todo.setPosizione(todos.size());
        todos.add(todo);
    }

    /**
     * Rimuove il ToDo dalla bacheca e ricompatta le posizioni.
     *
     * @param todo il ToDo da rimuovere
     */
    public void rimuoviToDo(ToDo todo) {
        todos.remove(todo);
        ricompattaPosizioni();
    }

    /**
     * Sposta il ToDo dalla posizione corrente a {@code nuovaPosizione},
     * riordinando di conseguenza gli altri ToDo.
     *
     * @param todo          il ToDo da spostare
     * @param nuovaPosizione la posizione di destinazione (0-based)
     */
    public void spostaToDo(ToDo todo, int nuovaPosizione) {
        if (!todos.contains(todo)) return;
        todos.remove(todo);
        int pos = Math.max(0, Math.min(nuovaPosizione, todos.size()));
        todos.add(pos, todo);
        ricompattaPosizioni();
    }

    /** Ricalcola i valori di {@code posizione} dopo ogni modifica all'ordine. */
    private void ricompattaPosizioni() {
        for (int i = 0; i < todos.size(); i++) {
            todos.get(i).setPosizione(i);
        }
    }

    // ── Getter / Setter ──────────────────────────────────────

    /** @return l'id della bacheca */
    public int getId()              { return id; }

    /** @param id l'id da impostare */
    public void setId(int id)       { this.id = id; }

    /** @return il titolo della bacheca */
    public TitoloBacheca getTitolo()                    { return titolo; }

    /** @param titolo il titolo da impostare */
    public void setTitolo(TitoloBacheca titolo)         { this.titolo = titolo; }

    /** @return la descrizione della bacheca */
    public String getDescrizione()                      { return descrizione; }

    /** @param descrizione la descrizione da impostare */
    public void setDescrizione(String descrizione)      { this.descrizione = descrizione; }

    /** @return l'utente proprietario */
    public Utente getUtente()                           { return utente; }

    /** @param utente l'utente da impostare */
    public void setUtente(Utente utente)                { this.utente = utente; }

    /** @return la lista ordinata dei ToDo */
    public List<ToDo> getTodos()                        { return todos; }

    /** @param todos la lista di ToDo da impostare */
    public void setTodos(List<ToDo> todos)              { this.todos = todos; }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) 
        	return true;
        if (!(o instanceof Bacheca)) 
        	return false;
        Bacheca b = (Bacheca) o;
        return id == b.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Bacheca{id=" + id + ", titolo=" + titolo + ", utente=" + utente + "}";
    }
}
