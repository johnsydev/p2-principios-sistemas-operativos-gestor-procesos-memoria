package minipcsimulator.model;

import java.util.List;

public class GuaranteedAlgorithm implements SchedulingAlgorithm {
    private final int quantum;

    public GuaranteedAlgorithm(int quantum) {
        this.quantum = (quantum > 0) ? quantum : 1;
    }

    @Override
    public Process selectNext(List<Process> readyProcesses) {
        return selectNext(readyProcesses, 0);
    }

    @Override
    public Process selectNext(List<Process> readyProcesses, int currentTime) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        int n = readyProcesses.size();
        Process selected = null;
        double bestRatio = Double.MAX_VALUE;

        for (Process p : readyProcesses) {
            // CPU que le correspondía
            double entitled = (currentTime - p.getArrivalTime()) / (double) n;
            // si no ha tenido tiempo de CPU, ratio = 0
            double ratio = (entitled > 0) ? p.getCpuTime() / entitled : 0;

            // Selecciona el proceso con el menor ratio, y en caso de empate, el que llegó primero
            if (selected == null
                    || ratio < bestRatio
                    || (ratio == bestRatio && p.getArrivalTime() < selected.getArrivalTime())) {
                bestRatio = ratio;
                selected = p;
            }
        }
        return selected;
    }

    @Override
    public int getQuantum() {
        return quantum;
    }
} 

