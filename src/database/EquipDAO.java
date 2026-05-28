package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import proyecto.Equip;

public class EquipDAO {
	private String insert = "INSERT IGNORE INTO Equip (Nom) VALUES (?)";

	/**
	 * Insereix un equip a la base de dades si encara no existeix. Captura els
	 * errors per evitar que l'aplicació finalitzi de forma incorrecta.
	 */
	public EquipDAO() {
	}
	
	private String getInsert() {
		return insert;
	}
	/**
	 * funcio que automatitza la insercio de Equips a la taula
	 * @param equip	:	l'equip que vols inserir
	 */
	public void inserirEquip(Equip equip) {
		// Instanciem la teva connexió per defecte
		ConexionDB conexionDB = new ConexionDB();
		Connection conn = conexionDB.getDb();

		if (conn == null) {
			System.out.println("No es pot inserir l'equip: Error de connexió amb la BBDD.");
			return;
		}

		try (PreparedStatement ps = conn.prepareStatement(getInsert())) {
			ps.setString(1, equip.getNom());
			ps.executeUpdate();
		} catch (SQLException e) {
			// Mostrem missatge informatiu i evitar tancament incorrecte
			System.out.println("Error informatiu en inserir l'equip " + equip.getNom() + ": " + e.getMessage());
		} finally {
			// Tanquem la connexió de forma segura al final de l'operació
			try {
				if (conn != null && !conn.isClosed()) {
					conexionDB.tancar();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
