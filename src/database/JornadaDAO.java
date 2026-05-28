package database;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import proyecto.Equip;
import proyecto.Partit;

public class JornadaDAO {
    private final Connection conn;

    /**
     * inicializa la clase de gestion para la base de datos, gestiona Jornada
     * @param conn	: la conexion a la base de datos
     */
    public JornadaDAO(Connection conn) {
        this.conn = conn;
    }
    
    /**
     * metodo para conseguir el año actual de manera dinamica
     * @return	:	el año actual
     */
    private int getYear() {
        return Year.now().getValue();
    }

    /**
     * guarda en la base de datos las jornadas
     * @param numJornada	:	el numero de la jornada a guardar
     * @param partit	:	el partido que se tiene que guardar en la jornada
     */
    public void desarPartitInicial(int numJornada, Partit partit) {
        if (conn == null) return;

        String sql = "INSERT INTO Jornada (Numero_jornada, Any, EquipA, EquipB, MarcadorA, MarcadorB) "
                   + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Numero_jornada = VALUES(Numero_jornada)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numJornada);
            ps.setInt(2, getYear());
            ps.setString(3, partit.getEquipLocal().getNom());
            ps.setString(4, partit.getEquipVisitant().getNom());

            // Si el partido esta pendiente (0 o null) se guarda como NULL a la BBDD
            if (partit.getGolsLocal() == null || (partit.getGolsLocal() == 0 && partit.getGolsVisitant() == 0)) {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(5, partit.getGolsLocal());
                ps.setInt(6, partit.getGolsVisitant());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error informatiu en desar el partit inicial: " + e.getMessage());
        }
    }

    /**
     * Query para conseguir la primera jornada pendent
     * @return -1 en caso de que no haya jornadas, la jornada en caso de que haya una
     */
    public int obtenirPrimeraJornadaPendent() {
        if (conn == null) return -1;

        String sql = "SELECT MIN(Numero_jornada) AS primera_pendent FROM Jornada WHERE MarcadorA IS NULL AND Any = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, getYear());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int j = rs.getInt("primera_pendent");
                    return rs.wasNull() ? -1 : j;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error informatiu en consultar la jornada pendent: " + e.getMessage());
        }
        return -1;
    }
    public List<Partit> obtenirPartitsBBDDPerJornada(int numJornada) {
        List<Partit> partitsBBDD = new ArrayList<>();
        if (conn == null) return partitsBBDD;

        String sql = "SELECT EquipA, EquipB, MarcadorA, MarcadorB FROM Jornada WHERE Numero_jornada = ? AND Any = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numJornada);
            ps.setInt(2, getYear()); // El mètode getYear() que ja tenies creat
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nomLocal = rs.getString("EquipA");
                    String nomVisitant = rs.getString("EquipB");
                    
                    // UsemgetObject per si els marcadors són NULL a la base de dades
                    Integer golsLocal = (Integer) rs.getObject("MarcadorA");
                    Integer golsVisitant = (Integer) rs.getObject("MarcadorB");
                    
                    // Creem les entitats de model
                    Equip local = new Equip(nomLocal);
                    Equip visitant = new Equip(nomVisitant);
                    
                    Partit partit = new Partit(local, visitant, golsLocal, golsVisitant);
                    partit.setNumeroJornada(numJornada);
                    
                    // Els guardem a la llista
                    partitsBBDD.add(partit);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en carregar partits des de la BBDD: " + e.getMessage());
        }
        
        return partitsBBDD;
    }

    public void actualitzarResultatPartit(int numJornada, String equipA, String equipB, int golsA, int golsB) {
        if (conn == null) return;

        String sql = "UPDATE Jornada SET MarcadorA = ?, MarcadorB = ? "
                   + "WHERE Numero_jornada = ? AND Any = ? AND EquipA = ? AND EquipB = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, golsA);
            ps.setInt(2, golsB);
            ps.setInt(3, numJornada);
            ps.setInt(4, getYear());
            ps.setString(5, equipA);
            ps.setString(6, equipB);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error informatiu en actualitzar el resultat: " + e.getMessage());
        }
    }
}