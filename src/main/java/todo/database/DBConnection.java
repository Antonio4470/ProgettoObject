package todo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce la connessione al database PostgreSQL tramite il pattern Singleton.
 * <p>
 * Fornisce un singolo oggetto {@link Connection} riutilizzato da tutti i DAO.
 * In un'applicazione multi-thread si dovrebbe sostituire con un connection pool
 * (es. HikariCP), ma per questa applicazione desktop mono-utente è sufficiente.
 *
 * <h3>Configurazione</h3>
 * Modificare le costanti {@code URL}, {@code USER} e {@code PASSWORD}
 * in base all'ambiente di esecuzione, oppure caricarle da un file
 * {@code db.properties} nella cartella risorse.
 */
public class DBConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/todomanager";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "Antonio2005.";

    /** Unica istanza della connessione. */
    private static Connection instance;

    /** Costruttore privato: impedisce istanziazioni esterne. */
    private DBConnection() {}

    /**
     * Restituisce la connessione al database, creandola se non esiste
     * o se è stata chiusa/invalidata.
     *
     * @return la {@link Connection} attiva
     * @throws SQLException se la connessione non può essere stabilita
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
            instance.setAutoCommit(true);
        }
        return instance;
    }

    /**
     * Chiude la connessione al database, se aperta.
     * Da invocare alla chiusura dell'applicazione.
     */
    public static void closeConnection() {
        if (instance != null) {
            try {
                if (!instance.isClosed()) instance.close();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Errore chiusura: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }
}
