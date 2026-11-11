package vehicules;

public enum TypeVehicule {
	RIEN(0),
	VOITURE(1),
	CAMION(2),
	CONVOI(4),
	PRIO(1);
	
	public int taille;
	
	TypeVehicule(int n) {
		taille = n;
	}
}
