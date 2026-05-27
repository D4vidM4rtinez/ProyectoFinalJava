package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
	private Connection db;

	/**
	 * conexion Default para la base de datos
	 */
	public ConexionDB() {
		try {
			db = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/projecte2?serverTimezone=Europe/Madrid",
					"damProg", "damProg");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("ha habido un problema conectandose a la base de datos");
		}
	}

	/**
	 * conexion a la base de datos de manera mas customizada
	 * 
	 * @param port : el puerto donce este la base de datos (127.0.0.1:3306)
	 * @param user : el nombre del usuario con el que deseas conectarte
	 * @param pwd  : la contraseña del usuario con la que deseas conectarte
	 */
	public ConexionDB(String port, String user, String pwd) {
		try {
			db = DriverManager.getConnection("jdbc:mysql://" + port + "/projecte2?serverTimezone=Europe/Madrid", user,
					pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("ha habido un problema conectandose a la base de datos");
		}
	}

	public Connection getDb() {
		return this.db;
	}

	/**
	 * Mètode afegit per tancar la connexió de manera segura quan acabis de fer
	 * operacions
	 */
	public void tancar() {
		try {
			if (db != null && !db.isClosed()) {
				db.close();
				}
		} catch (SQLException e) {
			System.out.println("Error al intentar cerrar la conexión: " + e.getMessage());

		}
	}
}
