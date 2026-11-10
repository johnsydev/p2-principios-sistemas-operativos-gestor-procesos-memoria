package minipcsimulator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parte del simulador (no del SO), guarda los programas seleccionados que
 * todavía no han llegado al sistema. Cuando el reloj llega a su tiempo de
 * llegada se le entregan al SO para que cree el Job.
 */
public class ArrivalQueue {

    /**
     * Clase que representa un programa que todavía no ha llegado al sistema.
     */
    public static class PendingProgram {
        Map.Entry<ArrayList<String>, ArrayList<String>> fileData; // [ruta, nombre] -> líneas
        String name;
        int rafaga;
        int arrivalTime = 0;
        int priority = 1;
        int cpu = 1;

        public PendingProgram(Map.Entry<ArrayList<String>, ArrayList<String>> fileData, String name, int rafaga) {
            this.fileData = fileData;
            this.name = name;
            this.rafaga = rafaga;
        }

        public Map.Entry<ArrayList<String>, ArrayList<String>> getFileData() {
            return fileData;
        }
        
        public String getName() {
            return name;
        }

        public int getRafaga() {
            return rafaga;
        }

        public int getArrivalTime() {
             return arrivalTime;
        }
        public void setArrivalTime(int arrivalTime) { 
            this.arrivalTime = arrivalTime;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
             this.priority = priority;
        }

        public int getCpu() {
            return cpu;
        }

        public void setCpu(int cpu) {
            this.cpu = cpu;
        }
    }


    // ESTO ES PARTE DE LA CLASE ArrivalQueue:

    private ArrayList<PendingProgram> programs = new ArrayList<>();

    public void add(PendingProgram program) {
        programs.add(program);
    }

    public ArrayList<PendingProgram> getPrograms() {
        return programs;
    }

    public boolean hasPending() {
        return !programs.isEmpty();
    }

    public int size() {
        return programs.size();
    }

    /**
     * Saca de la cola los programas que ya llegaron, ordenados por tiempo de llegada.
     */
    public List<PendingProgram> getArrivedPrograms(int currentTime) {
        List<PendingProgram> arrived = new ArrayList<>();
        for (PendingProgram p : programs) {
            if (p.getArrivalTime() <= currentTime) {
                arrived.add(p);
            }
        }
        arrived.sort((a, b) -> a.getArrivalTime() - b.getArrivalTime());
        programs.removeAll(arrived);
        return arrived;
    }

    public ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (PendingProgram p : programs) {
            names.add(p.getName());
        }
        return names;
    }

    public void clear() {
        programs.clear();
    }

    public int getNextCpuID(int cpuCount) {
        return size() % cpuCount + 1;
    }
}