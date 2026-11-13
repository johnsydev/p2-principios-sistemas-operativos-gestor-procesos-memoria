package minipcsimulator.model;

import java.util.List;

public class RoundRobinAlgorithm implements SchedulingAlgorithm {
    private final int quantum;

    public RoundRobinAlgorithm(int quantum) {
        this.quantum = (quantum > 0) ? quantum : 1;
    }

    @Override
    public Process selectNext(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }
        return readyProcesses.get(0); // El que sigue en la cola de listos es el primer proceso de la lista
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    @Override
    public int getQuantum() {
        return quantum;
    }
}