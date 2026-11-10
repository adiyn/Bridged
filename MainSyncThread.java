package threads.Sync;

public class MainSyncThread {
    private static MonitorSyncThread m = new MonitorSyncThread();
    private static int N = 100;
    private static boolean[] pont = {true, true, true};
    private static int[] pos = {0, 0, 0, 0};
    private static int[] sens = {0, 0, 0, 0};


    public static void ex3a () {
        System.out.println("\nex 3a\n");

        Thread t1 = new Thread(new WaitThreadSync(m), "Thread-1");
        Thread t2 = new Thread(new WaitThreadSync(m), "Thread-2");
        Thread t3 = new Thread(new WakeSyncThread(m), "Thread-Reveil");

        t1.start();
        t2.start();
        t3.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void ex3b() {
        System.out.println("\nex 3b\n");

        Thread A = new Thread(new Thread(() -> {
            try {
                m.countOdd(N);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }), "Thread A");
        Thread B = new Thread(new Thread(() -> {
            try {
                m.countEven(N);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }), "Thread B");

        A.start();
        B.start();
    }

    public static void ex4() {
        System.out.println("\nex 4\n");

        Thread voiture_A = new Thread(new Thread(() -> {
            try {
                m.entrerDans(pont, sens[0], Thread.currentThread().getName(), pos[0]);
                pos[0]++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }), "Voiture A");

        Thread voiture_B = new Thread(new Thread(() -> {
            try {
                m.entrerDans(pont, sens[1], Thread.currentThread().getName(), pos[1]);
                pos[1]++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }), "Voiture B");

        Thread voiture_C = new Thread(new Thread(() -> {
            try {
                m.entrerDans(pont, sens[2], Thread.currentThread().getName(), pos[3]);
                pos[2]++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }), "Voiture C");

        Thread voiture_D = new Thread(new Thread(() -> {
            try {
                m.entrerDans(pont, sens[3], Thread.currentThread().getName(), pos[3]);
                pos[3]++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }), "Voiture D");

        voiture_A.start();
        voiture_B.start();
        voiture_C.start();
        voiture_D.start();
    }

    public static void main(String[] args) {
        //ex3a();

        //ex3b();

        ex4();
    }
}