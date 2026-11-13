package vehicules;

import pont.PontMonitor;

public class Bateau extends Thread {
	private final PontMonitor pont;
    private final boolean sens; //true = normal (gauche vers droite), false = reverse (droite vers gauche)
    private final long id = getId();
	
    public Bateau(PontMonitor pont, boolean sens) {
    	this.pont = pont;
    	this.sens = sens;
    }
    
	public void run() {
		pont.seRanger(id);
		
		try {
			pont.demanderPassageBateaux(sens);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
