package threads.Sync.projet.vehicules;

import threads.Sync.projet.PontMonitor;

public class Vehicule extends Thread {
    private final PontMonitor monitor;
    private final PontMonitor.TypeVehicule type;
    private final boolean sensGD;
    private final String nom;
    private final int segments;

    /**
     *
     * @param monitor       PontMonitor
     * @param type          Enum (voiture, camion, convoi, prioritaire, bateau)
     * @param sensGD        true = Gauche -> Droite, false = Droite -> Gauche :)
     * @param nom           ex : clio-1
     * @param nbSegments    longueur du pont
     */
    public Vehicule(PontMonitor monitor, PontMonitor.TypeVehicule type, boolean sensGD, String nom, int nbSegments) {
        this.monitor = monitor;
        this.type = type;
        this.sensGD = sensGD;
        this.nom = nom;
        this.segments = nbSegments;
    }

    @Override
    public void run() {
        try {
            monitor.entrerPont(type, sensGD, nom);
            int positionTete = type.taille - 1;


            while (positionTete < segments - 1) {
                Thread.sleep(type == PontMonitor.TypeVehicule.PRIORITAIRE ? 200 : 500);
                monitor.avancer(type, sensGD, positionTete);
                positionTete++;
            }

            Thread.sleep(300);
            monitor.sortirPont(type, sensGD, nom);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}