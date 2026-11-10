package threads.Sync;

public class WaitThreadSync extends Thread{
    private MonitorSyncThread monitor;
    public WaitThreadSync(MonitorSyncThread m) {
        this.monitor = m;
    }
    @Override
    public synchronized void run() {
        try {
            monitor.attendre();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
