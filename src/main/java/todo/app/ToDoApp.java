package todo.app;

import todo.controller.ToDoController;
import todo.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;


/**
 * GUI principale dell'applicazione ToDo.
 * Usa un CardLayout per navigare tra le schermate:
 *   CARD_LOGIN  → form di login/registrazione
 *   CARD_HOME   → lista bacheche + lista ToDo
 *   CARD_TODO   → dettaglio di un ToDo (checklist, condivisioni)
 */
public class ToDoApp extends JFrame {

    // ── Controller (unico punto di accesso al backend) ────────────────
    private final ToDoController ctrl = new ToDoController();

    // ── Navigazione ───────────────────────────────────────────────────
    private final CardLayout   cardLayout = new CardLayout();
    private final JPanel       cardPanel  = new JPanel(cardLayout);

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_HOME  = "HOME";
    private static final String CARD_TODO  = "TODO";

    // ── Stato sessione ────────────────────────────────────────────────
    private Bacheca  bachecaSelezionata;
    private ToDo     todoSelezionato;

    // ══════════════════════════════════════════════════════════════════
    // COSTRUTTORE
    // ══════════════════════════════════════════════════════════════════
    public ToDoApp() {
        setTitle("ToDo Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        // Aggiunge le tre card al pannello principale
        cardPanel.add(buildLoginCard(), CARD_LOGIN);
        cardPanel.add(buildHomeCard(),  CARD_HOME);
        cardPanel.add(buildTodoCard(),  CARD_TODO);

        add(cardPanel);
        cardLayout.show(cardPanel, CARD_LOGIN); // parte dalla schermata di login
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    // CARD 1 — LOGIN / REGISTRAZIONE
    // ══════════════════════════════════════════════════════════════════

    // ── Componenti login ──
    private JTextField  loginField;
    private JPasswordField passwordField;
    private JLabel      loginMessaggio;

    private JPanel buildLoginCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(240, 244, 248));

        JPanel box = new JPanel(new GridLayout(5, 1, 6, 6));
        box.setPreferredSize(new Dimension(420, 280));
        box.setBorder(new EmptyBorder(20, 30, 20, 30));
        box.setBackground(Color.WHITE);

        loginField    = new JTextField();
        passwordField = new JPasswordField();
        loginMessaggio = new JLabel(" ");
        loginMessaggio.setForeground(Color.RED);
        loginMessaggio.setFont(new Font("Arial", Font.ITALIC, 11));

        JButton btnLogin     = new JButton("Accedi");
        JButton btnRegistra  = new JButton("Registrati");

        btnLogin.addActionListener(e -> eseguiLogin());
        btnRegistra.addActionListener(e -> eseguiRegistrazione());

        box.add(labeledField("Login",    loginField));
        box.add(labeledField("Password", passwordField));
        box.add(loginMessaggio);
        box.add(btnLogin);
        box.add(btnRegistra);

        card.add(box);
        return card;
    }

    private void eseguiLogin() {
        try {
            String login = loginField.getText().trim();
            String pwd   = new String(passwordField.getPassword());
            if (ctrl.login(login, pwd)) {
                loginMessaggio.setText(" ");
                aggiornaHome();
                cardLayout.show(cardPanel, CARD_HOME);
            } else {
                loginMessaggio.setText("Credenziali non valide.");
            }
        } catch (SQLException ex) {
            mostraErrore(ex);
        }
    }

    private void eseguiRegistrazione() {
        try {
            String login = loginField.getText().trim();
            String pwd   = new String(passwordField.getPassword());
            if (login.isEmpty() || pwd.isEmpty()) {
                loginMessaggio.setText("Login e password obbligatori.");
                return;
            }
            ctrl.registra(login, pwd);
            loginMessaggio.setForeground(new Color(0, 120, 0));
            loginMessaggio.setText("Registrazione effettuata. Ora accedi.");
        } catch (SQLException ex) {
            loginMessaggio.setForeground(Color.RED);
            loginMessaggio.setText("Login già esistente.");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // CARD 2 — HOME (bacheche + ToDo)
    // ══════════════════════════════════════════════════════════════════

    // ── Componenti home ──
    private JLabel          homeUtente;
    private JList<Bacheca>  listaBacheche;
    private DefaultListModel<Bacheca> modelBacheche = new DefaultListModel<>();
    private JList<ToDo>     listaTodo;
    private DefaultListModel<ToDo>    modelTodo     = new DefaultListModel<>();
    private JTextField      campoNuovoTodo;
    private JTextField      campoCerca;

    private JPanel buildHomeCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(245, 247, 250));

        // ── Barra superiore ──
        JPanel barraTop = new JPanel(new BorderLayout());
        barraTop.setBackground(new Color(30, 80, 160));
        homeUtente = new JLabel("  Benvenuto");
        homeUtente.setForeground(Color.WHITE);
        homeUtente.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(200, 50, 50));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            ctrl.logout();
            cardLayout.show(cardPanel, CARD_LOGIN);
        });

        campoCerca = new JTextField(15);
        JButton btnCerca = new JButton("Cerca");
        btnCerca.addActionListener(e -> eseguiRicerca());

        JPanel cercaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        cercaPanel.setBackground(new Color(30, 80, 160));
        cercaPanel.add(campoCerca);
        cercaPanel.add(btnCerca);
        cercaPanel.add(btnLogout);

        barraTop.add(homeUtente,  BorderLayout.WEST);
        barraTop.add(cercaPanel,  BorderLayout.EAST);

        // ── Pannello bacheche (sinistra) ──
        listaBacheche = new JList<>(modelBacheche);
        listaBacheche.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaBacheche.setCellRenderer(new BachecaRenderer());
        listaBacheche.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) aggiornaTodo();
        });

        JScrollPane scrollBacheche = new JScrollPane(listaBacheche);
        scrollBacheche.setPreferredSize(new Dimension(180, 0));
        scrollBacheche.setBorder(BorderFactory.createTitledBorder("Bacheche"));

        // ── Pannello ToDo (centro) ──
        listaTodo = new JList<>(modelTodo);
        listaTodo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaTodo.setCellRenderer(new TodoRenderer());
        listaTodo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) apriDettaglioTodo();
            }
        });

        JScrollPane scrollTodo = new JScrollPane(listaTodo);
        scrollTodo.setBorder(BorderFactory.createTitledBorder("ToDo"));

        // ── Barra inferiore: nuovo ToDo ──
        campoNuovoTodo = new JTextField();
        JButton btnAggiungi = new JButton("+ Aggiungi ToDo");
        btnAggiungi.setBackground(new Color(40, 160, 80));
        btnAggiungi.setForeground(Color.WHITE);
        btnAggiungi.setFocusPainted(false);
        btnAggiungi.addActionListener(e -> aggiungiTodo());

        JButton btnElimina = new JButton("Elimina");
        btnElimina.setBackground(new Color(200, 50, 50));
        btnElimina.setForeground(Color.WHITE);
        btnElimina.setFocusPainted(false);
        btnElimina.addActionListener(e -> eliminaTodo());

        JButton btnScadenzaOggi = new JButton("In scadenza oggi");
        btnScadenzaOggi.addActionListener(e -> mostraScadenzaOggi());

        JPanel barraBottom = new JPanel(new BorderLayout(5, 0));
        barraBottom.add(campoNuovoTodo, BorderLayout.CENTER);

        JPanel bottoniBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        bottoniBottom.add(btnScadenzaOggi);
        bottoniBottom.add(btnElimina);
        bottoniBottom.add(btnAggiungi);
        barraBottom.add(bottoniBottom, BorderLayout.EAST);

        card.add(barraTop,        BorderLayout.NORTH);
        card.add(scrollBacheche,  BorderLayout.WEST);
        card.add(scrollTodo,      BorderLayout.CENTER);
        card.add(barraBottom,     BorderLayout.SOUTH);

        return card;
    }

    private void aggiornaHome() {
        homeUtente.setText("  Benvenuto, " + ctrl.getUtenteCorrente().getLogin());
        modelBacheche.clear();
        try {
            for (Bacheca b : ctrl.getBacheche()) modelBacheche.addElement(b);
            if (!modelBacheche.isEmpty()) {
                listaBacheche.setSelectedIndex(0);
            }
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void aggiornaTodo() {
        bachecaSelezionata = listaBacheche.getSelectedValue();
        if (bachecaSelezionata == null) return;
        modelTodo.clear();
        try {
            for (ToDo t : ctrl.getToDoDiBacheca(bachecaSelezionata.getId()))
                modelTodo.addElement(t);
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void aggiungiTodo() {
        String titolo = campoNuovoTodo.getText().trim();
        if (titolo.isEmpty() || bachecaSelezionata == null) return;
        try {
            ctrl.creaToDo(titolo, bachecaSelezionata.getId());
            campoNuovoTodo.setText("");
            aggiornaTodo();
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void eliminaTodo() {
        ToDo sel = listaTodo.getSelectedValue();
        if (sel == null) return;
        int conferma = JOptionPane.showConfirmDialog(this,
                "Eliminare \"" + sel.getTitolo() + "\"?",
                "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
        if (conferma == JOptionPane.YES_OPTION) {
            try {
                ctrl.eliminaToDo(sel.getId());
                aggiornaTodo();
            } catch (SQLException ex) { mostraErrore(ex); }
        }
    }

    private void mostraScadenzaOggi() {
        try {
            List<ToDo> lista = ctrl.getToDoInScadenzaOggi();
            modelTodo.clear();
            for (ToDo t : lista) modelTodo.addElement(t);
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void eseguiRicerca() {
        String testo = campoCerca.getText().trim();
        if (testo.isEmpty()) { aggiornaTodo(); return; }
        try {
            modelTodo.clear();
            for (ToDo t : ctrl.cercaToDo(testo)) modelTodo.addElement(t);
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void apriDettaglioTodo() {
        todoSelezionato = listaTodo.getSelectedValue();
        if (todoSelezionato == null) return;
        aggiornaTodoCard();
        cardLayout.show(cardPanel, CARD_TODO);
    }

    // ══════════════════════════════════════════════════════════════════
    // CARD 3 — DETTAGLIO TODO (checklist + condivisioni)
    // ══════════════════════════════════════════════════════════════════

    // ── Componenti dettaglio ──
    private JLabel          todoTitoloLabel;
    private JList<Attivita> listaAttivita;
    private DefaultListModel<Attivita> modelAttivita = new DefaultListModel<>();
    private JTextField      campoNuovaAttivita;
    private JTextField      campoCondivisione;
    private JLabel          todoStatoLabel;

    private JPanel buildTodoCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Barra superiore ──
        JButton btnIndietro = new JButton("← Indietro");
        btnIndietro.addActionListener(e -> {
            aggiornaTodo();
            cardLayout.show(cardPanel, CARD_HOME);
        });
        todoTitoloLabel = new JLabel("ToDo");
        todoTitoloLabel.setFont(new Font("Arial", Font.BOLD, 16));
        todoStatoLabel  = new JLabel();
        todoStatoLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JPanel barraTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        barraTop.setBackground(new Color(30, 80, 160));
        btnIndietro.setBackground(new Color(30, 80, 160));
        btnIndietro.setForeground(Color.WHITE);
        btnIndietro.setFocusPainted(false);
        todoTitoloLabel.setForeground(Color.WHITE);
        todoStatoLabel.setForeground(new Color(200, 230, 255));
        barraTop.add(btnIndietro);
        barraTop.add(todoTitoloLabel);
        barraTop.add(todoStatoLabel);

        // ── Pannello checklist (sinistra) ──
        listaAttivita = new JList<>(modelAttivita);
        listaAttivita.setCellRenderer(new AttivitaRenderer());

        JScrollPane scrollAtt = new JScrollPane(listaAttivita);
        scrollAtt.setBorder(BorderFactory.createTitledBorder("Checklist"));

        campoNuovaAttivita = new JTextField();
        JButton btnAddAtt = new JButton("+ Attività");
        btnAddAtt.addActionListener(e -> aggiungiAttivita());

        JButton btnCompletaAtt = new JButton("✓ Completa");
        btnCompletaAtt.setBackground(new Color(40, 160, 80));
        btnCompletaAtt.setForeground(Color.WHITE);
        btnCompletaAtt.setFocusPainted(false);
        btnCompletaAtt.addActionListener(e -> completaAttivita());

        JButton btnEliminaAtt = new JButton("Elimina");
        btnEliminaAtt.setBackground(new Color(200, 50, 50));
        btnEliminaAtt.setForeground(Color.WHITE);
        btnEliminaAtt.setFocusPainted(false);
        btnEliminaAtt.addActionListener(e -> eliminaAttivita());

        JPanel bottomAtt = new JPanel(new BorderLayout(5, 0));
        JPanel bottoniBott = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        bottoniBott.add(btnEliminaAtt);
        bottoniBott.add(btnCompletaAtt);
        bottoniBott.add(btnAddAtt);
        bottomAtt.add(campoNuovaAttivita, BorderLayout.CENTER);
        bottomAtt.add(bottoniBott, BorderLayout.EAST);

        JPanel checklistPanel = new JPanel(new BorderLayout(5, 5));
        checklistPanel.add(scrollAtt,  BorderLayout.CENTER);
        checklistPanel.add(bottomAtt,  BorderLayout.SOUTH);

        // ── Pannello condivisioni (destra) ──
        campoCondivisione = new JTextField(12);
        JButton btnCondividi  = new JButton("Condividi");
        JButton btnRimuoviCon = new JButton("Rimuovi");
        btnCondividi.addActionListener(e -> aggiungiCondivisione());
        btnRimuoviCon.addActionListener(e -> rimuoviCondivisione());

        JPanel conPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        conPanel.setBorder(BorderFactory.createTitledBorder("Condivisioni"));
        conPanel.setPreferredSize(new Dimension(200, 0));
        conPanel.add(new JLabel("Login utente:"));
        conPanel.add(campoCondivisione);
        conPanel.add(btnCondividi);
        conPanel.add(btnRimuoviCon);

        card.add(barraTop,       BorderLayout.NORTH);
        card.add(checklistPanel, BorderLayout.CENTER);
        card.add(conPanel,       BorderLayout.EAST);

        return card;
    }

    private void aggiornaTodoCard() {
        todoTitoloLabel.setText(todoSelezionato.getTitolo());
        todoStatoLabel.setText("[" + todoSelezionato.getStato() + " Scadenza " + todoSelezionato.getDataScadenza() + "]");
        modelAttivita.clear();
        try {
            // Ricarica sempre la checklist dal database, per essere sicuri
            // di avere le attività aggiornate anche dopo essere tornati
            // dalla bacheca (l'oggetto in memoria potrebbe essere "vecchio").
            ctrl.getChecklist(todoSelezionato);
        } catch (SQLException ex) {
            mostraErrore(ex);
        }
        if (todoSelezionato.getChecklist() != null) {
            for (Attivita a : todoSelezionato.getChecklist().getAttivita())
                modelAttivita.addElement(a);
        }
    }

    private void aggiungiAttivita() {
        String nome = campoNuovaAttivita.getText().trim();
        if (nome.isEmpty()) return;
        try {
            // Crea la checklist se non esiste ancora
            if (todoSelezionato.getChecklist() == null)
                ctrl.creaChecklist(todoSelezionato);
            ctrl.aggiungiAttivita(todoSelezionato, nome);
            campoNuovaAttivita.setText("");
            aggiornaTodoCard();
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    /** Mostra i dettagli dell'attività selezionata in un dialog. */
    private void visualizzaAttivita() {
        Attivita sel = listaAttivita.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona prima un'attività dalla lista.",
                    "Nessuna selezione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String dettaglio = "Nome: " + sel.getNome()
                + "\nStato: " + (sel.isCompletata() ? "Completata" : "Da completare");
        JOptionPane.showMessageDialog(this, dettaglio,
                "Dettaglio attività", JOptionPane.INFORMATION_MESSAGE);
    }

    private void completaAttivita() {
        Attivita sel = listaAttivita.getSelectedValue();
        if (sel == null) return;
        try {
            ctrl.completaAttivita(todoSelezionato, sel);
            // Aggiorna l'etichetta stato (potrebbe essere diventato Completato)
            todoStatoLabel.setText("[" + todoSelezionato.getStato() + "]");
            aggiornaTodoCard();
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void eliminaAttivita() {
        Attivita sel = listaAttivita.getSelectedValue();
        if (sel == null) return;
        try {
            ctrl.eliminaAttivita(todoSelezionato, sel);
            aggiornaTodoCard();
        } catch (SQLException ex) { mostraErrore(ex); }
    }

    private void aggiungiCondivisione() {
        String login = campoCondivisione.getText().trim();
        if (login.isEmpty()) return;
        try {
            ctrl.aggiungiCondivisione(todoSelezionato, login);
            campoCondivisione.setText("");
            JOptionPane.showMessageDialog(this, "Condivisione aggiunta.");
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rimuoviCondivisione() {
        String login = campoCondivisione.getText().trim();
        if (login.isEmpty()) return;
        try {
            ctrl.rimuoviCondivisione(todoSelezionato, login);
            campoCondivisione.setText("");
            JOptionPane.showMessageDialog(this, "Condivisione rimossa.");
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // RENDERER personalizzati per JList
    // ══════════════════════════════════════════════════════════════════

    /** Mostra il nome della bacheca nella JList. */
    static class BachecaRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> l, Object v,
                int i, boolean sel, boolean foc) {
            super.getListCellRendererComponent(l, v, i, sel, foc);
            if (v instanceof Bacheca)
                setText(((Bacheca) v).getTitolo().name());
            return this;
        }
    }

    /** Mostra titolo e stato del ToDo con colore in base allo stato. */
    static class TodoRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> l, Object v,
                int i, boolean sel, boolean foc) {
            super.getListCellRendererComponent(l, v, i, sel, foc);
            if (v instanceof ToDo) {
                setText(((ToDo) v).getTitolo() + "  [" + ((ToDo) v).getStato().name() + "]");
                if (!sel) {
                    setForeground(((ToDo) v).getStato() == StatoToDo.Completato
                            ? new Color(0, 130, 0) : Color.BLACK);
                }
            }
            return this;
        }
    }

    /** Mostra il nome dell'attività con ✓ se completata. */
    static class AttivitaRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> l, Object v,
                int i, boolean sel, boolean foc) {
            super.getListCellRendererComponent(l, v, i, sel, foc);
            if (v instanceof Attivita) {
                setText((((Attivita) v).isCompletata() ? "✓  " : "○  ") + ((Attivita) v).getNome());
                if (!sel)
                    setForeground(((Attivita) v).isCompletata()
                            ? new Color(0, 130, 0) : Color.BLACK);
            }
            return this;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // UTILITÀ
    // ══════════════════════════════════════════════════════════════════

    /** Crea un pannello etichetta + campo in verticale. */
    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(3, 2));
        p.setOpaque(false);
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field,             BorderLayout.CENTER);
        return p;
    }

    /** Mostra un dialog di errore con il messaggio dell'eccezione. */
    private void mostraErrore(Exception ex) {
        JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Errore SQL", JOptionPane.ERROR_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoApp::new);
    }
}
