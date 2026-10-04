package todo.dao;

import todo.model.Bacheca;
import todo.model.TitoloBacheca;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per le operazioni CRUD sull'entità {@link Bacheca}.
 */
public interface BachecaDAO {

    /**
     * Inserisce una nuova bacheca nel database.
     * Viene aggiornato il campo {@code id} sull'oggetto passato.
     *
     * @param bacheca la bacheca da inserire
     * @throws SQLException in caso di errore SQL
     */
    void inserisci(Bacheca bacheca) throws SQLException;

    /**
     * Aggiorna la descrizione di una bacheca esistente.
     *
     * @param bacheca la bacheca con i dati aggiornati
     * @throws SQLException in caso di errore SQL
     */
    void aggiorna(Bacheca bacheca) throws SQLException;

    /**
     * Elimina una bacheca (e in cascata tutti i suoi ToDo).
     *
     * @param id l'id della bacheca da eliminare
     * @throws SQLException in caso di errore SQL
     */
    void elimina(int id) throws SQLException;

    /**
     * Recupera tutte le bacheche di un utente.
     *
     * @param loginUtente il login dell'utente
     * @return lista delle bacheche (senza i ToDo, caricati separatamente)
     * @throws SQLException in caso di errore SQL
     */
    List<Bacheca> trovaDaUtente(String loginUtente) throws SQLException;

    /**
     * Recupera una specifica bacheca di un utente tramite titolo.
     *
     * @param loginUtente il login dell'utente
     * @param titolo      il titolo della bacheca
     * @return un {@link Optional} contenente la bacheca se trovata
     * @throws SQLException in caso di errore SQL
     */
    Optional<Bacheca> trovaDaUtenteTitolo(String loginUtente, TitoloBacheca titolo)
            throws SQLException;

    /**
     * Crea le tre bacheche predefinite per un nuovo utente.
     *
     * @param loginUtente il login dell'utente
     * @throws SQLException in caso di errore SQL
     */
    void creaBackecheDefault(String loginUtente) throws SQLException;
}
