package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import proyecto.Partit;
import java.time.Year;

public class JornadaDAO {

	private int getYear() {
		return Year.now().getValue();
	}

	/**
	 * Insereix un partit inicial a la base de dades associat a una jornada. Si el
	 * partit ja existeix, fa un UPDATE (per si de cas).
	 */
	public void desarPartitInicial(int numJornada, Partit partit) {
		String sql = "INSERT INTO Jornada (Numero_jornada, Any, EquipA, EquipB, MarcadorA, MarcadorB) "
				+ "VALUES (?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE Numero_jornada = VALUES(Numero_jornada)";

		ConexionDB conexionDB = new ConexionDB();
		Connection conn = conexionDB.getDb();

		if (conn == null)
			return;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, numJornada);
			ps.setInt(2, getYear());
			ps.setString(3, partit.getEquipLocal().getNom());
			ps.setString(4, partit.getEquipVisitant().getNom());

			// Si el partit prové del CSV i encara no té gols (és pendent), guardem NULL a
			// la BBDD
			if (partit.getGolsLocal() == 0 && partit.getGolsVisitant() == 0) {
				ps.setNull(5, Types.INTEGER);
				ps.setNull(6, Types.INTEGER);
			} else {
				ps.setInt(5, partit.getGolsLocal());
				ps.setInt(6, partit.getGolsVisitant());
			}

			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error informatiu en desar el partit inicial: " + e.getMessage());
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * Troba el número de la PRIMERA jornada que té com a mínim un partit sense
	 * resultats (MarcadorA IS NULL).
	 * 
	 * @return El número de la jornada, o -1 si ja s'han jugat totes.
	 */
	public int obtenirPrimeraJornadaPendent() {
		String sql = "SELECT MIN(Numero_jornada) AS primera_pendent FROM Jornada WHERE MarcadorA IS NULL AND Any = ?";

		ConexionDB conexionDB = new ConexionDB();
		Connection conn = conexionDB.getDb();
		int jornadaPendent = -1;

		if (conn == null)
			return -1;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, getYear());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					jornadaPendent = rs.getInt("primera_pendent");
					if (rs.wasNull()) {
						jornadaPendent = -1; // Vol dir que el camp era NULL perquè no n'hi ha cap
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error informatiu en consultar la jornada pendent: " + e.getMessage());
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
			}
		}
		return jornadaPendent;
	}

	/**
	 * Actualitza els gols d'un partit concret introduïts des de la Interfície
	 * Gràfica (GUI).
	 */
	public void actualitzarResultatPartit(int numJornada, String equipA, String equipB, int golsA, int golsB) {
		String sql = "UPDATE Jornada SET MarcadorA = ?, MarcadorB = ? "
				+ "WHERE Numero_jornada = ? AND Any = ? AND EquipA = ? AND EquipB = ?";

		ConexionDB conexionDB = new ConexionDB();
		Connection conn = conexionDB.getDb();

		if (conn == null)
			return;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, golsA);
			ps.setInt(2, golsB);
			ps.setInt(3, numJornada);
			ps.setInt(4, getYear());
			ps.setString(5, equipA);
			ps.setString(6, equipB);

			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error informatiu en actualitzar el resultat a la BBDD: " + e.getMessage());
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
			}
		}
	}
}