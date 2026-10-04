package todo.dao;

import todo.model.Attivita;
import todo.model.Checklist;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Interfaccia DAO per le operazioni CRUD su {@link Checklist} e {@link Attivita}.
 */
public interface ChecklistDAO {

    /**
     * Crea una nuova checklist vuota associata a un ToDo.
     * Aggiorna il campo {@code id} sull'oggetto passato.
     *
     * @param checklist la checklist da creare
     * @param idTodo    l'id del ToDo proprietario
     * @throws SQLException in caso di errore SQL
     */
    void creaChecklist(Checklist checklist, int idTodo) throws SQLException;

    /**
     * Elimina la checklist (e tutte le sue attività) di un ToDo.
     *
     * @param idTodo l'id del ToDo
     * @throws SQLException in caso di errore SQL
     */
    void eliminaChecklist(int idTodo) throws SQLException;

    /**
     * Recupera la checklist di un ToDo con tutte le sue attività.
     *
     * @param idTodo l'id del ToDo
     * @return un {@link Optional} contenente la checklist se esiste
     * @throws SQLException in caso di errore SQL
     */
    Optional<Checklist> trovaDaToDo(int idTodo) throws SQLException;

    /**
     * Aggiunge una nuova attività alla checklist.
     * Aggiorna il campo {@code id} sull'oggetto passato.
     *
     * @param attivita    l'attività da aggiungere
     * @param idChecklist l'id della checklist
     * @throws SQLException in caso di errore SQL
     */
    void aggiungiAttivita(Attivita attivita, int idChecklist) throws SQLException;

    /**
     * Visualizza (stampa) i dettagli di una singola attività a partire dal suo id.
     *
     * @param idAttivita l'id dell'attività da visualizzare
     * @throws SQLException in caso di errore SQL
     */
    void visualizzaAttivita(int idAttivita) throws SQLException;

    /**
     * Aggiorna lo stato di un'attività.
     *
     * @param attivita l'attività con lo stato aggiornato
     * @throws SQLException in caso di errore SQL
     */
    void aggiornaAttivita(Attivita attivita) throws SQLException;

    /**
     * Elimina un'attività dalla checklist.
     *
     * @param idAttivita l'id dell'attività da eliminare
     * @throws SQLException in caso di errore SQL
     */
    void eliminaAttivita(int idAttivita) throws SQLException;
}