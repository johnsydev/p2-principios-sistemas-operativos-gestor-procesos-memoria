package minipcsimulator.model;

import java.util.List;

public class HRRNAlgorithm implements SchedulingAlgorithm {

    @Override
    public Process selectNext(List<Process> readyProcesses) {
        // se usa tiempo 0 como respaldo
        return selectNext(readyProcesses, 0);
    }

    @Override
    public Process selectNext(List<Process> readyProcesses, int currentTime) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        Process selected = null;
        double bestRatio = -1;

        for (Process p : readyProcesses) {
            if (p.getBurstTime() > 0) {
                int burst = p.getBurstTime(); 
                int waiting = currentTime - p.getArrivalTime();
                double ratio = (double) (waiting + burst) / burst;
                if (ratio > bestRatio || (ratio == bestRatio && p.getArrivalTime() < selected.getArrivalTime())) {
                    // Desempate
                    bestRatio = ratio;
                    selected = p;
                }
            }
        }
        return selected;
    }

    @Override
    public boolean isPreemptive() {
        return false;
    }
}