package vehicules;

import pont.PontMonitor;

public class Vehicule extends Thread {
    private final PontMonitor pont;
    private final TypeVehicule type;
    private final boolean sens; //true = normal (gauche vers droite), false = reverse (droite vers gauche)
    private final String nom;

    /**
     *
     * @param monitor       PontMonitor
     * @param type          Enum (voiture, camion, convoi, prioritaire, bateau)
     * @param sensGD        true = Gauche -> Droite, false = Droite -> Gauche :)
     * @param nom           ex : clio-1
     * @param nbSegments    longueur du pont
     */
    public Vehicule(PontMonitor pont, TypeVehicule type, boolean sens, String nom) {
    	if (type == TypeVehicule.RIEN) {
    		throw new IllegalArgumentException("Un véhicule ne peut avoir le type RIEN");
    	}
        this.pont = pont;
        this.type = type;
        this.sens = sens;
        this.nom = nom;
    }

    @Override
    public void run() {
        try {
            pont.entrerPont(type, sens, nom);
    
            int position = 0;
            int taillePont = pont.getTaille();

            while (position <= taillePont - 1) {
                Thread.sleep(type == TypeVehicule.PRIO ? 200 : 500);
                
                pont.avancer(type, sens, position);
                position += type.taille;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}