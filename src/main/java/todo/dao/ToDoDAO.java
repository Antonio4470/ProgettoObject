package todo.dao;

import todo.model.ToDo;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per le operazioni CRUD e di ricerca sull'entità {@link ToDo}.
 */
public interface ToDoDAO {

    /**
     * Inserisce un nuovo ToDo nel database.
     * Aggiorna il campo {@code id} sull'oggetto passato.
     *
     * @param todo il ToDo da inserire
     * @throws SQLException in caso di errore SQL
     */
    void inserisci(ToDo todo) throws SQLException;

    /**
     * Aggiorna tutti i campi modificabili di un ToDo esistente.
     *
     * @param todo il ToDo con i dati aggiornati
     * @throws SQLException in caso di errore SQL
     */
    void aggiorna(ToDo todo) throws SQLException;

    /**
     * Elimina un ToDo e tutte le sue dipendenze (checklist, condivisioni).
     *
     * @param id l'id del ToDo da eliminare
     * @throws SQLException in caso di errore SQL
     */
    void elimina(int id) throws SQLException;

    /**
     * Sposta un ToDo in una bacheca diversa o aggiorna la sua posizione.
     *
     * @param idTodo     l'id del ToDo da spostare
     * @param idBacheca  l'id della bacheca di destinazione
     * @param posizione  la nuova posizione nella bacheca
     * @throws SQLException in caso di errore SQL
     */
    void sposta(int idTodo, int idBacheca, int posizione) throws SQLException;

    /**
     * Recupera un ToDo tramite id, con tutte le sue associazioni.
     *
     * @param id l'id del ToDo
     * @return un {@link Optional} contenente il ToDo se trovato
     * @throws SQLException in caso di errore SQL
     */
    Optional<ToDo> trovaPerId(int id) throws SQLException;

    /**
     * Recupera tutti i ToDo di una bacheca, ordinati per posizione.
     *
     * @param idBacheca l'id della bacheca
     * @return lista ordinata di ToDo
     * @throws SQLException in caso di errore SQL
     */
    List<ToDo> trovaDaBacheca(int idBacheca) throws SQLException;

    /**
     * Recupera i ToDo visibili per un utente (propri + condivisi con lui)
     * in una specifica bacheca.
     *
     * @param loginUtente il login dell'utente
     * @param idBacheca   l'id della bacheca
     * @return lista ordinata di ToDo
     * @throws SQLException in caso di errore SQL
     */
    List<ToDo> trovaDaUtenteEBacheca(String loginUtente, int idBacheca) throws SQLException;

    /**
     * Recupera i ToDo in scadenza oggi per un utente.
     *
     * @param loginUtente il login dell'utente
     * @return lista di ToDo con scadenza odierna
     * @throws SQLException in caso di errore SQL
     */
    List<ToDo> trovaInScadenzaOggi(String loginUtente) throws SQLException;

    /**
     * Recupera i ToDo in scadenza entro una data specificata per un utente.
     *
     * @param loginUtente il login dell'utente
     * @param entro       data limite (inclusa)
     * @return lista di ToDo con scadenza entro la data indicata
     * @throws SQLException in caso di errore SQL
     */
    List<ToDo> trovaInScadenzaEntro(String loginUtente, LocalDate entro) throws SQLException;

    /**
     * Ricerca i ToDo di un utente per titolo (ricerca parziale, case-insensitive).
     *
     * @param loginUtente il login dell'utente
     * @param testo       testo da cercare nel titolo
     * @return lista di ToDo corrispondenti
     * @throws SQLException in caso di errore SQL
     */
    List<ToDo> cercaPerTitolo(String loginUtente, String testo) throws SQLException;

    /**
     * Aggiunge un utente alla lista di condivisione di un ToDo.
     *
     * @param idTodo      l'id del ToDo
     * @param loginUtente il login dell'utente da aggiungere
     * @throws SQLException in caso di errore SQL
     */
    void aggiungiCondivisione(int idTodo, String loginUtente) throws SQLException;

    /**
     * Rimuove un utente dalla lista di condivisione di un ToDo.
     *
     * @param idTodo      l'id del ToDo
     * @param loginUtente il login dell'utente da rimuovere
     * @throws SQLException in caso di errore SQL
     */
    void rimuoviCondivisione(int idTodo, String loginUtente) throws SQLException;
}
