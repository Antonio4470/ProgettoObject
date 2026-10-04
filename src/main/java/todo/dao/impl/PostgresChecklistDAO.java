package todo.dao.impl;

import todo.dao.ChecklistDAO;
import todo.database.DBConnection;
import todo.model.*;
import java.sql.*;
import java.util.Optional;

/**
 * Implementazione PostgreSQL di {@link ChecklistDAO}.
 */
public class PostgresChecklistDAO implements ChecklistDAO {

    @Override
    public void creaChecklist(Checklist checklist, int idTodo) throws SQLException {
        String sql = "INSERT INTO checklist (id_todo) VALUES (?) RETURNING id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTodo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) checklist.setId(rs.getInt("id"));
            }
        }
    }

    @Override
    public void eliminaChecklist(int idTodo) throws SQLException {
        String sql = "DELETE FROM checklist WHERE id_todo = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTodo);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Checklist> trovaDaToDo(int idTodo) throws SQLException {
        String sql = "SELECT id FROM checklist WHERE id_todo = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTodo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Checklist cl = new Checklist();
                cl.setId(rs.getInt("id"));

                // Carica le attività
                String sqlAtt = "SELECT id, nome, stato FROM attivita "+
                    "WHERE id_checklist = ? ORDER BY id";
                try (PreparedStatement ps2 =
                             DBConnection.getConnection().prepareStatement(sqlAtt)) {
                    ps2.setInt(1, cl.getId());
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            Attivita a = new Attivita();
                            a.setId(rs2.getInt("id"));
                            a.setNome(rs2.getString("nome"));
                            a.setStato(StatoAttivita.fromString(rs2.getString("stato")));
                            cl.aggiungiAttivita(a);
                        }
                    }
                }
                return Optional.of(cl);
            }
        }
    }

    @Override
    public void aggiungiAttivita(Attivita attivita, int idChecklist) throws SQLException {
    	String sql =
    	        "INSERT INTO attivita (nome, stato, id_checklist) " +
    	        "VALUES (?, ?::stato_attivita, ?) " +
    	        "RETURNING id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, attivita.getNome());
            ps.setString(2, attivita.getStato().toString());
            ps.setInt   (3, idChecklist);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) attivita.setId(rs.getInt("id"));
            }
        }
    }

    @Override
    public void visualizzaAttivita(int idAttivita) throws SQLException {
        String sql = "SELECT id, nome, stato FROM attivita WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idAttivita);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Attività con id " + idAttivita + " non trovata.");
                    return;
                }
                String nome  = rs.getString("nome");
                String stato = rs.getString("stato");
                System.out.println("Attività #" + idAttivita
                        + " - Nome: " + nome
                        + " - Stato: " + stato);
            }
        }
    }
    @Override
    public void aggiornaAttivita(Attivita attivita) throws SQLException {
        String sql = "UPDATE attivita SET nome = ?, stato = ?::stato_attivita WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, attivita.getNome());
            ps.setString(2, attivita.getStato().toString());
            ps.setInt   (3, attivita.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminaAttivita(int idAttivita) throws SQLException {
        String sql = "DELETE FROM attivita WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idAttivita);
            ps.executeUpdate();
        }
    }
}
