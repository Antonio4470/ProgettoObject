package todo.controller;

import todo.dao.*;
import todo.dao.impl.*;
import todo.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller principale del sistema (strato Control nel pattern BCE).
 * <p>
 * Coordina l'interazione tra il boundary (GUI) e i DAO, applicando
 * la logica di business che non appartiene alle singole entità del modello
 * (es. completamento automatico del ToDo alla chiusura della checklist).
 * <p>
 * È il punto di accesso unico per la GUI: ogni azione dell'utente
 * viene delegata a questo controller, che invoca i DAO appropriati.
 */
public class ToDoController {

    // ── DAO ──────────────────────────────────────────────────
    private final UtenteDAO    utenteDAO    = new PostgresUtenteDAO();
    private final BachecaDAO   bachecaDAO   = new PostgresBachecaDAO();
    private final ToDoDAO      todoDAO      = new PostgresToDoDAO();
    private final ChecklistDAO checklistDAO = new PostgresChecklistDAO();

    /** Utente attualmente autenticato (sessione corrente). */
    private Utente utenteCorrente;

    // ── AUTENTICAZIONE ───────────────────────────────────────

    /**
     * Autentica un utente.
     *
     * @param login    il login dell'utente
     * @param password la password dell'utente
     * @return {@code true} se le credenziali sono corrette
     * @throws SQLException in caso di errore SQL
     */
    public boolean login(String login, String password) throws SQLException {
        Optional<Utente> u = utenteDAO.login(login, password);
        u.ifPresent(value -> this.utenteCorrente = value);
        return u.isPresent();
    }

    /**
     * Registra un nuovo utente e crea le sue tre bacheche predefinite.
     *
     * @param login    il login desiderato
     * @param password la password desiderata
     * @throws SQLException in caso di errore (es. login già esistente)
     */
    public void registra(String login, String password) throws SQLException {
        Utente u = new Utente(login, password);
        utenteDAO.inserisci(u);
        bachecaDAO.creaBackecheDefault(login);
    }

    /** Disconnette l'utente corrente dalla sessione. */
    public void logout() {
        this.utenteCorrente = null;
    }

    /** @return l'utente attualmente autenticato, o {@code null} */
    public Utente getUtenteCorrente() { return utenteCorrente; }

    // ── BACHECA ──────────────────────────────────────────────

    /**
     * Restituisce le bacheche dell'utente corrente.
     *
     * @return lista delle bacheche
     * @throws SQLException in caso di errore SQL
     */
    public List<Bacheca> getBacheche() throws SQLException {
        checkAuth();
        return bachecaDAO.trovaDaUtente(utenteCorrente.getLogin());
    }

    /**
     * Aggiorna la descrizione di una bacheca.
     *
     * @param bacheca la bacheca da aggiornare
     * @throws SQLException in caso di errore SQL
     */
    public void aggiornaBacheca(Bacheca bacheca) throws SQLException {
        checkAuth();
        bachecaDAO.aggiorna(bacheca);
    }

    // ── TODO CRUD ────────────────────────────────────────────

    /**
     * Crea un nuovo ToDo nella bacheca indicata.
     *
     * @param titolo    titolo del ToDo
     * @param idBacheca id della bacheca di destinazione
     * @return il ToDo creato con id assegnato
     * @throws SQLException in caso di errore SQL
     */
    public ToDo creaToDo(String titolo, int idBacheca) throws SQLException {
        checkAuth();
        Bacheca b = new Bacheca();
        b.setId(idBacheca);
        ToDo todo = new ToDo(titolo, b, utenteCorrente);
        todoDAO.inserisci(todo);
        return todo;
    }

    /**
     * Aggiorna i dati di un ToDo esistente.
     *
     * @param todo il ToDo con i dati modificati
     * @throws SQLException in caso di errore SQL
     */
    public void aggiornaToDo(ToDo todo) throws SQLException {
        checkAuth();
        todoDAO.aggiorna(todo);
    }

    /**
     * Elimina un ToDo.
     *
     * @param idTodo id del ToDo da eliminare
     * @throws SQLException in caso di errore SQL
     */
    public void eliminaToDo(int idTodo) throws SQLException {
        checkAuth();
        todoDAO.elimina(idTodo);
    }

    /**
     * Sposta un ToDo in una bacheca (eventualmente diversa) e ne aggiorna la posizione.
     *
     * @param idTodo     id del ToDo da spostare
     * @param idBacheca  id della bacheca di destinazione
     * @param posizione  nuova posizione (0-based)
     * @throws SQLException in caso di errore SQL
     */
    public void spostaToDo(int idTodo, int idBacheca, int posizione) throws SQLException {
        checkAuth();
        todoDAO.sposta(idTodo, idBacheca, posizione);
    }

    // ── TODO QUERY ───────────────────────────────────────────

    /**
     * Restituisce i ToDo visibili per l'utente corrente in una bacheca.
     *
     * @param idBacheca id della bacheca
     * @return lista ordinata di ToDo (propri + condivisi)
     * @throws SQLException in caso di errore SQL
     */
    public List<ToDo> getToDoDiBacheca(int idBacheca) throws SQLException {
        checkAuth();
        return todoDAO.trovaDaUtenteEBacheca(utenteCorrente.getLogin(), idBacheca);
    }

    /**
     * Restituisce i ToDo in scadenza oggi per l'utente corrente.
     *
     * @return lista di ToDo con scadenza odierna
     * @throws SQLException in caso di errore SQL
     */
    public List<ToDo> getToDoInScadenzaOggi() throws SQLException {
        checkAuth();
        return todoDAO.trovaInScadenzaOggi(utenteCorrente.getLogin());
    }

    /**
     * Restituisce i ToDo in scadenza entro una data specificata.
     *
     * @param entro data limite (inclusa)
     * @return lista di ToDo
     * @throws SQLException in caso di errore SQL
     */
    public List<ToDo> getToDoInScadenzaEntro(LocalDate entro) throws SQLException {
        checkAuth();
        return todoDAO.trovaInScadenzaEntro(utenteCorrente.getLogin(), entro);
    }

    /**
     * Ricerca i ToDo dell'utente corrente per testo nel titolo.
     *
     * @param testo testo da cercare
     * @return lista di ToDo corrispondenti
     * @throws SQLException in caso di errore SQL
     */
    public List<ToDo> cercaToDo(String testo) throws SQLException {
        checkAuth();
        return todoDAO.cercaPerTitolo(utenteCorrente.getLogin(), testo);
    }

    // ── CONDIVISIONI ─────────────────────────────────────────

    /**
     * Aggiunge una condivisione a un ToDo.
     * Solo l'autore può aggiungere condivisioni.
     *
     * @param todo          il ToDo su cui aggiungere la condivisione
     * @param loginCondiviso il login dell'utente con cui condividere
     * @throws SQLException            in caso di errore SQL
     * @throws IllegalArgumentException se l'utente corrente non è l'autore
     */
    public void aggiungiCondivisione(ToDo todo, String loginCondiviso)
            throws SQLException {
        checkAuth();
        checkAutore(todo);
        todoDAO.aggiungiCondivisione(todo.getId(), loginCondiviso);
    }

    /**
     * Rimuove una condivisione da un ToDo.
     * Solo l'autore può rimuovere condivisioni.
     *
     * @param todo          il ToDo da cui rimuovere la condivisione
     * @param loginCondiviso il login dell'utente da rimuovere
     * @throws SQLException            in caso di errore SQL
     * @throws IllegalArgumentException se l'utente corrente non è l'autore
     */
    public void rimuoviCondivisione(ToDo todo, String loginCondiviso)
            throws SQLException {
        checkAuth();
        checkAutore(todo);
        todoDAO.rimuoviCondivisione(todo.getId(), loginCondiviso);
    }

    // ── CHECKLIST ────────────────────────────────────────────

    /**
     * Crea una checklist vuota per il ToDo indicato.
     *
     * @param todo il ToDo a cui associare la checklist
     * @throws SQLException in caso di errore SQL
     */
    public void creaChecklist(ToDo todo) throws SQLException {
        checkAuth();
        todo.creaChecklist();
        checklistDAO.creaChecklist(todo.getChecklist(), todo.getId());
    }

    /**
     * Aggiunge un'attività alla checklist di un ToDo.
     *
     * @param todo     il ToDo con la checklist
     * @param nomeAtt  nome della nuova attività
     * @return l'attività creata
     * @throws SQLException            in caso di errore SQL
     * @throws IllegalStateException   se il ToDo non ha una checklist
     */
    public Attivita aggiungiAttivita(ToDo todo, String nomeAtt) throws SQLException {
        checkAuth();
        if (todo.getChecklist() == null)
            throw new IllegalStateException("Il ToDo non ha una checklist.");
        Attivita a = new Attivita(nomeAtt);
        checklistDAO.aggiungiAttivita(a, todo.getChecklist().getId());
        todo.getChecklist().aggiungiAttivita(a);
        return a;
    }
    /**
     * Ricarica la checklist (con tutte le attività aggiornate) di un ToDo
     * direttamente dal database, impostandola sull'oggetto passato.
     *
     * @param todo il ToDo di cui aggiornare la checklist
     * @throws SQLException in caso di errore SQL
     */
    public void getChecklist(ToDo todo) throws SQLException {
        checkAuth();
        checklistDAO.trovaDaToDo(todo.getId()).ifPresent(todo::setChecklist);
    }

    /**
     * Segna un'attività come completata e, se tutte le attività sono
     * completate, aggiorna automaticamente lo stato del ToDo.
     *
     * @param todo     il ToDo contenente l'attività
     * @param attivita l'attività da completare
     * @throws SQLException in caso di errore SQL
     */
    public void completaAttivita(ToDo todo, Attivita attivita) throws SQLException {
        checkAuth();
        attivita.completa();
        checklistDAO.aggiornaAttivita(attivita);

        // Completamento automatico del ToDo
        todo.aggiornaStatoDaChecklist();
        if (todo.getStato() == StatoToDo.Completato) {
            todoDAO.aggiorna(todo);
        }
    }

    /**
     * Elimina un'attività dalla checklist.
     *
     * @param todo     il ToDo contenente la checklist
     * @param attivita l'attività da eliminare
     * @throws SQLException in caso di errore SQL
     */
    public void eliminaAttivita(ToDo todo, Attivita attivita) throws SQLException {
        checkAuth();
        checklistDAO.eliminaAttivita(attivita.getId());
        if (todo.getChecklist() != null)
            todo.getChecklist().rimuoviAttivita(attivita);
    }

    // ── GUARD METHODS ────────────────────────────────────────

    /** Verifica che l'utente sia autenticato. */
    private void checkAuth() {
        if (utenteCorrente == null)
            throw new IllegalStateException("Nessun utente autenticato.");
    }

    /** Verifica che l'utente corrente sia l'autore del ToDo. */
    private void checkAutore(ToDo todo) {
        if (!todo.getAutore().getLogin().equals(utenteCorrente.getLogin()))
            throw new IllegalArgumentException(
                "Solo l'autore può modificare le condivisioni.");
    }
}
