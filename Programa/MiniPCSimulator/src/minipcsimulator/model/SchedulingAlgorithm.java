package minipcsimulator.model;

import java.util.List;

public interface SchedulingAlgorithm {
    Process selectNext(List<Process> readyProcesses);

    default Process selectNext(List<Process> readyProcesses, int currentTime) {
        return selectNext(readyProcesses);
    }

    // true = en cada tick se reevalúa aunque haya un proceso corriendo
    default boolean isPreemptive() {
        return false;
    }

    // sin quantum por defecto
    default int getQuantum() {
        return 0;
    }
}