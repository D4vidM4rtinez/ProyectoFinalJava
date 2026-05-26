public class Partit {
    private Equip equipLocal;
    private Equip equipVisitant;
    private int golsLocal;
    private int golsVisitant;

    public Partit(Equip equipLocal, Equip equipVisitant, int golsLocal, int golsVisitant) {
        this.equipLocal = equipLocal;
        this.equipVisitant = equipVisitant;
        this.golsLocal = golsLocal;
        this.golsVisitant = golsVisitant;
    }

    public Equip getEquipLocal() { return equipLocal; }
    public Equip getEquipVisitant() { return equipVisitant; }
    public int getGolsLocal() { return golsLocal; }
    public int getGolsVisitant() { return golsVisitant; }

    @Override
    public String toString() {
        return equipLocal + " " + golsLocal + " - " + golsVisitant + " " + equipVisitant;
    }
}