package proyecto;


import java.util.Objects;

public class Equip {
    private String nom;

    public Equip(String nom) {
        this.nom = nom.trim();
    }

    public String getNom() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equip equip = (Equip) o;
        return nom.equalsIgnoreCase(equip.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom.toLowerCase());
    }

    @Override
    public String toString() {
        return nom;
    }
}