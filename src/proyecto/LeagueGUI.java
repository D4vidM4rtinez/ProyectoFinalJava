package proyecto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LeagueGUI extends JFrame {
    
    private final GestorLliga controlador;
    
    private JPanel mainPanel;
    private JPanel matchesPanel;
    private JLabel titleLabel;
    private JButton saveButton;
    
    private int jornadaActual;
    private List<Partit> currentMatches;
    private List<JTextField[]> scoreFields;

    public LeagueGUI(GestorLliga controlador) {
        this.controlador = controlador;
        
        setTitle("Administrador de Liga");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
        loadNextMatchday();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Carregant jornada...", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Scrollable)
        matchesPanel = new JPanel();
        matchesPanel.setLayout(new BoxLayout(matchesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(matchesPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Button
        saveButton = new JButton("Guardar resultados");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveResults());
        mainPanel.add(saveButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadNextMatchday() {
        matchesPanel.removeAll();
        scoreFields = new ArrayList<>();
        currentMatches = new ArrayList<>();

        // 1. Preguntem quina és la jornada pendent actual
        jornadaActual = controlador.obtenirJornadaActualGUI();

        if (jornadaActual == -1) {
            titleLabel.setText("Sin jornadas pendientes");
            saveButton.setEnabled(false);
        } else {
            titleLabel.setText("Jornada Pendiente: " + jornadaActual);
            
            // demanem els partits directament a la Base de Dades:
            currentMatches = controlador.obtenirPartitsDeLaBBDD(jornadaActual);

            // Generem la interfície gràfica dinàmicament amb les dades de MySQL
            for (Partit match : currentMatches) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
                
                JLabel teamALabel = new JLabel(match.getEquipLocal().getNom(), SwingConstants.RIGHT);
                teamALabel.setPreferredSize(new Dimension(120, 20));
                
                // Si el partit ja té gols a la BBDD, els posem; si és NULL, es mostra buit
                String textGolsA = (match.getGolsLocal() != null) ? match.getGolsLocal().toString() : "";
                String textGolsB = (match.getGolsVisitant() != null) ? match.getGolsVisitant().toString() : "";
                
                JTextField scoreAField = new JTextField(textGolsA, 3);
                JLabel vsLabel = new JLabel("-");
                JTextField scoreBField = new JTextField(textGolsB, 3);
                
                JLabel teamBLabel = new JLabel(match.getEquipVisitant().getNom(), SwingConstants.LEFT);
                teamBLabel.setPreferredSize(new Dimension(120, 20));

                rowPanel.add(teamALabel);
                rowPanel.add(scoreAField);
                rowPanel.add(vsLabel);
                rowPanel.add(scoreBField);
                rowPanel.add(teamBLabel);

                matchesPanel.add(rowPanel);
                scoreFields.add(new JTextField[]{scoreAField, scoreBField});
            }
            saveButton.setEnabled(!currentMatches.isEmpty());
        }
        
        matchesPanel.revalidate();
        matchesPanel.repaint();
    }
    
    private void saveResults() {
        try {
            // Requeriment g: Validacions de dades de seguretat
            for (int i = 0; i < currentMatches.size(); i++) {
                String strA = scoreFields.get(i)[0].getText().trim();
                String strB = scoreFields.get(i)[1].getText().trim();

                // g.i: Camps buits
                if (strA.isEmpty() || strB.isEmpty()) {
                    showError("No es poden desar resultats si hi ha camps buits.");
                    return;
                }

                // g.ii: Valors no numèrics
                int scoreA = Integer.parseInt(strA);
                int scoreB = Integer.parseInt(strB);

                // g.iii: Dades incorrectes (negatius)
                if (scoreA < 0 || scoreB < 0) {
                    showError("Els gols no poden ser valors negatius.");
                    return;
                }
            }

            // Requeriment h: Guardar a la BBDD si tot està bé
            for (int i = 0; i < currentMatches.size(); i++) {
                Partit match = currentMatches.get(i);
                int golsL = Integer.parseInt(scoreFields.get(i)[0].getText().trim());
                int golsV = Integer.parseInt(scoreFields.get(i)[1].getText().trim());

                controlador.guardarResultatDesDeGUI(jornadaActual, match.getEquipLocal().getNom(), match.getEquipVisitant().getNom(), golsL, golsV);
            }

            JOptionPane.showMessageDialog(this, "Resultats desats amb èxit!", "Èxit", JOptionPane.INFORMATION_MESSAGE);
            
            // Requeriment h.ii: Càrrega automàtica de la següent jornada pendent
            loadNextMatchday();

        } catch (NumberFormatException ex) {
            showError("Els valors introduïts han de ser numèrics."); // Requeriment g.ii
        } catch (Exception ex) {
            showError("Error al desar: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error de Validació", JOptionPane.ERROR_MESSAGE);
    }
}