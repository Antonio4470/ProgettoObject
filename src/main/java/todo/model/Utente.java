package todo.model;

import java.util.Objects;

/**
 * Rappresenta un utente del sistema.
 * La coppia (login, password) deve essere unica nel sistema.
 */
public class Utente {

    /** Identificatore univoco dell'utente, usato come PK nel DB. */
    private String login;

    /** Password (in produzione memorizzata come hash BCrypt). */
    private String password;

    // ── Costruttori ──────────────────────────────────────────

    public Utente() {}

    /**
     * @param login    identificatore univoco
     * @param password credenziale di accesso
     */
    public Utente(String login, String password) {
        this.login    = login;
        this.password = password;
    }

    // ── Getter / Setter ──────────────────────────────────────

    /** @return il login dell'utente */
    public String getLogin()    { return login; }

    /** @param login il login da impostare */
    public void setLogin(String login)       { this.login = login; }

    /** @return la password dell'utente */
    public String getPassword() { return password; }

    /** @param password la password da impostare */
    public void setPassword(String password) { this.password = password; }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) 
        	return true;
        if (!(o instanceof Utente)) 
        	return false;
        Utente u = (Utente)o;
        return Objects.equals(login, u.login);
    }

    @Override
    public int hashCode() { return Objects.hash(login); }

    @Override
    public String toString() {
        return "Utente{login='" + login + "'}";
    }
}
