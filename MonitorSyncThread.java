package threads.Sync;

import java.awt.desktop.ScreenSleepEvent;

import static java.lang.Thread.sleep;

public class MonitorSyncThread {
    private boolean disponible = false;
    private boolean canCount = false;
    private int sensCourant = 0; // 1 = sens A -> B, -1 = sens B -> A
    private int nbVoituresSurPont = 0;

    public synchronized void attendre() throws InterruptedException {
        while (!disponible) {
            System.out.println(Thread.currentThread().getName() + " attend");
            wait();
        }
        System.out.println(Thread.currentThread().getName() + " continue");
        notify();
    }
    public synchronized void reveiller() {
        disponible = !disponible;
        canCount = !canCount;
        System.out.println("Réveil de tous les threads !");
        notifyAll();
    }

    public void countOdd(int N) throws InterruptedException {
        for (int i=1; i<N; i+=2) {
            while (canCount) {
                System.out.println(Thread.currentThread().getName() + " attend");
                wait();
            }
            System.out.println(Thread.currentThread().getName() + " " + i);
            reveiller();
        }
    }
    public void countEven(int N) throws InterruptedException {
        for (int i=0; i<N; i+=2) {
            while (!canCount) {
                System.out.println(Thread.currentThread().getName() + " attend");
                wait();
            }
            System.out.println(Thread.currentThread().getName() + " " + i);
            reveiller();
        }
    }

    public synchronized void entrerDans(boolean[] pont, int sens, String nom, int pos) throws InterruptedException {
        while (sensCourant != 0 && sensCourant != sens) {
            System.out.println(nom + " attend (sens opposé)");
            wait();
        }
        sensCourant = sens;
        nbVoituresSurPont++;

        while (!pont[pos]) wait();
        pont[pos] = false;
        System.out.println(nom + " entre dans S" + (pos + 1));
        sleep(500);
        pont[pos] = true;
        notifyAll();

        nbVoituresSurPont--;
        if (nbVoituresSurPont == 0) {
            sensCourant = 0;
            System.out.println(nom + " est sortie, pont libre");
        }
        notifyAll();
    }
}
