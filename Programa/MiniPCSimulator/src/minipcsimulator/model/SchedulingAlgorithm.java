package minipcsimulator.model;

import java.util.List;

public interface SchedulingAlgorithm {
    Process selectNext(List<Process> readyProcesses);

    // Para algoritmos que dependen del reloj (HRRN)
    default Process selectNext(List<Process> readyProcesses, int currentTime) {
        return selectNext(readyProcesses);
    }

    default boolean isPreemptive() {
        return false;
    }

    default int getQuantum() {
        return 0;
    }
}