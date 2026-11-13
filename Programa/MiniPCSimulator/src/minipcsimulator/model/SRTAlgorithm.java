package minipcsimulator.model;

import java.util.List;

public class SRTAlgorithm implements SchedulingAlgorithm {
    @Override
    // Selecciona el proceso con el tiempo de ráfaga más corto entre los procesos listos.
    public Process selectNext(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        Process selectedProcess = readyProcesses.get(0);
        for (Process process : readyProcesses) {
            if (process.getBurstTime() < selectedProcess.getBurstTime()
                    || (process.getBurstTime() == selectedProcess.getBurstTime()
                        && process.getArrivalTime() < selectedProcess.getArrivalTime())) {
                selectedProcess = process;
            }
        }
        return selectedProcess;
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }
}