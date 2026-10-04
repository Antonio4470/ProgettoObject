package todo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entità centrale del sistema. Rappresenta un'attività da svolgere.
 * <p>
 * Un ToDo appartiene a una {@link Bacheca} ed è creato da un {@link Utente} autore.
 * Può essere condiviso con altri utenti: in quel caso apparirà nella bacheca
 * corrispondente (stesso {@link TitoloBacheca}) di ciascun utente condiviso.
 * <p>
 * Può contenere una {@link Checklist} opzionale; quando tutte le attività della
 * checklist sono completate, il ToDo viene automaticamente marcato come completato.
 */
public class ToDo {

    private int            id;
    private String         titolo;
    private LocalDate      dataScadenza;
    private String         colore;
    private int            posizione;
    private String         url;
    private String         descrizione;
    private byte[]         immagine;
    private StatoToDo      stato;

    /** Bacheca in cui si trova questo ToDo. */
    private Bacheca        bacheca;

    /** Utente che ha creato il ToDo. */
    private Utente         autore;

    /** Lista degli utenti con cui il ToDo è condiviso (esclude l'autore). */
    private List<Utente>   condivisioni = new ArrayList<>();

    /** Checklist opzionale (può essere null se non ancora creata). */
    private Checklist      checklist;

    // ── Costruttori ──────────────────────────────────────────

    public ToDo() {
        this.stato = StatoToDo.NonCompletato;
    }

    /**
     * @param titolo   titolo del ToDo
     * @param bacheca  bacheca di appartenenza
     * @param autore   utente creatore
     */
    public ToDo(String titolo, Bacheca bacheca, Utente autore) {
        this.titolo   = titolo;
        this.bacheca  = bacheca;
        this.autore   = autore;
        this.stato    = StatoToDo.NonCompletato;
        this.posizione = 0;
    }

    // ── Business logic ───────────────────────────────────────

    /**
     * Verifica se il ToDo è scaduto rispetto alla data odierna.
     *
     * @return {@code true} se la data di scadenza è nel passato
     */
    public boolean isScaduto() {
        return dataScadenza != null && dataScadenza.isBefore(LocalDate.now());
    }

    /**
     * Verifica se il ToDo scade oggi.
     *
     * @return {@code true} se la data di scadenza è la data odierna
     */
    public boolean scadeOggi() {
        return LocalDate.now().equals(dataScadenza);
    }

    /**
     * Verifica se il ToDo scade entro la data indicata (inclusa).
     *
     * @param entro data limite
     * @return {@code true} se la scadenza è ≤ entro
     */
    public boolean scadeEntro(LocalDate entro) {
        return dataScadenza != null && !dataScadenza.isAfter(entro);
    }

    /**
     * Aggiunge un utente alla lista di condivisione.
     * Non ha effetto se l'utente è già presente o è l'autore.
     *
     * @param u utente da aggiungere
     */
    public void aggiungiCondivisione(Utente u) {
        if (!u.equals(autore) && !condivisioni.contains(u)) {
            condivisioni.add(u);
        }
    }

    /**
     * Rimuove un utente dalla lista di condivisione.
     *
     * @param u utente da rimuovere
     */
    public void rimuoviCondivisione(Utente u) {
        condivisioni.remove(u);
    }

    /**
     * Crea e associa una nuova checklist vuota a questo ToDo.
     * Se esiste già, non fa nulla.
     */
    public void creaChecklist() {
        if (this.checklist == null) {
            this.checklist = new Checklist();
        }
    }

    /**
     * Controlla se tutte le attività della checklist sono completate e,
     * in caso affermativo, aggiorna automaticamente lo stato del ToDo.
     * Deve essere chiamato ogni volta che un'attività cambia stato.
     */
    public void aggiornaStatoDaChecklist() {
        if (checklist != null && checklist.tutteCompletate()) {
            this.stato = StatoToDo.Completato;
        }
    }

    // ── Getter / Setter ──────────────────────────────────────

    /** @return l'id del ToDo */
    public int getId()                              { return id; }
    /** @param id l'id da impostare */
    public void setId(int id)                       { this.id = id; }

    /** @return il titolo del ToDo */
    public String getTitolo()                       { return titolo; }
    /** @param titolo il titolo da impostare */
    public void setTitolo(String titolo)            { this.titolo = titolo; }

    /** @return la data di scadenza, o {@code null} se non impostata */
    public LocalDate getDataScadenza()              { return dataScadenza; }
    /** @param dataScadenza la data di scadenza da impostare */
    public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza = dataScadenza; }

    /** @return il colore di sfondo (nome CSS o hex) */
    public String getColore()                       { return colore; }
    /** @param colore il colore da impostare */
    public void setColore(String colore)            { this.colore = colore; }

    /** @return la posizione nella bacheca (0-based) */
    public int getPosizione()                       { return posizione; }
    /** @param posizione la posizione da impostare */
    public void setPosizione(int posizione)         { this.posizione = posizione; }

    /** @return l'URL correlata, o {@code null} */
    public String getUrl()                          { return url; }
    /** @param url l'URL da impostare */
    public void setUrl(String url)                  { this.url = url; }

    /** @return la descrizione dettagliata, o {@code null} */
    public String getDescrizione()                  { return descrizione; }
    /** @param descrizione la descrizione da impostare */
    public void setDescrizione(String descrizione)  { this.descrizione = descrizione; }

    /** @return i byte dell'immagine allegata, o {@code null} */
    public byte[] getImmagine()                     { return immagine; }
    /** @param immagine i byte dell'immagine da impostare */
    public void setImmagine(byte[] immagine)        { this.immagine = immagine; }

    /** @return lo stato del ToDo */
    public StatoToDo getStato()                     { return stato; }
    /** @param stato lo stato da impostare */
    public void setStato(StatoToDo stato)           { this.stato = stato; }

    /** @return la bacheca di appartenenza */
    public Bacheca getBacheca()                     { return bacheca; }
    /** @param bacheca la bacheca da impostare */
    public void setBacheca(Bacheca bacheca)         { this.bacheca = bacheca; }

    /** @return l'utente autore del ToDo */
    public Utente getAutore()                       { return autore; }
    /** @param autore l'autore da impostare */
    public void setAutore(Utente autore)            { this.autore = autore; }

    /** @return la lista degli utenti con cui il ToDo è condiviso */
    public List<Utente> getCondivisioni()           { return condivisioni; }
    /** @param condivisioni la lista di condivisioni da impostare */
    public void setCondivisioni(List<Utente> condivisioni) { this.condivisioni = condivisioni; }

    /** @return la checklist associata, o {@code null} se assente */
    public Checklist getChecklist()                 { return checklist; }
    /** @param checklist la checklist da impostare */
    public void setChecklist(Checklist checklist)   { this.checklist = checklist; }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) 
        	return true;
        if (!(o instanceof ToDo)) 
        	return false;
        ToDo t = (ToDo) o;
        return id == t.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ToDo{id=" + id + ", titolo='" + titolo + "', stato=" + stato + "}";
    }
}
