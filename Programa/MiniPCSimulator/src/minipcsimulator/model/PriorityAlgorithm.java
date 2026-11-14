package minipcsimulator.model;

import java.util.List;

public class PriorityAlgorithm implements SchedulingAlgorithm {
    private final boolean preemptive;

    public PriorityAlgorithm(boolean preemptive) {
        this.preemptive = preemptive;
    }

    @Override
    public Process selectNext(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        Process selected = readyProcesses.get(0);
        for (Process p : readyProcesses) {
            if (p.getPCB().getPriority() > selected.getPCB().getPriority() || (p.getPCB().getPriority() == selected.getPCB().getPriority()
                        && p.getArrivalTime() < selected.getArrivalTime())) {
                selected = p;
            }
        }
        return selected;
    }

    @Override
    public boolean isPreemptive() {
        return preemptive;
    }
}