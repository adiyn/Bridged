package threads.Sync.projet;

import java.util.LinkedList;
import java.util.Queue;

/**
 * moniteur synchronisant la circulation routière et le trafic des bateaux sur et sous le pont
 */
public class PontMonitor {

    /**
     * cycle d'ouverture
     */
    public enum EtatPont {
        FERME,
        OUVERTURE,
        OUVERT,
        FERMETURE
    }

    /**
     * catégories de véhicules avec leur taille
     */
    public enum TypeVehicule {
        VOITURE(1),
        CAMION(2),
        CONVOI(4),
        PRIORITAIRE(1);

        public final int taille;
        TypeVehicule(int taille) { this.taille = taille; }
    }

    private final int segments;
    private final boolean[] voieGD; // true = libre
    private final boolean[] voieDG; // true = libre

    private EtatPont etat = EtatPont.FERME;
    private int vehiculesSurPont = 0;

    // gestion des convois
    private int convoisEnAttenteGD = 0;
    private int convoisEnAttenteDG = 0;
    private int convoisSurPontGD = 0;
    private int convoisSurPontDG = 0;

    // gestion des véhicules prioritaires et bande d'urgence
    private int prioritairesEnAttente = 0;
    private int prioritairesSurPont = 0;
    private boolean bandeUrgenceActive = false;

    // file d'attente fifo pour les bateaux
    private final Queue<Long> fileBateaux = new LinkedList<>();
    private long dernierBateauTraverse = -1;

    // timers pour le pont
    private long tempsDerniereFermeture = 0;
    private final long DELAI_MIN_REOUVERTURE_MS;

    /**
     * Initialise le moniteur
     *
     * @param nbSegments           nombre de segments composant chaque voie du pont
     * @param delaiReouvertureMs   délai minimum entre la fermeture complète et une réouverture
     */
    public PontMonitor(int nbSegments, long delaiReouvertureMs) {
        this.segments = nbSegments;
        this.DELAI_MIN_REOUVERTURE_MS = delaiReouvertureMs;
        this.voieGD = new boolean[segments];
        this.voieDG = new boolean[segments];
        for (int i = 0; i < segments; i++) {
            voieGD[i] = true;
            voieDG[i] = true;
        }
        this.tempsDerniereFermeture = System.currentTimeMillis();
    }

    /**
     * Bloque le véhicule jusqu'à ce que les conditions d'entrée soient réunies, puis réserve ses segments nécessaires
     *
     * @param type                  type du véhicule
     * @param sensGD                true pour Gauche -> Droite, false pour Droite -> Gauche
     * @param nom                   identifiant véhicule
     * @throws InterruptedException si le thread en attente d'accès est interrompu
     */
    public synchronized void entrerPont(TypeVehicule type, boolean sensGD, String nom) throws InterruptedException {
        // enregistrement en file d'attente
        if (type == TypeVehicule.CONVOI) {
            if (sensGD) convoisEnAttenteGD++; else convoisEnAttenteDG++;
        } else if (type == TypeVehicule.PRIORITAIRE) {
            prioritairesEnAttente++;
        }

        try {
            while (!peutEntrer(type, sensGD)) {
                wait();
            }
        } finally {
            // décrémentation propre même en cas d'interruption
            if (type == TypeVehicule.CONVOI) {
                if (sensGD) convoisEnAttenteGD--; else convoisEnAttenteDG--;
            } else if (type == TypeVehicule.PRIORITAIRE) {
                prioritairesEnAttente--;
            }
        }

        // engagement effectif sur le pont
        if (type == TypeVehicule.CONVOI) {
            if (sensGD) convoisSurPontGD++; else convoisSurPontDG++;
        }

        if (type == TypeVehicule.PRIORITAIRE) {
            prioritairesSurPont++;
            bandeUrgenceActive = true;
        }

        reserverSegments(sensGD, 0, type.taille, false);
        vehiculesSurPont++;
        System.out.println("> " + nom + " (" + type + ") s'engage sur le pont.");
        notifyAll();
    }

    /**
     * vérifie si un véhicule remplit toutes les conditions pour s'engager
     */
    private boolean peutEntrer(TypeVehicule type, boolean sensGD) {
        // le pont doit être fermé, lol
        if (etat != EtatPont.FERME) {
            return false;
        }

        // priorité absolue aux véhicules d'urgence
        boolean prioritaireEnCours = prioritairesEnAttente > 0 || prioritairesSurPont > 0;
        if (type != TypeVehicule.PRIORITAIRE && prioritaireEnCours) {
            return false;
        }

        // voie adverse bloquée par un convoi ?
        if (voieBloqueeParConvoi(type, sensGD)) {
            return false;
        }

        // un convoi attend que toute la voie d'en face soit vide
        if (type == TypeVehicule.CONVOI) {
            boolean[] voieFace = sensGD ? voieDG : voieGD;
            if (compterVehicules(voieFace) > 0) {
                return false;
            }
        }

        // vérifie que les segments d'entrée nécessaires sont libres
        return verifierSegmentsLibres(sensGD, 0, type.taille);
    }

    /**
     * vérifie si un convoi arrivant ou présent en face interdit l'accès
     */
    private boolean voieBloqueeParConvoi(TypeVehicule type, boolean sensGD) {
        int convoisSurPontEnFace   = sensGD ? convoisSurPontDG   : convoisSurPontGD;
        int convoisEnAttenteEnFace = sensGD ? convoisEnAttenteDG : convoisEnAttenteGD;

        // si un convoi roule déjà en face, interdiction d'entrer
        if (convoisSurPontEnFace > 0) {
            return true;
        }

        // si un convoi attend en face
        if (convoisEnAttenteEnFace > 0) {
            // arbitrage : gauche -> droite a la priorité si deux convois attendent
            if (type == TypeVehicule.CONVOI && sensGD) {
                return false; // on laisse passer gauche -> droite
            }
            return true; // les autres attendent
        }

        return false;
    }

    /**
     * Fait progresser le véhicule d'un cran en réservant la nouvelle position de tête et en libérant la queue (snake)
     *
     * @param type                  type du véhicule
     * @param sensGD                true pour Gauche -> Droite, false pour Droite -> Gauche
     * @param positionTeteActuelle  segment sur lequel le véhicule se trouve
     * @throws InterruptedException si le thread en attente du segment suivant est interrompu
     */
    public synchronized void avancer(TypeVehicule type, boolean sensGD, int positionTeteActuelle) throws InterruptedException {
        // les véhicules ordinaires s'arrêtent sur la bande d'urgence pendant le passage d'une urgence
        if (type != TypeVehicule.PRIORITAIRE) {
            while (bandeUrgenceActive) {
                wait();
            }
        }

        int nouvelleTete = positionTeteActuelle + 1;

        while (nouvelleTete < segments && !isSegmentLibre(sensGD, nouvelleTete)) {
            wait();
        }

        setSegment(sensGD, nouvelleTete, false);
        int queueALiberer = positionTeteActuelle - type.taille + 1;
        if (queueALiberer >= 0) {
            setSegment(sensGD, queueALiberer, true);
        }

        notifyAll();
    }

    /**
     * Libère l'ensemble des segments occupés par le véhicule et signale sa sortie du pont
     *
     * @param type   type du véhicule qui sort
     * @param sensGD true pour Gauche -> Droite, false pour Droite -> Gauche
     * @param nom    identifiant véhicule
     */
    public synchronized void sortirPont(TypeVehicule type, boolean sensGD, String nom) {
        for (int i = 0; i < type.taille; i++) {
            int seg = segments - 1 - i;
            if (seg >= 0) setSegment(sensGD, seg, true);
        }

        vehiculesSurPont--;

        if (type == TypeVehicule.CONVOI) {
            if (sensGD) convoisSurPontGD--; else convoisSurPontDG--;
        }

        if (type == TypeVehicule.PRIORITAIRE) {
            prioritairesSurPont--;
            if (prioritairesSurPont == 0) {
                bandeUrgenceActive = false;
                System.out.println("le véhicule prioritaire est sorti");
            }
        }

        System.out.println("> " + nom + " a quitté le pont.");
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

        while (vehiculesSurPont > 0) {
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
     * Vérifie la disponibilité d'un segment précis sur une voie
     *
     * @param sensGD direction à tester (true pour Gauche -> Droite, false pour Droite -> Gauche)
     * @param index  indice du segment
     * @return true si le segment est libre ou hors du pont, false s'il est occupé
     */
    private boolean isSegmentLibre(boolean sensGD, int index) {
        if (index >= segments) return true;
        return sensGD ? voieGD[index] : voieDG[index];
    }

    /**
     * Met à jour l'état d'occupation d'un segment donné.
     *
     * @param sensGD voie cible (true pour Gauche -> Droite, false pour Droite -> Gauche)
     * @param index  indice du segment à occuper ou libérer
     * @param valeur true pour libérer le segment, false pour l'occuper
     */
    private void setSegment(boolean sensGD, int index, boolean valeur) {
        if (index >= 0 && index < segments) {
            if (sensGD) voieGD[index] = valeur;
            else voieDG[index] = valeur;
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
        boolean[] voie = sensGD ? voieGD : voieDG;
        for (int i = debut; i < debut + taille; i++) {
            if (i < segments && !voie[i]) return false;
        }
        return true;
    }

    /**
     * Réserve ou libère de façon atomique un ensemble de segments consécutifs.
     *
     * @param sensGD voie concernée (true pour Gauche -> Droite, false pour Droite -> Gauche)
     * @param debut  premier segment
     * @param taille nombre de segments contigus à mettre à jour
     * @param libre  true pour marquer comme libre, false pour occuper
     */
    private void reserverSegments(boolean sensGD, int debut, int taille, boolean libre) {
        boolean[] voie = sensGD ? voieGD : voieDG;
        for (int i = debut; i < debut + taille; i++) {
            if (i < segments) voie[i] = libre;
        }
    }

    /**
     * Calcule le nombre de segments actuellement occupés sur une voie donnée.
     *
     * @param voie les segments de la voie.
     * @return nombre de segments occupés.
     */
    private int compterVehicules(boolean[] voie) {
        int count = 0;
        for (boolean b : voie) if (!b) count++;
        return count;
    }
}