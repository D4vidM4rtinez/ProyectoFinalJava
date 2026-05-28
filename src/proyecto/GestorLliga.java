package proyecto;

import database.ConexionDB;
import database.EquipDAO;
import database.JornadaDAO;
import java.io.*;
import java.util.*;

public class GestorLliga {

    private final Map<Integer, Jornada> jornadesMap = new TreeMap<>();
    private final Set<Equip> equipsUnics = new HashSet<>();
    
    private ConexionDB conexionDB;
    private EquipDAO equipDAO;
    private JornadaDAO jornadaDAO;

    public GestorLliga() {
        // Requeriment d: Es connecta abans de començar a treballar
        this.conexionDB = new ConexionDB();
        this.equipDAO = new EquipDAO(conexionDB.getDb());
        this.jornadaDAO = new JornadaDAO(conexionDB.getDb());
    }

    public void carregarDadesCsv(String rutaFitxer) {
        File fitxer = new File(rutaFitxer);

        if (!fitxer.exists()) {
            try (FileWriter fw = new FileWriter(fitxer)) {
                fw.write("Jornada,EquipLocal,EquipVisitant,GolsLocal,GolsVisitant\n");
                fw.write("1,Barça,Madrid,0,0\n");
                fw.write("1,Girona,Espanyol,0,0\n");
                fw.write("2,Madrid,Girona,0,0\n");
                fw.write("2,Espanyol,Barça,0,0\n");
            } catch (IOException e) {
                System.err.println("Error en crear el fitxer automàticament: " + e.getMessage());
                return;
            }
        }

        String linia;
        int comptadorLinia = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fitxer))) {
            while ((linia = br.readLine()) != null) {
                comptadorLinia++;

                if (comptadorLinia == 1 && linia.toLowerCase().contains("jornada")) continue;
                if (linia.trim().isEmpty()) continue;

                String[] dades = linia.split(",");
                if (dades.length < 3) {
                    System.err.println("Error a la línia " + comptadorLinia + ": Format incorrecte.");
                    continue;
                }

                try {
                    int numJornada = Integer.parseInt(dades[0].trim());
                    String nomLocal = dades[1].trim();
                    String nomVisitant = dades[2].trim();

                    Integer golsLocal = null;
                    Integer golsVisitant = null;

                    if (dades.length >= 5) {
                        golsLocal = Integer.parseInt(dades[3].trim());
                        golsVisitant = Integer.parseInt(dades[4].trim());
                    }

                    if (nomLocal.equalsIgnoreCase(nomVisitant)) {
                        System.err.println("Error a la línia " + comptadorLinia + ": Un equip no pot jugar contra si mateix.");
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
                    nouPartit.setNumeroJornada(numJornada);
                    jornadesMap.get(numJornada).afegirPartit(nouPartit);

                } catch (NumberFormatException e) {
                    System.err.println("Error a la línia " + comptadorLinia + ": Dades numèriques invàlides.");
                }
            }
            System.out.println("Procés de càrrega del CSV finalitzat.");
        } catch (IOException e) {
            System.err.println("Error crític en llegir el fitxer CSV: " + e.getMessage()); // Requeriment b.iv
        }
    }

    public void guardarDadesACorpusBBDD() {
        System.out.println("Iniciant la càrrega de dades a la base de dades...");
        try {
            for (Equip equip : equipsUnics) {
                equipDAO.inserirEquip(equip);
            }
            for (Jornada jornada : jornadesMap.values()) {
                for (Partit partit : jornada.getPartits()) {
                    jornadaDAO.desarPartitInicial(jornada.getNumero(), partit);
                }
            }
            System.out.println("La càrrega a la base de dades ha finalitzat correctament."); // Requeriment b.v
        } catch (Exception e) {
            System.err.println("Error en carregar les dades a la BBDD: " + e.getMessage()); // Requeriment b.iv
        }
    }

    private Equip cercarEquip(String nom) {
        return equipsUnics.stream()
                .filter(e -> e.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElse(null);
    }

    public Collection<Jornada> getJornadesPerAGUI() { return jornadesMap.values(); }
    
    public int obtenirJornadaActualGUI() { return jornadaDAO.obtenirPrimeraJornadaPendent(); }
    
    public List<Partit> obtenirPartitsDeLaBBDD(int jornada) {
        // Crida al DAO per descarregar els partits reals de MySQL
        return jornadaDAO.obtenirPartitsBBDDPerJornada(jornada);
    }

    public void guardarResultatDesDeGUI(int jornada, String local, String visitant, int golsL, int golsV) {
        jornadaDAO.actualitzarResultatPartit(jornada, local, visitant, golsL, golsV);
    }

    public void tancarConnexions() {
        if (conexionDB != null) conexionDB.tancar();
    }
}