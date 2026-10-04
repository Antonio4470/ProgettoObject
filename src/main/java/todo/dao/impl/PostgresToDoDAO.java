package todo.dao.impl;

import todo.dao.ToDoDAO;
import todo.database.DBConnection;
import todo.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione PostgreSQL di {@link ToDoDAO}.
 * Gestisce le operazioni CRUD sui ToDo, incluse le condivisioni.
 */
public class PostgresToDoDAO implements ToDoDAO {

    // ── INSERT ───────────────────────────────────────────────

    @Override
    public void inserisci(ToDo todo) throws SQLException {
        String sql = "INSERT INTO todo (titolo, data_scadenza, colore, posizione, url, "+
           "descrizione, immagine, stato, id_bacheca, login_autore) "+
           "VALUES (?,?,?,?,?,?,?,?::stato_todo,?,?) RETURNING id";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todo.getTitolo());
			//scadenza a 30 giorni da oggi
			LocalDate data = LocalDate.now().plusDays(30);
			ps.setDate(2, java.sql.Date.valueOf(data));
            //ps.setObject(2, todo.getDataScadenza());
            ps.setString(3, todo.getColore());
            ps.setInt   (4, todo.getPosizione());
            ps.setString(5, todo.getUrl());
            ps.setString(6, todo.getDescrizione());
            ps.setBytes (7, todo.getImmagine());
            ps.setString(8, todo.getStato().toString());
            ps.setInt   (9, todo.getBacheca().getId());
            ps.setString(10, todo.getAutore().getLogin());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) todo.setId(rs.getInt("id"));
            }
        }
    }

    // ── UPDATE ───────────────────────────────────────────────

    @Override
    public void aggiorna(ToDo todo) throws SQLException {
        String sql = "UPDATE todo SET "+
              "titolo        = ?, "+
              "data_scadenza = ?, "+
              "colore        = ?, "+
              "url           = ?, "+
              "descrizione   = ?, "+
              "immagine      = ?, "+
              "stato         = ?::stato_todo "+
            "WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, todo.getTitolo());
            ps.setObject(2, todo.getDataScadenza());
            ps.setString(3, todo.getColore());
            ps.setString(4, todo.getUrl());
            ps.setString(5, todo.getDescrizione());
            ps.setBytes (6, todo.getImmagine());
            ps.setString(7, todo.getStato().toString());
            ps.setInt   (8, todo.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void sposta(int idTodo, int idBacheca, int posizione) throws SQLException {
        String sql = "UPDATE todo SET id_bacheca = ?, posizione = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idBacheca);
            ps.setInt(2, posizione);
            ps.setInt(3, idTodo);
            ps.executeUpdate();
        }
    }

    // ── DELETE ───────────────────────────────────────────────

    @Override
    public void elimina(int id) throws SQLException {
        String sql = "DELETE FROM todo WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── SELECT ───────────────────────────────────────────────

    @Override
    public Optional<ToDo> trovaPerId(int id) throws SQLException {
        String sql = "SELECT t.*, b.titolo AS tit_bacheca, b.login_utente, "+
                   "b.descrizione AS desc_bacheca "+
            "FROM todo t JOIN bacheca b ON t.id_bacheca = b.id "+
            "WHERE t.id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ToDo todo = mapRow(rs);
                    caricaCondivisioni(todo);
                    return Optional.of(todo);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ToDo> trovaDaBacheca(int idBacheca) throws SQLException {
        String sql = "SELECT t.*, b.titolo AS tit_bacheca, b.login_utente, "+
                   "b.descrizione AS desc_bacheca "+
            "FROM todo t JOIN bacheca b ON t.id_bacheca = b.id "+
            "WHERE t.id_bacheca = ? "+
            "ORDER BY t.posizione";
        return eseguiQuery(sql, idBacheca);
    }

    @Override
    public List<ToDo> trovaDaUtenteEBacheca(String loginUtente, int idBacheca)
            throws SQLException {
        // Recupera i ToDo propri + quelli condivisi con l'utente nella bacheca
        String sql = "SELECT DISTINCT t.*, b.titolo AS tit_bacheca, b.login_utente, "+
                   "b.descrizione AS desc_bacheca "+
            "FROM todo t "+
            "JOIN bacheca b ON t.id_bacheca = b.id "+
            "LEFT JOIN condivisione c ON c.id_todo = t.id "+
            "WHERE t.id_bacheca = ? "+
              "AND (t.login_autore = ? OR c.login_utente = ?) "+
            "ORDER BY t.posizione";
        List<ToDo> result = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt   (1, idBacheca);
            ps.setString(2, loginUtente);
            ps.setString(3, loginUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ToDo t = mapRow(rs);
                    caricaCondivisioni(t);
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public List<ToDo> trovaInScadenzaOggi(String loginUtente) throws SQLException {
        String sql = "SELECT DISTINCT t.*, b.titolo AS tit_bacheca, b.login_utente, "+
                   "b.descrizione AS desc_bacheca "+
            "FROM todo t "+
            "JOIN bacheca b ON t.id_bacheca = b.id "+
            "LEFT JOIN condivisione c ON c.id_todo = t.id "+
            "WHERE t.data_scadenza = CURRENT_DATE "+
              "AND (t.login_autore = ? OR c.login_utente = ?) "+
            "ORDER BY t.posizione";
        List<ToDo> result = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, loginUtente);
            ps.setString(2, loginUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public List<ToDo> trovaInScadenzaEntro(String loginUtente, LocalDate entro)
            throws SQLException {
        String sql = "SELECT DISTINCT t.*, b.titolo AS tit_bacheca, b.login_utente, "+
                   "b.descrizione AS desc_bacheca "+
            "FROM todo t "+
            "JOIN bacheca b ON t.id_bacheca = b.id "+
            "LEFT JOIN condivisione c ON c.id_todo = t.id "+
            "WHERE t.data_scadenza <= ? "+
              "AND (t.login_autore = ? OR c.login_utente = ?) "+
            "ORDER BY t.data_scadenza, t.posizione";
        List<ToDo> result = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDate  (1, Date.valueOf(entro));
            ps.setString(2, loginUtente);
            ps.setString(3, loginUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public List<ToDo> cercaPerTitolo(String loginUtente, String testo) throws SQLException {
        String sql = "SELECT DISTINCT t.*, b.titolo AS tit_bacheca, b.login_utente, "+
                   "b.descrizione AS desc_bacheca "+
            "FROM todo t "+
            "JOIN bacheca b ON t.id_bacheca = b.id "+
            "LEFT JOIN condivisione c ON c.id_todo = t.id "+
            "WHERE LOWER(t.titolo) LIKE LOWER(?) "+
              "AND (t.login_autore = ? OR c.login_utente = ?) "+
            "ORDER BY t.titolo";
        List<ToDo> result = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + testo + "%");
            ps.setString(2, loginUtente);
            ps.setString(3, loginUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    // ── CONDIVISIONI ─────────────────────────────────────────

    @Override
    public void aggiungiCondivisione(int idTodo, String loginUtente) throws SQLException {
        String sql = "INSERT INTO condivisione (id_todo, login_utente) VALUES (?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt   (1, idTodo);
            ps.setString(2, loginUtente);
            ps.executeUpdate();
        }
    }

    @Override
    public void rimuoviCondivisione(int idTodo, String loginUtente) throws SQLException {
        String sql = "DELETE FROM condivisione WHERE id_todo = ? AND login_utente = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt   (1, idTodo);
            ps.setString(2, loginUtente);
            ps.executeUpdate();
        }
    }

    // ── HELPER PRIVATI ───────────────────────────────────────

    /**
     * Esegue una query con un singolo parametro intero e ritorna la lista di ToDo.
     */
    private List<ToDo> eseguiQuery(String sql, int param) throws SQLException {
        List<ToDo> result = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ToDo t = mapRow(rs);
                    caricaCondivisioni(t);
                    result.add(t);
                }
            }
        }
        return result;
    }

    /**
     * Mappa una riga del ResultSet in un oggetto {@link ToDo}.
     * Non carica le condivisioni (eseguire {@link #caricaCondivisioni} separatamente).
     */
    private ToDo mapRow(ResultSet rs) throws SQLException {
        ToDo todo = new ToDo();
        todo.setId          (rs.getInt("id"));
        todo.setTitolo      (rs.getString("titolo"));
        todo.setColore      (rs.getString("colore"));
        todo.setPosizione   (rs.getInt("posizione"));
        todo.setUrl         (rs.getString("url"));
        todo.setDescrizione (rs.getString("descrizione"));
        todo.setImmagine    (rs.getBytes("immagine"));
        todo.setStato       (StatoToDo.fromString(rs.getString("stato")));

        Date d = rs.getDate("data_scadenza");
        if (d != null) todo.setDataScadenza(d.toLocalDate());

        // Bacheca (lazy: solo id e titolo)
        Bacheca b = new Bacheca();
        b.setId(rs.getInt("id_bacheca"));
        b.setTitolo(TitoloBacheca.fromString(rs.getString("tit_bacheca")));
        b.setDescrizione(rs.getString("desc_bacheca"));
        Utente owner = new Utente(rs.getString("login_utente"), null);
        b.setUtente(owner);
        todo.setBacheca(b);

        // Autore
        todo.setAutore(new Utente(rs.getString("login_autore"), null));
        return todo;
    }

    /**
     * Carica le condivisioni di un ToDo e le aggiunge all'oggetto.
     *
     * @param todo il ToDo a cui aggiungere le condivisioni
     * @throws SQLException in caso di errore SQL
     */
    private void caricaCondivisioni(ToDo todo) throws SQLException {
        String sql = "SELECT login_utente FROM condivisione WHERE id_todo = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, todo.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<Utente> condivisioni = new ArrayList<>();
                while (rs.next()) {
                    condivisioni.add(new Utente(rs.getString("login_utente"), null));
                }
                todo.setCondivisioni(condivisioni);
            }
        }
    }
}
