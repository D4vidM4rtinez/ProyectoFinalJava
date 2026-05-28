package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private Connection db;

    /**
     * metodo para construir la classe, conecta automaticamente a la BBDD en el localhost:3306
     */
    public ConexionDB() {
        try {
            db = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/projecte2?serverTimezone=Europe/Madrid",
                    "damProg", "damProg");
        } catch (SQLException e) {
            System.err.println("Error informatiu de connexió amb la BBDD: " + e.getMessage());
        }
    }
    
    /**
     * devuelve la conexion a la base de datos
     * @return la conexion
     */
    public Connection getDb() {
        return this.db;
    }
    
    /**
     * cierra la conexion a la base de datos solo si no se ha cerrado ya o no existe
     */
    public void tancar() {
        try {
            if (db != null && !db.isClosed()) {
                db.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al tancar la connexió: " + e.getMessage());
        }
    }
}