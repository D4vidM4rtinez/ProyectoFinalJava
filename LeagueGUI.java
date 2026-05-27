package projecte2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase partido
class Match {
    int numeroJornada;
    int any;
    String equipA;
    String equipB;
    Integer marcadorA;
    Integer marcadorB;

    public Match(int numeroJornada, int any, String equipA, String equipB) {
        this.numeroJornada = numeroJornada;
        this.any = any;
        this.equipA = equipA;
        this.equipB = equipB;
    }
}
//Dejo a vuestro criterio modificar este codigo que queda en su propia rama, he hecho pruebas y funciona sin modificaciones.
// Gestión de BBDD
class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/projecte2";
    private static final String USER = "damProg";
    private static final String PASS = "damProg";
    private Connection connection;

    public DatabaseManager() throws SQLException {
        connect();
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASS);
    }

    public void loadCSV(String filepath) throws Exception {
        String insertEquipSQL = "INSERT IGNORE INTO Equip (Nom) VALUES (?)";
        String insertJornadaSQL = "INSERT IGNORE INTO Jornada (Numero_jornada, Any, EquipA, EquipB) VALUES (?, ?, ?, ?)";

        try (BufferedReader br = new BufferedReader(new FileReader(filepath));
             PreparedStatement psEquip = connection.prepareStatement(insertEquipSQL);
             PreparedStatement psJornada = connection.prepareStatement(insertJornadaSQL)) {

            String line;
            boolean firstLine = true;
            // El CSV no da año, un campo de la BBDD del proyecto que no puede ser null
            int currentYear = java.time.Year.now().getValue();

            connection.setAutoCommit(false);

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Salta primera linea ya que el CSV original tiene: "Jornada;EquipA;EquipB". Importante modificar si se cambia la forma de leer CSV

                String[] data = line.split(";");
                if (data.length < 3) continue;

                int jornadaNum = Integer.parseInt(data[0].trim());
                String equipA = data[1].trim();
                String equipB = data[2].trim();

                // Insert Teams
                psEquip.setString(1, equipA);
                psEquip.executeUpdate();
                psEquip.setString(1, equipB);
                psEquip.executeUpdate();

                // Insert Matchday
                psJornada.setInt(1, jornadaNum);
                psJornada.setInt(2, currentYear);
                psJornada.setString(3, equipA);
                psJornada.setString(4, equipB);
                psJornada.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public List<Match> getFirstPendingMatchday() throws SQLException {
        List<Match> matches = new ArrayList<>();
        // Encuentra la primera jornada que no tiene resultados guardados via consulta
        String findMatchdaySQL = "SELECT Numero_jornada, Any FROM Jornada WHERE MarcadorA IS NULL OR MarcadorB IS NULL ORDER BY Any ASC, Numero_jornada ASC LIMIT 1";
        
        int pendingJornada = -1;
        int pendingAny = -1;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(findMatchdaySQL)) {
            if (rs.next()) {
                pendingJornada = rs.getInt("Numero_jornada");
                pendingAny = rs.getInt("Any");
            } else {
                return matches; // Empty list implies no pending matches
            }
        }

        // Consulta todos los partidos de una misma jornada
        String getMatchesSQL = "SELECT EquipA, EquipB FROM Jornada WHERE Numero_jornada = ? AND Any = ?";
        try (PreparedStatement ps = connection.prepareStatement(getMatchesSQL)) {
            ps.setInt(1, pendingJornada);
            ps.setInt(2, pendingAny);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(new Match(pendingJornada, pendingAny, rs.getString("EquipA"), rs.getString("EquipB")));
                }
            }
        }
        return matches;
    }
    //Guarda resultados a la BBDD
    public void saveMatchResults(List<Match> matches) throws SQLException {
        String updateSQL = "UPDATE Jornada SET MarcadorA = ?, MarcadorB = ? WHERE Numero_jornada = ? AND Any = ? AND EquipA = ? AND EquipB = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement ps = connection.prepareStatement(updateSQL)) {
            for (Match m : matches) {
                ps.setInt(1, m.marcadorA);
                ps.setInt(2, m.marcadorB);
                ps.setInt(3, m.numeroJornada);
                ps.setInt(4, m.any);
                ps.setString(5, m.equipA);
                ps.setString(6, m.equipB);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}

// Interfaz, la he probado y creo que es exactamente lo que queremos. 
public class LeagueGUI extends JFrame {
    private DatabaseManager dbManager;
    private JPanel mainPanel;
    private JPanel matchesPanel;
    private JLabel titleLabel;
    private JButton saveButton;
    private List<Match> currentMatches;
    private List<JTextField[]> scoreFields; // Stores references to input fields [FieldA, FieldB]

    public LeagueGUI() {
        setTitle("Administrador de Liga");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
        connectToDatabase();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Connecting to database...", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton loadCSVButton = new JButton("Cargar fichero .CSV");
        loadCSVButton.addActionListener(e -> handleCSVLoad());
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(loadCSVButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Matches Panel (Scrollable)
        matchesPanel = new JPanel();
        matchesPanel.setLayout(new BoxLayout(matchesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(matchesPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Save Button
        saveButton = new JButton("Guardar resultados");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveResults());
        mainPanel.add(saveButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void connectToDatabase() {
        try {
            dbManager = new DatabaseManager();
            loadNextMatchday();
        } catch (SQLException e) {
            showError("Error en la conexión de la base de datos: " + e.getMessage());
            titleLabel.setText("Error de conexión.");
        }
    }

    private void handleCSVLoad() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                dbManager.loadCSV(path);
                JOptionPane.showMessageDialog(this, "Fichero .CSV cargado a la base de datos con exito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadNextMatchday(); // Refresh view
            } catch (Exception ex) {
                showError("Carga del fichero CSV fallida:" + ex.getMessage());
            }
        }
    }

    private void loadNextMatchday() {
        try {
            matchesPanel.removeAll();
            currentMatches = dbManager.getFirstPendingMatchday();
            scoreFields = new ArrayList<>();

            if (currentMatches == null || currentMatches.isEmpty()) {
                titleLabel.setText("Sin partidos pendientes");
                saveButton.setEnabled(false);
            } else {
                Match first = currentMatches.get(0);
                titleLabel.setText("Jornada Pendiente: " + first.numeroJornada + " (Año: " + first.any +(first.any+1) + ")");
                
                for (Match match : currentMatches) {
                    JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
                    
                    JLabel teamALabel = new JLabel(match.equipA, SwingConstants.RIGHT);
                    teamALabel.setPreferredSize(new Dimension(100, 20));
                    
                    JTextField scoreAField = new JTextField(3);
                    JLabel vsLabel = new JLabel("-");
                    JTextField scoreBField = new JTextField(3);
                    
                    JLabel teamBLabel = new JLabel(match.equipB, SwingConstants.LEFT);
                    teamBLabel.setPreferredSize(new Dimension(100, 20));

                    rowPanel.add(teamALabel);
                    rowPanel.add(scoreAField);
                    rowPanel.add(vsLabel);
                    rowPanel.add(scoreBField);
                    rowPanel.add(teamBLabel);

                    matchesPanel.add(rowPanel);
                    scoreFields.add(new JTextField[]{scoreAField, scoreBField});
                }
                saveButton.setEnabled(true);
            }
            
            matchesPanel.revalidate();
            matchesPanel.repaint();

        } catch (SQLException e) {
            showError("Error cargando jornada: " + e.getMessage());
        }
    }

    private void saveResults() {
        try {
            // Validation & parsing loop
            for (int i = 0; i < currentMatches.size(); i++) {
                String strScoreA = scoreFields.get(i)[0].getText().trim();
                String strScoreB = scoreFields.get(i)[1].getText().trim();

                if (strScoreA.isEmpty() || strScoreB.isEmpty()) {
                    showError("Todos los campos han de estar llenos antes de guardar");
                    return;
                }

                int scoreA = Integer.parseInt(strScoreA);
                int scoreB = Integer.parseInt(strScoreB);

                if (scoreA < 0 || scoreB < 0) {
                    showError("Puntuaciones no pueden ser numeros negativos");
                    return;
                }

                currentMatches.get(i).marcadorA = scoreA;
                currentMatches.get(i).marcadorB = scoreB;
            }

            // Guardar a la BBDD
            dbManager.saveMatchResults(currentMatches);
            JOptionPane.showMessageDialog(this, "Resultados guardados con exito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Auto-Cargar siguiente jornada
            loadNextMatchday();

        } catch (NumberFormatException ex) {
            showError("Invalid input: Scores must be valid numeric values.");
        } catch (SQLException ex) {
            showError("Database error while saving results: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LeagueGUI().setVisible(true);
        });
    }
}
