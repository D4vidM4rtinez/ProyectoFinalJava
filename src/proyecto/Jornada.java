package proyecto;

import java.util.ArrayList;
import java.util.List;

public class Jornada {
    private int numero;
    private List<Partit> partits;

    public Jornada(int numero) {
        this.numero = numero;
        this.partits = new ArrayList<>();
    }

    public int getNumero() { return numero; }
    public List<Partit> getPartits() { return partits; }
    public void afegirPartit(Partit partit) { this.partits.add(partit); }

    @Override
    public String toString() {
        return "Jornada " + numero + " (Partits: " + partits.size() + ")";
    }
}