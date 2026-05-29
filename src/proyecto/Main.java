package proyecto;

import javax.swing.SwingUtilities;

public class Main {
	public static void main(String[] args) {
		System.out.println("Iniciant el projecte de Gestió Esportiva...");

		// 1. Inicialitza el controlador (conecta automàticament a la BBDD)
		GestorLliga app = new GestorLliga();

		// 2. Ruta del fitxer de dades
		String rutaCsv = "partits.csv";

		// 3. Executa la lògica de fitxers i persistència inicial
		app.carregarDadesCsv(rutaCsv);
		app.guardarDadesACorpusBBDD();

		// 4. Inicia la Interfície Gràfica passant el controlador (MVC)
		SwingUtilities.invokeLater(() -> {
			new LeagueGUI(app).setVisible(true);
		});

		// Registra un mecanisme de seguretat (Shutdown Hook) que s'executa
		// just abans de tancar la JVM per evitar connexions zombis a MySQL.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Tancant connexions de forma segura...");
			app.tancarConnexions();
		}));
	}
}