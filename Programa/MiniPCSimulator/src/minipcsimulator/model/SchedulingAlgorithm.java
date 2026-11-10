package minipcsimulator.model;

import java.util.List;

public interface SchedulingAlgorithm {
    Process selectNext(List<Process> readyProcesses);
}
