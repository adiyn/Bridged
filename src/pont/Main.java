package pont;

import vehicules.TypeVehicule;
import vehicules.Vehicule;

public class Main {

	public static void main(String[] args) {
		PontMonitor pont = new PontMonitor(27, 60000);
		
		Vehicule voiture1 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture2 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture3 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture4 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture5 = new Vehicule(pont, TypeVehicule.VOITURE, false, "rever");
		
		voiture1.start();
		voiture2.start();
		voiture3.start();
		voiture4.start();
		voiture5.start();
	}

}
