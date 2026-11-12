package pont;

import vehicules.TypeVehicule;

public class AfficheurPont {
	private final int segments;
	
	public AfficheurPont(int nbSegs) {
		segments = nbSegs;
	}
	
	
	public void draw(TypeVehicule[] vNormale, TypeVehicule[] vReverse) {
		//clear le terminal
		System.out.print("\033[H\033[2J");
		System.out.flush();
		
		//pas afficher tout en haut quand même
		System.out.print("\n\n\n");
		
		//le haut du pont
		for (int i = 0; i < segments; i++) {
			System.out.print("-");
		} System.out.print("\n");
		
		//la voie de droite vers gauche
		afficherVoie(vReverse, false);
		
		//le millieu du pont
		for (int i = 0; i < segments; i++) {
			if (i%2 == 0)
				System.out.print("-");
			else
				System.out.print(" ");
		} System.out.print("\n");
		
		//la voie de gauche vers droite
		afficherVoie(vNormale, true);
		
		//le bas du pont
		for (int i = 0; i < segments; i++) {
			System.out.print("-");
		} System.out.print("\n");

		System.out.print("\n\n\n");
		
		
		System.out.flush();
	}
	
	private void afficherVoie(TypeVehicule[] voie, boolean normal) {
		if (normal)
			for (int i = 0; i < segments; i++) {
				switch (voie[i]) {
				case RIEN:
					System.out.print(" ");
					break;
				case VOITURE:
					System.out.print("V");
					break;
				case CAMION:
					System.out.print("C");
					break;
				case PRIO:
					System.out.print("P");
					break;
				}
			}
		else {
			for (int i = segments-1; i >= 0; i--) {
				switch (voie[i]) {
				case RIEN:
					System.out.print(" ");
					break;
				case VOITURE:
					System.out.print("V");
					break;
				case CAMION:
					System.out.print("C");
					break;
				case PRIO:
					System.out.print("P");
					break;
				}
			}
		}
		System.out.print("\n");
	}
}
