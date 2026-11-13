package pont;

import vehicules.TypeVehicule;

public class AfficheurPont {
	private final int segments;
	
	public AfficheurPont(int nbSegs) {
		if (nbSegs < 5) {
			throw new IllegalArgumentException("il n'y a pas assez de segments pour afficher le pont correctement");
		}
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
	
	/**
	 * Affiche le pont et un bateau qui passe dessous. le pont est affiché verticalement.
	 * 
	 * @param sens true pour nord-> sud, false pour l'inverse.
	 */
	public void drawBateau(boolean sens) {
		final int FRAMECOUNT = 22;
		for (int i = 0; i < FRAMECOUNT; i++) {
			for (int j = 0; j < segments; j++) {
				if (j == segments/2 +1) {
					drawFrameBateau(i, sens);
				} else if (j == segments/2 || j == segments/2 +2) {
					System.out.println("               ");
				} else if (j % 2 == 0) {
					System.out.println("     | | |     ");
				} else {
					System.out.println("     |   |     ");
				}
				
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void drawFrameBateau(int frame, boolean sens) {
		if (sens)
		switch (frame) {
		case 1:
			System.out.println("U              ");
			break;
		case 2:
			System.out.println("AU             ");
			break;
		case 3:
			System.out.println("EAU            ");
			break;
		case 4:
			System.out.println("TEAU           ");
			break;
		case 5:
			System.out.println("ATEAU          ");
			break;
		case 6:
			System.out.println("BATEAU         ");
			break;
		case 7:
			System.out.println(" BATEAU        ");
			break;
		case 8:
			System.out.println("  BATEAU       ");
			break;
		case 9:
			System.out.println("   BATEAU      ");
			break;
		case 10:
			System.out.println("    BATEAU     ");
			break;
		case 11:
			System.out.println("     BATEAU    ");
			break;
		case 12:
			System.out.println("      BATEAU   ");
			break;
		case 13:
			System.out.println("       BATEAU  ");
			break;
		case 14:
			System.out.println("        BATEAU ");
			break;
		case 15:
			System.out.println("         BATEAU");
			break;
		case 16:
			System.out.println("          BATEA");
			break;
		case 17:
			System.out.println("           BATE");
			break;
		case 18:
			System.out.println("            BAT");
			break;
		case 19:
			System.out.println("             BA");
			break;
		case 20:
			System.out.println("              B");
			break;
		case 21:
			System.out.println("               ");
			break;
		}
		else {
			switch (frame) {
			case 1:
				System.out.println("              U");
				break;
			case 2:
				System.out.println("             UA");
				break;
			case 3:
				System.out.println("            UAE");
				break;
			case 4:
				System.out.println("           UAET");
				break;
			case 5:
				System.out.println("          UAETA");
				break;
			case 6:
				System.out.println("         UAETAB");
				break;
			case 7:
				System.out.println("        UAETAB ");
				break;
			case 8:
				System.out.println("       UAETAB  ");
				break;
			case 9:
				System.out.println("      UAETAB   ");
				break;
			case 10:
				System.out.println("     UAETAB    ");
				break;
			case 11:
				System.out.println("    UAETAB     ");
				break;
			case 12:
				System.out.println("   UAETAB      ");
				break;
			case 13:
				System.out.println("  UAETAB       ");
				break;
			case 14:
				System.out.println(" UAETAB        ");
				break;
			case 15:
				System.out.println("UAETAB         ");
				break;
			case 16:
				System.out.println("AETAB          ");
				break;
			case 17:
				System.out.println("ETAB           ");
				break;
			case 18:
				System.out.println("TAB            ");
				break;
			case 19:
				System.out.println("AB             ");
				break;
			case 20:
				System.out.println("B              ");
				break;
			case 21:
				System.out.println("               ");
				break;
			}
		}
	}
	
	public void drawOuverture() {
		for (int i = 0; i < segments; i++) {
			if (i % 2 == 0) {
				System.out.println("     | | |     ");
			} else {
				System.out.println("     |   |     ");
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < segments; i++) {
			if (i == segments/2 +1) {
				System.out.println("               ");
			} else if (i % 2 == 0) {
				System.out.println("     | | |     ");
			} else {
				System.out.println("     |   |     ");
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < segments; i++) {
			if (i >= segments/2 && i <= segments/2 +2) {
				System.out.println("               ");
			} else if (i % 2 == 0) {
				System.out.println("     | | |     ");
			} else {
				System.out.println("     |   |     ");
			}
		}
	}

	
	public void drawFermeture() {
		for (int i = 0; i < segments; i++) {
			if (i >= segments/2 && i <= segments/2 +2) {
				System.out.println("               ");
			} else if (i % 2 == 0) {
				System.out.println("     | | |     ");
			} else {
				System.out.println("     |   |     ");
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < segments; i++) {
			if (i == segments/2 +1) {
				System.out.println("               ");
			} else if (i % 2 == 0) {
				System.out.println("     | | |     ");
			} else {
				System.out.println("     |   |     ");
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < segments; i++) {
			if (i % 2 == 0) {
				System.out.println("     | | |     ");
			} else {
				System.out.println("     |   |     ");
			}
		}
	}
}

