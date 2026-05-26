public class Main {
    public static void main(String[] args) {
        // 1. Instanciamos el gestor de la liga
        GestorLliga app = new GestorLliga();

        // 2. Definimos la ruta del archivo CSV a probar
        String rutaCsv = "partits.csv";

        System.out.println("Iniciant el projecte...");

        // 3. Cargamos los datos
        app.carregarDadesCsv(rutaCsv);

        // 4. Mostramos el resultado por consola (Simulando la futura GUI)
        System.out.println("\n--- EQUIPS DETECTATS ---");
        app.getEquipsPerAGUI().forEach(equip -> System.out.println("- " + equip));

        System.out.println("\n--- JORNADES I PARTITS PROCESSATS ---");
        for (Jornada j : app.getJornadesPerAGUI()) {
            System.out.println("\n" + j);
            for (Partit p : j.getPartits()) {
                System.out.println("  -> " + p);
            }
        }
    }
}