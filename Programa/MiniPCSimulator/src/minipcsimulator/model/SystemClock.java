package minipcsimulator.model;

public class SystemClock {
    private static SystemClock instance = null;

    public SystemClock() {
        instance = this;
    }

    public static SystemClock getInstance() {
        if (instance == null) {
            instance = new SystemClock();
        }
        return instance;
    }

    private int ticks = -1;

    public void tick() {
        this.ticks++;
    }

    public int getTicks() {
        return this.ticks;
    }

    public void reset() {
        this.ticks = -1;
    }
}
