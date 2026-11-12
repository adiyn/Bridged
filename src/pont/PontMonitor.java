package pont;

import java.util.LinkedList;
import java.util.Queue;
import vehicules.*;

/**
 * moniteur synchronisant la circulation routière et le trafic des bateaux sur et sous le pont
 */
public class PontMonitor {

	/**
	 * cycle d'ouverture
	 */
	public enum EtatPont {
		OUVERT,
		OUVERTURE,
		FERME,
		FERMETURE
	}

	//trucs de pont
	private final int segments;
	private final TypeVehicule[] voieNormale; // RIEN = libre, autrement c'est pris
	private final TypeVehicule[] voieReverse;
	private int vehiculesSurNormal = 0;
	private int vehiculesSurReverse = 0;
	private EtatPont etat = EtatPont.FERME;

	private boolean entreeNormaleOuverte = true; //pour les convois
	private boolean entreeReverseOuverte = true;

	//trucs de gestion maritime
	private final Queue<Long> fileBateaux = new LinkedList<>();
	private long dernierBateauTraverse = -1;

	// timers pour le pont
	private long tempsDerniereFermeture = 0;
	private final long DELAI_MIN_REOUVERTURE_MS;

	private AfficheurPont afficheur;
	/**
	 * Initialise le moniteur
	 *
	 * @param nbSegments           nombre de segments composant chaque voie du pont
	 * @param delaiReouvertureMs   délai minimum entre la fermeture complète et une réouverture
	 */
	public PontMonitor(int nbSegments, long delaiReouvertureMs) {
		this.segments = nbSegments;

		this.voieNormale = new TypeVehicule[segments];
		this.voieReverse = new TypeVehicule[segments];

		for (int i = 0; i < segments; i++) {
			voieNormale[i] = TypeVehicule.RIEN;
			voieReverse[i] = TypeVehicule.RIEN;
		}

		this.DELAI_MIN_REOUVERTURE_MS = delaiReouvertureMs;
		this.tempsDerniereFermeture = System.currentTimeMillis();
		
		System.out.println("pont créé.");
		
		this.afficheur = new AfficheurPont(nbSegments);
		afficheur.draw(voieNormale, voieReverse);
		
	}



	/**
	 * Bloque le véhicule jusqu'à ce que les conditions d'entrée soient réunies, puis réserve ses segments nécessaires
	 *
	 * @param type                  type du véhicule
	 * @param sensGD                true pour Gauche -> Droite, false pour Droite -> Gauche
	 * @param nom                   identifiant véhicule
	 * @throws InterruptedException si le thread en attente d'accès est interrompu
	 */
	public synchronized void entrerPont(TypeVehicule type, boolean sens, String nom) throws InterruptedException {
		// enregistrement en file d'attente

		while (!peutEntrer(type, sens)) {
			wait();
		}

		reserverSegments(sens, 0, type.taille, type);

		if (sens) {
			vehiculesSurNormal++;
		} else {
			vehiculesSurReverse++;
		}
		
		afficheur.draw(voieNormale, voieReverse);

		notifyAll();
	}


	/**
	 * vérifie si un véhicule remplit toutes les conditions pour s'engager
	 */
	private boolean peutEntrer(TypeVehicule type, boolean sensGD) {
		// le pont doit être fermé aux bateaux
		if (etat != EtatPont.FERME) {
			return false;
		}

		// vérifie que les segments d'entrée nécessaires sont libres
		return verifierSegmentsLibres(sensGD, 0, type.taille);
	}


	/**
	 * Fait progresser le véhicule en réservant sa taille devant lui et libérant sa place actuelle 
	 *
	 * @param type                  type du véhicule
	 * @param sensGD                true pour Gauche -> Droite, false pour Droite -> Gauche
	 * @param positionTeteActuelle  segment sur lequel le véhicule se trouve
	 * @throws InterruptedException si le thread en attente du segment suivant est interrompu
	 */
	public synchronized void avancer(TypeVehicule type, boolean sensGD, int positionActuelle) throws InterruptedException {


		int nouvellePos = positionActuelle + type.taille;

		if (nouvellePos < segments)
			while (!verifierSegmentsLibres(sensGD, nouvellePos, type.taille)) {
				wait();
			}
		else {
			sortirPont(type, sensGD);
		}
		//c'est bon

		reserverSegments(sensGD, nouvellePos, type.taille, type); //on réserve les segments suivants

		reserverSegments(sensGD, positionActuelle, type.taille, TypeVehicule.RIEN); //on libère ceux où on est

		afficheur.draw(voieNormale, voieReverse);
		
		notifyAll();
	}

	/**
	 * Libère l'ensemble des segments occupés par le véhicule et signale sa sortie du pont
	 *
	 * @param type   type du véhicule qui sort
	 * @param sensGD true pour Gauche -> Droite, false pour Droite -> Gauche
	 * @param nom    identifiant véhicule
	 */
	public synchronized void sortirPont(TypeVehicule type, boolean sens) {


		if (sens) {
			vehiculesSurNormal--;
		}
		else {
			vehiculesSurReverse--;
		}

		/*		if (type == TypeVehicule.CONVOI) {
			if (sensGD) convoisSurPontGD--; else convoisSurPontDG--;
		}

		if (type == TypeVehicule.PRIORITAIRE) {
			prioritairesSurPont--;
			if (prioritairesSurPont == 0) {
				bandeUrgenceActive = false;
				System.out.println("le véhicule prioritaire est sorti");
			}
		}
		 */
		notifyAll();
	}

	/**
	 * Enregistre la demande d'un bateau dans la file
	 *
	 * @param bateauId identifiant unique bateau
	 */
	public synchronized void demanderPassageBateau(long bateauId) {
		fileBateaux.add(bateauId);
		System.out.println("bateau #" + bateauId + " enregistré dans la file d'attente.");
		notifyAll();
	}

	/**
	 * Gère l'ouverture du pont, la sortie des véhicules et le passage des bateaux
	 *
	 * @throws InterruptedException si l'attente d'évacuation ou de franchissement est interrompue
	 */
	public synchronized void verifierEtDeclencherOuverture() throws InterruptedException {
		while (etat != EtatPont.FERME || fileBateaux.isEmpty()) {
			wait();
		}

		long tempsEcoule = System.currentTimeMillis() - tempsDerniereFermeture;
		if (tempsEcoule < DELAI_MIN_REOUVERTURE_MS) {
			long attenteRestante = DELAI_MIN_REOUVERTURE_MS - tempsEcoule;
			wait(attenteRestante);
			if (etat != EtatPont.FERME || fileBateaux.isEmpty()) {
				return;
			}
		}

		System.out.println("\nOUVERTURE DU PONT : BARRAGES BAISSÉS");
		etat = EtatPont.OUVERTURE;
		notifyAll();

		while (vehiculesSurNormal+vehiculesSurReverse > 0) {
			wait();
		}

		etat = EtatPont.OUVERT;
		System.out.println("PONT OUVERT. NAVIRES, AVANCEZ !\n");
		notifyAll();

		while (!fileBateaux.isEmpty()) {
			long bateauActuel = fileBateaux.poll();
			dernierBateauTraverse = bateauActuel;
			System.out.println("Bateau #" + bateauActuel + " avance sous le pont.");
			notifyAll();

			while (dernierBateauTraverse == bateauActuel) {
				wait();
			}
		}

		etat = EtatPont.FERMETURE;
		System.out.println("\nFERMETURE DU PONT EN COURS...");
		notifyAll();

		// temporisation mécanique de fermeture
		Thread.sleep(500);

		etat = EtatPont.FERME;
		tempsDerniereFermeture = System.currentTimeMillis();
		System.out.println("PONT FERMÉ : REPRISE DE LA CIRCULATION\n");
		notifyAll();
	}

	/**
	 * Notifie qu'un bateau est passé
	 *
	 * @param bateauId identifiant du bateau qui est passé
	 */
	public synchronized void signalerBateauPasse(long bateauId) {
		if (dernierBateauTraverse == bateauId) {
			dernierBateauTraverse = -1;
			System.out.println("Bateau #" + bateauId + " a fini de traverser.");
			notifyAll();
		}
	}

	/**
	 * Met un bateau en attente tant que le pont n'est pas ouvert ou que son tour n'est pas venu
	 *
	 * @param bateauId identifiant du bateau en attente
	 * @throws InterruptedException si l'attente est interrompue
	 */
	public synchronized void attendreAutorisationBateau(long bateauId) throws InterruptedException {
		while ((etat != EtatPont.OUVERT) || (dernierBateauTraverse != bateauId)) {
			wait();
		}
	}

	/**
	 * Contrôle la disponibilité simultanée d'une série de segments consécutifs.
	 *
	 * @param sensGD voie concernée (true pour Gauche -> Droite, false pour Droite -> Gauche)
	 * @param debut  premier segment à tester
	 * @param taille nombre de segments
	 * @return true si tous les segments de la plage sont libres, false sinon
	 */
	private boolean verifierSegmentsLibres(boolean sensGD, int debut, int taille) {
		TypeVehicule[] voie = sensGD ? voieNormale : voieReverse;

		//on s'arrête de boucler en arrivant à la fin du tableau ou quand on a assez réservé
		int stoppingPoint = debut + taille < segments ? debut + taille : segments;

		for (int i = debut; i < stoppingPoint; i++) {
			if (voie[i] != TypeVehicule.RIEN) return false;
		}
		return true;
	}

	/**
	 * Réserve ou libère de façon atomique un ensemble de segments consécutifs.
	 *
	 * @param sensGD voie concernée (true pour Gauche -> Droite, false pour Droite -> Gauche)
	 * @param debut  premier segment
	 * @param taille nombre de segments contigus à mettre à jour
	 * @param nouvelleVal TypeVehicule.RIEN pour marquer comme libre, le type de véhicule pour occuper
	 */
	private void reserverSegments(boolean sensGD, int debut, int taille, TypeVehicule nouvelleVal) {
		TypeVehicule[] voie = sensGD ? voieNormale : voieReverse;

		//on s'arrête de boucler en arrivant à la fin du tableau ou quand on a assez réservé
		int stoppingPoint = debut + taille < segments ? debut + taille : segments;

		for (int i = debut; i < stoppingPoint; i++) {
			voie[i] = nouvelleVal;
		}
	}

	public int getTaille() {
		return segments;
	}
}