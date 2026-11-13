package minipcsimulator.model;

import java.util.List;

public interface SchedulingAlgorithm {
    Process selectNext(List<Process> readyProcesses);

    default boolean isPreemptive() {
        return false;
    }

    default int getQuantum() {
        return 0; // sin quantum por defecto
    }
}