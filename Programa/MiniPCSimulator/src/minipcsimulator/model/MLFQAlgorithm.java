package minipcsimulator.model;

import java.util.List;

public class MLFQAlgorithm implements SchedulingAlgorithm {
    private final int quantum;
    private final int numLevels;

    public MLFQAlgorithm(int quantum, int numLevels) {
        this.quantum = (quantum > 0) ? quantum : 1;
        this.numLevels = (numLevels > 1) ? numLevels : 2;
    }

    @Override
    public Process selectNext(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        // El primero de la cola de menor nivel. El < estricto conserva el primero de cada
        // nivel, o sea el FIFO dentro de la cola (la lista guarda el orden de entrada).
        Process selected = readyProcesses.get(0);
        for (Process p : readyProcesses) {
            if (p.getLevel() < selected.getLevel()) {
                selected = p;
            }
        }
        return selected;
    }

    // Cuando un proceso agota su quantum baja una cola (la última no baja más)
    @Override
    public void onQuantumExpired(Process p) {
        int nivelActual = p.getLevel();
        int ultimoNivel = numLevels - 1;

        if (nivelActual < ultimoNivel) {
            p.setLevel(nivelActual + 1);
        }
    }

    @Override
    public int getQuantum() {
        return quantum;
    }
}