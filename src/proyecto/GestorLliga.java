
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GestorLliga {

    private final Map<Integer, Jornada> jornadesMap = new TreeMap<>();
    private final Set<Equip> equipsUnics = new HashSet<>();

    /**
     * Carga el archivo CSV adaptándose tanto si tiene 3 columnas (solo partidos)
     * como si ya tiene 5 columnas (con goles incorporados).
     */
    public void carregarDadesCsv(String rutaFitxer) {
        File fitxer = new File(rutaFitxer);

        if (!fitxer.exists()) {
            System.out.println(" El fitxer '" + rutaFitxer + "' no existeix. Creant un fitxer nou amb dades d'exemple...");
            try (FileWriter fw = new FileWriter(fitxer)) {
                fw.write("Jornada,EquipLocal,EquipVisitant,GolsLocal,GolsVisitant\n");
                fw.write("1,Barça,Madrid,2,1\n");
                fw.write("1,Girona,Espanyol,3,0\n");
                System.out.println(" Fitxer '" + rutaFitxer + "' creat correctament.");
            } catch (IOException e) {
                System.err.println(" Error en crear el fitxer automàticament: " + e.getMessage());
                return;
            }
        }

        String linia;
        String separadorCsv = ",";
        int comptadorLinia = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fitxer))) {

            while ((linia = br.readLine()) != null) {
                comptadorLinia++;

                // Ignoramos la cabecera
                if (comptadorLinia == 1 && linia.toLowerCase().contains("jornada")) {
                    continue;
                }

                if (linia.trim().isEmpty()) continue;

                String[] dades = linia.split(separadorCsv);

                // Controlamos que al menos tenga los datos de los equipos (3 columnas)
                if (dades.length < 3) {
                    System.err.println(" Error a la línia " + comptadorLinia + ": Format incorrecte (massa pocs camps).");
                    continue;
                }

                try {
                    int numJornada = Integer.parseInt(dades[0].trim());
                    String nomLocal = dades[1].trim();
                    String nomVisitant = dades[2].trim();

                    // Si el CSV no tiene goles (longitud 3), ponemos 0 por defecto. Si los tiene, los lee.
                    int golsLocal = 0;
                    int golsVisitant = 0;

                    if (dades.length >= 5) {
                        golsLocal = Integer.parseInt(dades[3].trim());
                        golsVisitant = Integer.parseInt(dades[4].trim());
                    }

                    if (nomLocal.equalsIgnoreCase(nomVisitant)) {
                        System.err.println("️ Error a la línia " + comptadorLinia + ": Un equip no pot jugar contra si mateix.");
                        continue;
                    }

                    Equip local = new Equip(nomLocal);
                    Equip visitant = new Equip(nomVisitant);

                    equipsUnics.add(local);
                    equipsUnics.add(visitant);

                    local = cercarEquip(nomLocal);
                    visitant = cercarEquip(nomVisitant);

                    jornadesMap.putIfAbsent(numJornada, new Jornada(numJornada));
                    Partit nouPartit = new Partit(local, visitant, golsLocal, golsVisitant);
                    jornadesMap.get(numJornada).afegirPartit(nouPartit);

                } catch (NumberFormatException e) {
                    System.err.println("️ Error a la línia " + comptadorLinia + ": Dades numèriques invàlides.");
                }
            }
            System.out.println(" Procés de càrrega finalitzat correctament.");

        } catch (IOException e) {
            System.err.println(" Error crític: No s'ha pogut llegir el fitxer CSV. " + e.getMessage());
        }
    }

    /*
      sobreescribir el archivo.
     */
    public void guardarDadesCsv(String rutaFitxer) {
        try (FileWriter fw = new FileWriter(rutaFitxer)) {
            // Escribimos la cabecera estándar de 5 columnas
            fw.write("Jornada,EquipLocal,EquipVisitant,GolsLocal,GolsVisitant\n");

            // Recorremos todas las jornadas y sus partidos para guardarlos
            for (Jornada jornada : jornadesMap.values()) {
                for (Partit partit : jornada.getPartits()) { // Asegúrate de que Jornada tiene getPartits()
                    fw.write(String.format("%d,%s,%s,%d,%d\n",
                            jornada.getNumero(), // O el método que devuelva el id de la jornada
                            partit.getEquipLocal().getNom(),
                            partit.getEquipVisitant().getNom(),
                            partit.getGolsLocal(),
                            partit.getGolsVisitant()
                    ));
                }
            }
            System.out.println(" Fitxer CSV modificat i guardat correctament a: " + rutaFitxer);
        } catch (IOException e) {
            System.err.println(" Error en guardar les modificacions al CSV: " + e.getMessage());
        }
    }

    private Equip cercarEquip(String nom) {
        return equipsUnics.stream()
                .filter(e -> e.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElse(null);
    }

    public List<Equip> getEquipsPerAGUI() {
        List<Equip> llista = new ArrayList<>(equipsUnics);
        llista.sort(Comparator.comparing(Equip::getNom));
        return llista;
    }

    public Collection<Jornada> getJornadesPerAGUI() {
        return jornadesMap.values();
    }
}