package todo.dao.impl;

import todo.dao.BachecaDAO;
import todo.database.DBConnection;
import todo.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione PostgreSQL di {@link BachecaDAO}.
 */
public class PostgresBachecaDAO implements BachecaDAO {

    @Override
    public void inserisci(Bacheca bacheca) throws SQLException {
        String sql = "INSERT INTO bacheca (titolo, descrizione, login_utente) "+
            "VALUES (?, ?, ?) "+
            "RETURNING id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setObject(1, bacheca.getTitolo().name(), Types.OTHER);
            ps.setString(2, bacheca.getDescrizione());
            ps.setString(3, bacheca.getUtente().getLogin());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) bacheca.setId(rs.getInt("id"));
            }
        }
    }

    @Override
    public void aggiorna(Bacheca bacheca) throws SQLException {
        String sql = "UPDATE bacheca SET descrizione = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, bacheca.getDescrizione());
            ps.setInt(2, bacheca.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void elimina(int id) throws SQLException {
        String sql = "DELETE FROM bacheca WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Bacheca> trovaDaUtente(String loginUtente) throws SQLException {
        String sql = "SELECT id, titolo, descrizione, login_utente "+
            "FROM bacheca "+
            "WHERE login_utente = ? "+
            "ORDER BY id";
        List<Bacheca> bacheche = new ArrayList<>();
        Utente utente = new Utente(loginUtente, null);
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, loginUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bacheche.add(mapRow(rs, utente));
            }
        }
        return bacheche;
    }

    @Override
    public Optional<Bacheca> trovaDaUtenteTitolo(String loginUtente, TitoloBacheca titolo)
            throws SQLException {
        String sql = "SELECT id, titolo, descrizione, login_utente "+
            "FROM bacheca "+
            "WHERE login_utente = ? AND titolo = ?::titolo_bacheca";
        Utente utente = new Utente(loginUtente, null);
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, loginUtente);
            ps.setString(2, titolo.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs, utente));
            }
        }
        return Optional.empty();
    }

    @Override
    public void creaBackecheDefault(String loginUtente) throws SQLException {
        Utente u = new Utente(loginUtente, null);
        for (TitoloBacheca t : TitoloBacheca.values()) {
            Bacheca b = new Bacheca(t, "", u);
            inserisci(b);
        }
    }

    /** Mappa una riga del ResultSet in un oggetto {@link Bacheca}. */
    private Bacheca mapRow(ResultSet rs, Utente utente) throws SQLException {
        Bacheca b = new Bacheca();
        b.setId(rs.getInt("id"));
        b.setTitolo(TitoloBacheca.fromString(rs.getString("titolo")));
        b.setDescrizione(rs.getString("descrizione"));
        b.setUtente(utente);
        return b;
    }
}
