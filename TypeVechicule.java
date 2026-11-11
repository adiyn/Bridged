
public enum TypeVechicule {
	Voiture(1),
	Camion(2),
	Convoi(4),
	Prio(1);
	
	public int taille;
	
	TypeVehicule(int n) {
		taille = n;
	}
}
