package pont;

import vehicules.Bateau;
import vehicules.TypeVehicule;
import vehicules.Vehicule;

public class Main {

	public static void main(String[] args) {
		PontMonitor pont = new PontMonitor(27, 6);
		
		Vehicule voiture1 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture2 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture3 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture4 = new Vehicule(pont, TypeVehicule.VOITURE, true, "test");
		Vehicule voiture5 = new Vehicule(pont, TypeVehicule.VOITURE, false, "rever");
		Bateau bato = new Bateau(pont, true);
		
		
		voiture1.start();
		voiture2.start();
		voiture3.start();
		bato.start();
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		voiture4.start();
		voiture5.start();
	}

}
