package minipcsimulator.model;

import java.util.List;

public class GuaranteedAlgorithm implements SchedulingAlgorithm {
     private int lastPID = -1; // proceso escogido en la última selección (el que venía corriendo)

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

            // Selecciona el proceso con el menor ratio, y en caso de empate se aplican las reglas de desempate
            if (selected == null
                    || ratio < bestRatio
                    || (ratio == bestRatio && ganaEmpate(p, selected))) {
                bestRatio = ratio;
                selected = p;
            }
        }

        lastPID = selected.getPID();
        return selected;
    }

    // reglas de desempate: 1) sigue el que venía corriendo, 2) el que llegó primero, 3) el de menor PID (orden de carga)
    private boolean ganaEmpate(Process p, Process actual) {
        if (p.getPID() == lastPID) return true;
        if (actual.getPID() == lastPID) return false;
        if (p.getArrivalTime() != actual.getArrivalTime()) {
            return p.getArrivalTime() < actual.getArrivalTime();
        }
        return p.getPID() < actual.getPID();
    }

    public boolean isPreemptive() {
        return true;
    }
} 

