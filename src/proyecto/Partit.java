package proyecto;

public class Partit {
	private int id;
	private int numeroJornada;
	private Equip equipLocal;
	private Equip equipVisitant;
	private Integer golsLocal; // Usem Integer per admetre NULL a la BBDD
	private Integer golsVisitant;

	public Partit(Equip equipLocal, Equip equipVisitant, Integer golsLocal, Integer golsVisitant) {
		this.equipLocal = equipLocal;
		this.equipVisitant = equipVisitant;
		this.golsLocal = golsLocal;
		this.golsVisitant = golsVisitant;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumeroJornada() {
		return numeroJornada;
	}

	public void setNumeroJornada(int numeroJornada) {
		this.numeroJornada = numeroJornada;
	}

	public Equip getEquipLocal() {
		return equipLocal;
	}

	public Equip getEquipVisitant() {
		return equipVisitant;
	}

	public Integer getGolsLocal() {
		return golsLocal;
	}

	public void setGolsLocal(Integer golsLocal) {
		this.golsLocal = golsLocal;
	}

	public Integer getGolsVisitant() {
		return golsVisitant;
	}

	public void setGolsVisitant(Integer golsVisitant) {
		this.golsVisitant = golsVisitant;
	}

	@Override
	public String toString() {
		String gl = (golsLocal == null) ? "PENDENT" : golsLocal.toString();
		String gv = (golsVisitant == null) ? "PENDENT" : golsVisitant.toString();
		return equipLocal + " " + gl + " - " + gv + " " + equipVisitant;
	}
}