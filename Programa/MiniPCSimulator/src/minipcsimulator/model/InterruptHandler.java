package minipcsimulator.model;

public class InterruptHandler {
    MainMemory memory;
    ProcessList processList;
    Scheduler scheduler;

    String bufferOutput = "";
    boolean hasPendingOutput = false;
    boolean hasPendingInput = false;
    FileSystem fileSystem;

    public InterruptHandler(MainMemory memory, ProcessList processList, Scheduler scheduler, FileSystem fileSystem) {
        this.memory = memory;
        this.processList = processList;
        this.scheduler = scheduler;
        this.fileSystem = fileSystem;
    }

    public void handleInterruptEXIT(CPU cpu) {
        // Obtener el PCB actual
        PCB pcb = cpu.getPCB();
        if (pcb == null) {
            System.out.println("No hay PCB actual para manejar la interrupción EXIT.");
            return;
        }

        cpu.getPCB().setState(Process.ProcessState.EXIT);

        // Eliminar el proceso de la lista de procesos
        processList.removeProcess(pcb.getPID());

        sendToIInternalConsole("[PID " + pcb.getPID() + "]: " + "Proceso terminado con éxito." + "\n");

        cpu.setPCB(null);

        // Verificar si hay trabajos pendientes para admitir

        boolean haySuspendidosPendientes = this.scheduler.hasPendingSuspendedProcesses();

        if (!haySuspendidosPendientes) {
            this.scheduler.checkAdmitJob(); // Admitir automáticamente el siguiente Job en espera si cabe en la RAM liberada
        } else {
            System.out.println("No se admiten nuevos Jobs porque hay procesos en READY_SUSPENDED esperando memoria RAM.");
        } 

    }

    public void handleInterruptIO(CPU cpu, String ioType) {
        if (ioType.equals("OUTPUT")) {

            this.bufferOutput = cpu.getPCB().getDX() + "\n";
            this.hasPendingOutput = true;
            
        } else {
            System.out.println("Tipo de E/S desconocido: " + ioType);
        }
    }

    public void handleInterruptIOInput(Process blockedProcess, int value) {
        if (blockedProcess != null && blockedProcess.getPCB() != null) {
            this.hasPendingInput = false;
            if (value < 0 || value > 255) {
                blockedProcess.getPCB().setState(Process.ProcessState.EXIT);
                throw new RuntimeException("Error: El valor de entrada debe estar entre 0 y 255.");
            }
            blockedProcess.getPCB().setDX(String.valueOf(value));
            
            blockedProcess.getPCB().setState(Process.ProcessState.READY);
            
            System.out.println("InterruptHandler: Proceso PID " + blockedProcess.getPCB().getPID() + " desbloqueado con DX = " + value);
        }
    }

    public void handleInterruptFileManager(CPU cpu, String operation, String fileName) {
        cpu.getPCB().setState(Process.ProcessState.BLOCKED);
        System.out.println("InterruptHandler: Proceso PID " + cpu.getPCB().getPID() + " bloqueado para operación de File Manager: " + operation + " con DX = " + fileName + " y AL = " + cpu.getAL());
        switch (operation) {
            case "CREATE_FILE":
                int createResult = this.fileSystem.createFile(fileName);
                //cpu.setAL(createResult);
                break;
            case "DELETE_FILE":
                int deleteResult = this.fileSystem.deleteFile(fileName);
                //cpu.setAL(deleteResult);
                break;
            case "READ_FILE":
                String content = this.fileSystem.readFile(fileName);
                if (content != null) {
                    cpu.setAL(Integer.parseInt(content)); // Success
                }
                break;
            case "WRITE_FILE":
                int writeResult = this.fileSystem.writeFile(fileName, String.valueOf(cpu.getAL()));
                //cpu.setAL(writeResult); // Success
                break;
            case "OPEN_FILE":
                int openResult = this.fileSystem.openFile(fileName);
                //cpu.setAL(openResult); // Success
                cpu.getPCB().addOpenedFile(fileName);
                break;
            default:
                System.out.println("Operación de File Manager desconocida: " + operation);
        }
        cpu.getPCB().setState(Process.ProcessState.READY);
    }

    public boolean hasPendingOutput() {
        return hasPendingOutput;
    }

    public boolean hasPendingInput() {
        return hasPendingInput;
    }

    public String clearBufferOutput() {
        String output = bufferOutput;
        bufferOutput = "";
        hasPendingOutput = false;
        return output;
    }

    public void sendToIInternalConsole(String output) {
        this.bufferOutput = output + "\n";
        this.hasPendingOutput = true;
    }
}
