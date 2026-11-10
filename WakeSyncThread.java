package threads.Sync;

public class WakeSyncThread extends Thread {
    private MonitorSyncThread monitor;
    public WakeSyncThread(MonitorSyncThread m) {
        this.monitor = m;
    }
    @Override
    public synchronized void run() {
        monitor.reveiller();
    }
}
