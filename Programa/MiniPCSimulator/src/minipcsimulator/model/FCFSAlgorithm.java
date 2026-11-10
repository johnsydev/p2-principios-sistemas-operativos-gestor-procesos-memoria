package minipcsimulator.model;

import java.util.List;

public class FCFSAlgorithm implements SchedulingAlgorithm {
    @Override
    public Process selectNext(List<Process> readyProcesses) {
        return readyProcesses.isEmpty() ? null : readyProcesses.get(0);
    }
}
