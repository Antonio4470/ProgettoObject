package todo.dao;

import todo.model.Utente;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Interfaccia DAO per le operazioni CRUD sull'entità {@link Utente}.
 */
public interface UtenteDAO {

    /**
     * Inserisce un nuovo utente nel database.
     *
     * @param utente l'utente da inserire
     * @throws SQLException in caso di errore SQL o violazione di unicità
     */
    void inserisci(Utente utente) throws SQLException;

    /**
     * Cerca un utente tramite login e password (autenticazione).
     *
     * @param login    il login dell'utente
     * @param password la password dell'utente
     * @return un {@link Optional} contenente l'utente se le credenziali sono corrette
     * @throws SQLException in caso di errore SQL
     */
    Optional<Utente> login(String login, String password) throws SQLException;

    /**
     * Cerca un utente per login.
     *
     * @param login il login da cercare
     * @return un {@link Optional} contenente l'utente se trovato
     * @throws SQLException in caso di errore SQL
     */
    Optional<Utente> trovaPerId(String login) throws SQLException;

    /**
     * Elimina un utente dal database.
     *
     * @param login il login dell'utente da eliminare
     * @throws SQLException in caso di errore SQL
     */
    void elimina(String login) throws SQLException;
}
