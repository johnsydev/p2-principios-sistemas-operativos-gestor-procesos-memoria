package minipcsimulator.model;

import java.util.ArrayList;
import java.util.Stack;
import minipcsimulator.model.Process.ProcessState;

/**
 * Clase que representa el PCB (Process Control Block) de un proceso en el sistema operativo simulado.
 */
public class PCB {

    // Referencia al proceso padre
    private Process ownerProcess; 

    // Posiciones en memoria
    private int memoryPositionPCB; // posición en memoria del PCB
    private int startPosition;
    private int sizeProcessScope;
    private int endPosition;
    
    // Información del PCB
    private int PID = 0;
    private ProcessState state = ProcessState.NEW;
    private int PC = 0; // Program Counter
    private int AC = 0; // Acumulador

    // Registros
    private String AX = "0";
    private int BX = 0;
    private int CX = 0;
    private String DX = "0";

    private Stack<Integer> stack;

    private int PSW = 0; // program status word, para banderas aritmeticas y de control, para este proyecto es solo Zero Flag (ZF)

    // Otros atributos

    private int cpu_id = 0;
    private int start_time = 0; // tick de tiempo de inicio del proceso
    private int time_spent = 0; // tiempo total que ha estado en ejecución el proceso en ticks
    private int time_end = 0; // tick de tiempo de fin del proceso

    ArrayList<String> openedFiles = new ArrayList<>();

    private PCB nextPCB; // referencia al siguiente PCB en la lista enlazada de PCBs

    private int priority = 1; // por defecto 1 para todos durante este proyecto
    
    // DISCO

    private int diskStartPosition = 0;
    private int diskProgramSize = 0; 

    // al iniciar el proceso

    /**
     * Constructor de la clase PCB.
     * Inicializa el PCB con el ID del proceso (los procesos inician en 100) y la posición de inicio en memoria.
     * @param id El identificador del proceso (número simple que inicia en 1 y es asignado por el Kernel).
     * @param startPosition La posición de inicio en memoria del proceso.
     * @param pcbStartAddress La posición de inicio en memoria del PCB.
     */
    public PCB(Process ownerProcess, int startPosition, int pcbStartAddress) {
        this.ownerProcess = ownerProcess;
        this.PID = ownerProcess.getPID();                            
        this.startPosition = startPosition;         // 4 es el tamaño fijo de PCB + cantidad de registros
        this.memoryPositionPCB = pcbStartAddress; //(SystemConfig.getPcbMemoryStart() + (this.PID-SystemConfig.FIRST_PROCESS_ID)) * (SystemConfig.PCB_SIZE);
        this.state = ProcessState.NEW;
        this.PC = startPosition;
        stack = new Stack();
    }

    /**
     * Calcula la posición final del proceso en memoria, basada en la cantidad de instrucciones que tiene, para evitar desbordamiento de memoria.
     * @param instructionsCount La cantidad de instrucciones del proceso.
     */
    public void configEndPosition(int instructionsCount) {
        this.sizeProcessScope = instructionsCount;
        this.endPosition = startPosition + instructionsCount;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public void setDiskStartPosition(int diskStartPosition) {
        this.diskStartPosition = diskStartPosition;
    }

    public void setDiskProgramSize(int diskProgramSize) {
        this.diskProgramSize = diskProgramSize;
    }

    public int getDiskStartPosition() {
        return diskStartPosition;
    }

    public int getDiskProgramSize() {
        return diskProgramSize;
    }

    /**
     * Obtiene la posición de inicio en memoria del proceso.
     * @return La posición de inicio en memoria del proceso.
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * Obtiene la cantidad de instrucciones del proceso.
     * @return La cantidad de instrucciones del proceso.
     */
    public int getSizeProcessScope() {
        return sizeProcessScope;
    }

    /**
     * Obtiene la posición final en memoria del proceso, para validar que el PC no se salga del rango de memoria del proceso.
     * @return La posición final en memoria del proceso.
     */
    public int getEndPosition() {
        return endPosition;
    }

    /**
     * Obtiene el ID del proceso.
     * @return El ID del proceso.
     */
    public int getPID() {
        return PID;
    }

    /**
     * Obtiene la posición en memoria del proceso.
     * @return La posición en memoria del proceso.
     */
    public int getMemoryPositionPCB() {
        return memoryPositionPCB;
    }

    /**
     * Cambia el estado del proceso.
     * @param state El nuevo estado del proceso.
     */
    public void setState(ProcessState state) {
        if (ownerProcess != null) {
            ownerProcess.setState(state);
        }
    }

    /**
     * Obtiene el estado actual del proceso.
     * @return El estado actual del proceso.
     */
    public ProcessState getState() {
        if (ownerProcess != null) {
            ProcessState state = ownerProcess.getState();
            this.state = state;
            return state;
        }
        return ProcessState.NEW;
    }

    // Getters / Setters para registros:

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }    

    public int getAC() {
        return AC;
    }

    public void setAC(int AC) {
        this.AC = AC;
    }

    public String getAX() {
        return AX;
    }

    public void setAX(String AX) {
        this.AX = AX;
    }

    public int getBX() {
        return BX;
    }

    public void setBX(int BX) {
        this.BX = BX;
    }

    public int getCX() {
        return CX;
    }

    public void setCX(int CX) {
        this.CX = CX;
    }

    public String getDX() {
        return DX;
    }

    public void setDX(String DX) {
        this.DX = DX;
    }

    public Stack<Integer> getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int PSW) {
        this.PSW = PSW;
    }

    public int getPriority() {
        return priority;
    }

    public void setCpuID(int cpu_id) {
        this.cpu_id = cpu_id;
    }

    public int getCpuID() {
        return cpu_id;
    }

    public int getStartTime() {
        return start_time;
    }

    public void setStartTime(int start_time) {
        this.start_time = start_time;
    }

    public int getTimeSpent() {
        return time_spent;
    }

    public int getTimeEnd() {
        return time_end;
    }

    public void setTimeSpent(int time_spent) {
        this.time_spent = time_spent;
    }

    public void incrementTimeSpent() {
        this.time_spent++;
    }

    public void setTimeEnd(int time_end) {
        this.time_end = time_end;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void addOpenedFile(String fileName) {
        if (!openedFiles.contains(fileName)) {
            openedFiles.add(fileName);
        }
    }

    public void removeOpenedFile(String fileName) {
        openedFiles.remove(fileName);
    }

    public String getStringOpenedFiles() {
        return String.join(", ", openedFiles);
    }

    public void setNextPCB(PCB nextPCB) {
        this.nextPCB = nextPCB;
    }

    public PCB getNextPCB() {
        return nextPCB;
    }
}
