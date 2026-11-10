package minipcsimulator.model;

import java.util.ArrayList;
import minipcsimulator.model.Process.ProcessState;

/**
 * Clase que representa un proceso en el sistema operativo simulado.
 * Contiene la lista de instrucciones del proceso y su PCB (Process Control Block).
 */
public class Process {

        // Estados del proceso posibles
    public enum ProcessState {
        NEW("NEW"), //al seleccionar el archivo
        READY("READY"), //al cargar el programa a memoria (sin suspensión)
        READY_SUSPENDED("READY_SUSPENDED"), //al suspenderlo
        RUNNING("RUNNING"), //al ejecutarlo
        BLOCKED("BLOCKED"), //esperando I/O (sin suspensión)
        BLOCKED_SUSPENDED("BLOCKED_SUSPENDED"), //al bloquearlo por espera de I/O y suspenderlo
        EXIT("EXIT"); //al terminar de ejecutarse

        private final String displayName;

        ProcessState(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public boolean isSuspended() {
            return this == READY_SUSPENDED || this == BLOCKED_SUSPENDED;
        }
    }

    private ArrayList<Instruction> instructions;
    private PCB pcb;
    private ProcessState state;
    private int burstTime;
    private int arrivalTime;

    private int pid;


    // Esto es para interfaz
    String pathFile;
    String nameFile;

    // al iniciar el proceso

    /**
     * Constructor de la clase Process.
     * Crea el proceso y su PCB.
     * @param id El identificador del proceso a crear, es un int simple que inicia en 1 que lo asigna el Kernel.
     * @param startPosition La posición de memoria donde inicia el proceso en la memoria principal (RAM).
     * @param pcbStartAddress La posición de inicio en memoria del PCB.
     */
    public Process(int id, int startPosition, int pcbStartAddress) {
        this.pid = id;
        this.instructions = new ArrayList<>(); //vacía esperando a Loader
        this.pcb = new PCB(this, startPosition, pcbStartAddress);
        
    }

    /**
     * Asigna las instrucciones al proceso y configura la posición final del proceso en memoria.
     * Lo establece el Loader cuando carga el programa a disco.
     * @param instructions ArrayList de instrucciones que se asignarán al proceso.
     */
    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
        this.pcb.configEndPosition(instructions.size());
        this.setBurstTime();
    }

    /**
     * Obtiene el ArrayList de instrucciones del proceso.
     * @return El ArrayList de instrucciones del proceso.
     */
    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * Obtiene el PCB del proceso.
     * @return El PCB del proceso.
     */
    public PCB getPCB() {
        return pcb;
    }

    public void setFilePathAndName(String path, String name) {
        this.pathFile = path;
        this.nameFile = name;
    }

    public String getPathFile() {
        return pathFile;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public ProcessState getState() {
        return state;
    }

    public int getPID() {
        return pid;
    }

    public int getBurstTime(){
        return burstTime;
    }

    public void setBurstTime() {
        this.burstTime = instructions.size();
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
