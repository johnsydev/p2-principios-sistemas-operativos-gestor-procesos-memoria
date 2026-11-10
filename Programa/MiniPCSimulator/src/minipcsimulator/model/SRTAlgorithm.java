package minipcsimulator.model;

import java.util.List;

public class SRTAlgorithm implements SchedulingAlgorithm {
    @Override
    public Process selectNext(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        Process selectedProcess = readyProcesses.get(0);
        for (Process process : readyProcesses) {
            if (process.getBurstTime() < selectedProcess.getBurstTime()) {
                selectedProcess = process;
            } else if (process.getBurstTime() == selectedProcess.getBurstTime()) {
                if (process.getArrivalTime() < selectedProcess.getArrivalTime()) {
                    selectedProcess = process;
                }
            }
        }
        return selectedProcess;
    }
}
