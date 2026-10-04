package todo.dao.impl;

import todo.dao.UtenteDAO;
import todo.database.DBConnection;
import todo.model.Utente;

import java.sql.*;
import java.util.Optional;

/**
 * Implementazione PostgreSQL di {@link UtenteDAO}.
 */
public class PostgresUtenteDAO implements UtenteDAO {

    @Override
    public void inserisci(Utente utente) throws SQLException {
        String sql = "INSERT INTO utente (login, password) VALUES (?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, utente.getLogin());
            ps.setString(2, utente.getPassword());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Utente> login(String login, String password) throws SQLException {
        String sql = "SELECT login, password FROM utente WHERE login = ? AND password = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utente> trovaPerId(String login) throws SQLException {
        String sql = "SELECT login, password FROM utente WHERE login = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public void elimina(String login) throws SQLException {
        String sql = "DELETE FROM utente WHERE login = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, login);
            ps.executeUpdate();
        }
    }

    /** Mappa una riga del ResultSet in un oggetto {@link Utente}. */
    private Utente mapRow(ResultSet rs) throws SQLException {
        return new Utente(rs.getString("login"), rs.getString("password"));
    }
}
