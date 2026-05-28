package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import proyecto.Equip;

public class EquipDAO {
    private final Connection conn;
    
    /**
     * inicializa la clase de gestion para la base de datos de equipo
     * @param conn	: la conexion a la base de datos
     */
    public EquipDAO(Connection conn) {
        this.conn = conn;
    }
    
    /**
     * inserta el equipo a la base de datos
     * @param equip	: el equipo a insertar
     */
    public void inserirEquip(Equip equip) {
        if (conn == null) return;

        String sql = "INSERT IGNORE INTO Equip (Nom) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equip.getNom());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error informatiu en inserir l'equip " + equip.getNom() + ": " + e.getMessage());
        }
    }
}