package minipcsimulator.model;

import java.util.ArrayList;
import minipcsimulator.utils.SystemConfig;

/**
 * Clase Loader que se encarga de cargar programas en memoria y convertir instrucciones a su representación interna.
 * @author johnsydev
 */
public class Loader {

    private static int nextFitIndex = SystemConfig.getUserMemoryStart();
    private static int nextFitIndexVirtual = SystemConfig.getDiskMemoryVirtualStart();
    public static ArrayList<Integer> temporalFrames = new ArrayList<>();
    public static ArrayList<Integer> temporalFramesVirtual = new ArrayList<>();

    public static void setTemporalFrames(ArrayList<Integer> frames, boolean isVirtual) {
        if (isVirtual) {
            temporalFramesVirtual = frames;
        } else  {
            temporalFrames = frames;
        }
    }

    public static ArrayList<Integer> getTemporalFrames(boolean isVirtual) {
        if (isVirtual) {
            ArrayList<Integer> tmp = new ArrayList<>(Loader.temporalFramesVirtual);
            Loader.temporalFramesVirtual = new ArrayList<>();
            return tmp;
        }
        ArrayList<Integer> tmp = new ArrayList<>(Loader.temporalFrames);
        Loader.temporalFrames = new ArrayList<>();
        return tmp;
    }

    public static void resetNextFitIndex() {
        nextFitIndex = SystemConfig.getUserMemoryStart();
        nextFitIndexVirtual = SystemConfig.getDiskMemoryVirtualStart();
    }

    public static int getNextFitIndex(boolean isVirtual) {
        if (isVirtual) {
            return nextFitIndexVirtual;
        }
        return nextFitIndex;
    }

    private static void setNextFitIndex(int startProcess, int size, boolean isVirtual) {
        int next = startProcess + size;

        if (isVirtual) {
            if (next >= SystemConfig.getDiskSize()) {
                next = SystemConfig.getDiskMemoryVirtualStart();
            }
            nextFitIndexVirtual = next;
        } else  {
            if (next >= SystemConfig.getMemorySize()) {
                next = SystemConfig.getUserMemoryStart();
            }
            nextFitIndex = next;
        }
    }

    /**
     * Carga el programa en disco (no en la memoria principal), crea las instrucciones para almacenarlas en el proceso y tenerlas listas para cargar en memoria posteriormente.
     * @param lines Lista de líneas del archivo .asm originales.
     * @param asmArray Matriz de instrucciones del archivo .asm tratadas por el parser (divididas en partes separadas por comas).
     * @param process El proceso al que se le asignarán las instrucciones.
     * @param disk El disco donde se almacenarán los programas.
     * @param fileIndex El índice del archivo que se almacenará en el disco.
     * @return La dirección de inicio en el disco donde se almacenó el programa.
     * @throws Exception Si no hay espacio libre suficiente en el disco para almacenar el programa.
     */
    public static int loadProgram(ArrayList<String> lines, ArrayList<ArrayList<String>> asmArray, Disk disk, FileIndex fileIndex) {
        int i = 0;
        ArrayList<Instruction> instructions = new ArrayList<>();
        int freeSpaceAddress;
        try {
            freeSpaceAddress = getFreeSpaceInDisk(asmArray.size(), disk);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el programa: " + e.getMessage());
        }

        int startAddress = freeSpaceAddress;
        fileIndex.setStartPosition(startAddress);

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            Instruction instruction = new Instruction(line, asmArray.get(i));
            instructions.add(instruction);

            disk.setPositionInstruction(freeSpaceAddress, instruction);

            freeSpaceAddress++;
            // Para GUI
            i++;
        }

        try {
            int posIndex = getFreeSpaceInFileIndex(disk);
            disk.setFileIndex(posIndex, fileIndex);
            fileIndex.setDiskIndexPosition(posIndex);
        } catch (Exception e) {
            throw new RuntimeException("Error al almacenar el índice del archivo en el disco: " + e.getMessage());
        }
        return startAddress;
    }

    /**
     * Carga las instrucciones del proceso en la memoria principal (RAM) directamente.
     * La carga la realiza iniciando en la primera posición de memoria de usuario definida en SystemConfig, pues actualmente es solo un proceso.
     * Con esto, el proceso pasa a estado READY y puede ser ejecutado por el CPU.
     * @param process El proceso al que se le asignarán las instrucciones.
     * @param memory La memoria principal del sistema.
     * @param disk El disco donde se encuentran las instrucciones.
     * @param startAddress La dirección de inicio en la memoria principal donde se cargarán las instrucciones.
     */
    public static void loadToMemory(Process process, MainMemory memory, Disk disk) {
        int size = process.getPCB().getDiskProgramSize(); // Cantidad de instrucciones
        int startAddressDisk = process.getPCB().getDiskStartPosition(); // posición inicio en DISCO

        ArrayList<Instruction> instructions = new ArrayList<>();
        try {
            int position = getFreeSpaceInMemory(size, memory); // posición inicio en RAM, i y position son RAM
            Loader.setNextFitIndex(position, size, false);
            int userMemoryStart = SystemConfig.getUserMemoryStart();

            if (SystemConfig.getMemoryStrategyAssignment().equals("paging")) {
                ArrayList<Integer> frames = Loader.getTemporalFrames(false);
                int pageSize = SystemConfig.getPagingSize();
                int copied = 0;
                for (Integer frame : frames) {
                    int index = userMemoryStart + pageSize * frame;
                    for (int i = index; i < index + pageSize; i++) {
                        if (copied >= size) break;
                        Instruction instruction = disk.getInstruction(startAddressDisk);
                        memory.setPositionInstruction(i, instruction);

                        instructions.add(instruction);
                        startAddressDisk++;
                        copied++;
                    }
                }
                process.getPCB().setFramesTable(frames);
            } else {
                for (int i = position; i < position + size; i++) {
                    Instruction instruction = disk.getInstruction(startAddressDisk);
                    memory.setPositionInstruction(i, instruction);

                    instructions.add(instruction);
                    startAddressDisk++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el programa en memoria: " + e.getMessage());
        }
        process.setInstructions(instructions);
    }

    /**
     * Carga las instrucciones del proceso en la memoria virtual del disco.
     * La carga la realiza iniciando en la primera posición de memoria virtual definida en SystemConfig.
     * Con esto, el proceso pasa a estado READY_SUSPENDED y puede ser ejecutado por el CPU cuando se cargue en memoria principal.
     * @param process El proceso al que se le asignarán las instrucciones.
     * @param disk El disco donde se encuentran las instrucciones.
     */
    public static void loadToVirtualMemory(Process process, Disk disk) {
        int size = process.getPCB().getDiskProgramSize();
        int startAddressDisk = process.getPCB().getDiskStartPosition();

        ArrayList<Instruction> instructions = new ArrayList<>();
        try {
            int positionVirtual = getFreeSpaceInVirtualMemory(size, disk);
            Loader.setNextFitIndex(positionVirtual, size, true);

            int diskMemoryVirtualStart = SystemConfig.getDiskMemoryVirtualStart();

            if (SystemConfig.getMemoryStrategyAssignment().equals("paging")) {
                ArrayList<Integer> frames = Loader.getTemporalFrames(true);
                int pageSize = SystemConfig.getPagingSize();
                int copied = 0;
                for (Integer frame : frames) {
                    int index = diskMemoryVirtualStart + pageSize * frame;
                    for (int i = index; i < index + pageSize; i++) {
                        if (copied >= size) break;
                        Instruction instruction = disk.getInstruction(startAddressDisk);
                        disk.setPositionInstruction(i, instruction);

                        instructions.add(instruction);
                        startAddressDisk++;
                        copied++;
                    }
                }
                process.getPCB().setFramesTable(frames);
            } else {

                for (int i = positionVirtual; i < positionVirtual + size; i++) {
                    Instruction instruction = disk.getInstruction(startAddressDisk);
                    disk.setPositionInstruction(i, instruction);

                    instructions.add(instruction);
                    startAddressDisk++;
                }
            }

            // Guarda la posición de inicio de la Memoria Virtual en el PCB
            process.getPCB().setStartPosition(positionVirtual);
            process.getPCB().setPC(positionVirtual);

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el programa en memoria virtual: " + e.getMessage());
        }
        process.setInstructions(instructions);
    }

    public static void loadFromVirtualToMemory(Process process, MainMemory memory, Disk disk) {
        int size = process.getPCB().getDiskProgramSize();
        int startVirtualAddress = process.getPCB().getStartPosition(); // Inicio en memoria virtual

        ArrayList<Instruction> instructions = new ArrayList<>();
        try {
            // nueva dirección de inicio libre en la RAM de Usuario
            int newRamPosition = getFreeSpaceInMemory(size, memory);
            Loader.setNextFitIndex(newRamPosition, size, false);

            int userMemoryStart = SystemConfig.getUserMemoryStart();

            if (SystemConfig.getMemoryStrategyAssignment().equals("paging")) {
                ArrayList<Integer> frames = Loader.getTemporalFrames(false);
                ArrayList<Integer> virtualFrames = process.getPCB().getFramesTable(); // marcos en memoria virtual
                int diskMemoryVirtualStart = SystemConfig.getDiskMemoryVirtualStart();
                int pageSize = SystemConfig.getPagingSize();
                int copied = 0;
                for (int p = 0; p < frames.size(); p++) {
                    int index = userMemoryStart + pageSize * frames.get(p);
                    int virtualIndex = diskMemoryVirtualStart + pageSize * virtualFrames.get(p);
                    for (int i = index; i < index + pageSize; i++) {
                        if (copied >= size) break;
                        Instruction instruction = disk.getInstruction(virtualIndex);

                        memory.setPositionInstruction(i, instruction);
                        instructions.add(instruction);

                        disk.setPosition(virtualIndex, null);

                        virtualIndex++;
                        copied++;
                    }
                }
                process.getPCB().setFramesTable(frames);
                process.getPCB().setStartPosition(newRamPosition);
                process.getPCB().setPC(newRamPosition);
                process.setInstructions(instructions);
            } else {

                // mueve instrucciones de memoria virtual a RAM y limpia memoria virtual
                for (int i = 0; i < size; i++) {
                    int currentVirtualAddr = startVirtualAddress + i;
                    int currentRamAddr = newRamPosition + i;

                    Instruction instruction = disk.getInstruction(currentVirtualAddr);
                    memory.setPositionInstruction(currentRamAddr, instruction);
                    instructions.add(instruction);

                    disk.setPosition(currentVirtualAddr, null);
                }

                // actualiza PCB
                process.getPCB().setStartPosition(newRamPosition);
                process.getPCB().setPC(newRamPosition); // Seteamos el PC al inicio en RAM
                process.setInstructions(instructions);   // Internamente ejecuta configEndPosition(size)
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el programa desde memoria virtual a memoria principal: " + e.getMessage());
        }
    }

    /**
     * Busca un bloque continuo de celdas libres en el Disco a partir de la
     * dirección reservada para programas de usuario.
     * 
     * @param size Cantidad de instrucciones consecutivas que requiere el programa.
     * @param disk El Disco Virtual donde se buscará el espacio libre.
     * @return La dirección de memoria del Disco donde inicia el espacio libre encontrado.
     * @throws Exception Si el disco está lleno o no hay un bloque contiguo del tamaño requerido.
     */
    public static int getFreeSpaceInDisk(int size, Disk disk) throws Exception {
        int startAddress = SystemConfig.getStartDiskForPrograms();
        int totalDiskSize = SystemConfig.getDiskSize();

        int consecutiveFree = 0;
        int startIndex = -1;

        for (int i = startAddress; i < totalDiskSize; i++) {
            // Verificamos si la celda actual en el disco está libre
            if (disk.getPosition(i) == null) {
                if (consecutiveFree == 0) {
                    startIndex = i; // marca el inicio si no había uno
                }
                consecutiveFree++;

                // si se encuentra bloque continuo con la cantidad de celdas necesarias
                if (consecutiveFree == size) {
                    return startIndex;
                }
            } else {
                // si se rompe la continuidad, reiniciamos el conteo
                consecutiveFree = 0;
                startIndex = -1;
            }
        }

        // Si después de recorrer todo el disco no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre contiguo suficiente en el Disco. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInMemory_first_next_Fit(int size, MainMemory memory, String type) throws Exception {
        int userMemoryStart = SystemConfig.getUserMemoryStart();
        int startAddress = userMemoryStart;
        if (type.equals("next")) {
            startAddress = Loader.getNextFitIndex(false);
        }
        int totalMemorySize = SystemConfig.getMemorySize();

        int consecutiveFree = 0;
        int startIndex = -1;

        for (int i = startAddress; i < totalMemorySize; i++) {
            // Verificamos si la celda actual en la memoria está libre
            if (memory.getPosition(i) == null) {
                if (consecutiveFree == 0) {
                    startIndex = i; // marca el inicio si no había uno
                }
                consecutiveFree++;

                // si se encuentra bloque continuo con la cantidad de celdas necesarias
                if (consecutiveFree == size) {
                    return startIndex;
                }
            } else {
                // si se rompe la continuidad, reiniciamos el conteo
                consecutiveFree = 0;
                startIndex = -1;
            }
        }

        // no se encontró desde el puntero, se reinicia el conteo para dar la vuelta hasta donde habíamos empezado
        consecutiveFree = 0;
        startIndex = -1;

        if (type.equals("next")) {
            for (int i = userMemoryStart; i < totalMemorySize; i++) {
                if (memory.getPosition(i) == null) {
                    if (consecutiveFree == 0) {
                        startIndex = i; // marca el inicio si no había uno
                    }
                    consecutiveFree++;

                    // si se encuentra bloque continuo con la cantidad de celdas necesarias
                    if (consecutiveFree == size) {
                        return startIndex;
                    }
                } else {
                    // si se rompe la continuidad, reiniciamos el conteo
                    consecutiveFree = 0;
                    startIndex = -1;

                    // si ya dimos toda la vuelta, salir
                    if (i >= startAddress) {
                        break;
                    }
                }
            }
        }

        // Si después de recorrer toda la memoria no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre contiguo suficiente en la Memoria Principal. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInMemory_bestFit(int size, MainMemory memory) throws Exception {
        int userMemoryStart = SystemConfig.getUserMemoryStart();
        int startAddress = userMemoryStart;
        int totalMemorySize = SystemConfig.getMemorySize();

        int consecutiveFree = 0;
        int startIndex = -1;

        int bestFitIndex = -1;
        int bestFitSize = -1;

        for (int i = startAddress; i < totalMemorySize; i++) {
            // Verificamos si la celda actual en la memoria está libre
            if (memory.getPosition(i) == null) {
                if (consecutiveFree == 0) {
                    startIndex = i; // marca el inicio si no había uno
                }
                consecutiveFree++;
            } else {
                // si se rompe la continuidad, reiniciamos el conteo
                if ((bestFitIndex == -1 || consecutiveFree < bestFitSize) && consecutiveFree >= size) {
                    bestFitIndex = startIndex;
                    bestFitSize = consecutiveFree;
                }

                consecutiveFree = 0;
                startIndex = -1;
            }
        }

        if ((bestFitIndex == -1 || consecutiveFree < bestFitSize) && consecutiveFree >= size) {
            bestFitIndex = startIndex;
            bestFitSize = consecutiveFree;
        }

        if (bestFitIndex != -1) {
            return bestFitIndex;
        }

        // Si después de recorrer toda la memoria no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre contiguo suficiente en la Memoria Principal. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInMemory_fixed_equal(int size, MainMemory memory) throws Exception {
        int userMemoryStart = SystemConfig.getUserMemoryStart();
        int totalMemorySize = SystemConfig.getMemorySize();
        int partitionSize = SystemConfig.getPartitionFixedEqualSize();

        if (partitionSize < size) {
            throw new Exception("El proceso es más grande que las particiones de la Memoria Principal. " +
                            "Se requerían " + size + " celdas libres y las particiones son de " + partitionSize + " celdas.");
        }

        for (int i = userMemoryStart; (i+partitionSize) <= totalMemorySize; i+=partitionSize) {
            // Verificamos si la celda actual en la memoria está libre
            if (memory.getPosition(i) == null) {
                return i;
            }
        }

        throw new Exception("No hay espacio libre contiguo suficiente en las particiones de la Memoria Principal. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInMemory_fixed_different(int size, MainMemory memory) throws Exception {
        int userMemoryStart = SystemConfig.getUserMemoryStart();
        int totalMemorySize = SystemConfig.getMemorySize();
        int[] partitionsSize = SystemConfig.getPartitionFixedDifferentSize();

        int bestIndex = -1;
        int bestDiffSize = -1;

        int row = userMemoryStart;

        for (int i = 0; i < partitionsSize.length; i++) {
            if (row + partitionsSize[i] > totalMemorySize) {
                break;
            }

            // Verificamos si la celda actual en la memoria está libre
            if (memory.getPosition(row) == null && partitionsSize[i] >= size) {
                int diff = partitionsSize[i] - size;
                if (bestIndex == -1 || diff < bestDiffSize) {
                    bestIndex = row;
                    bestDiffSize = diff;
                }
            }
            row += partitionsSize[i];
        }

        if (bestIndex != -1) {
            return bestIndex;
        }

        throw new Exception("No hay espacio libre contiguo suficiente en las particiones de la Memoria Principal. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static ArrayList<Integer> getFreeSpaceInMemory_paging(int size, MainMemory memory) throws Exception {
        int userMemoryStart = SystemConfig.getUserMemoryStart();
        int totalMemorySize = SystemConfig.getMemorySize();
        int pagingSize = SystemConfig.getPagingSize();

        int missingSpace = size;
        ArrayList<Integer> framesIndexes = new ArrayList<>();

        int idx = 0;
        for (int i = userMemoryStart; (i+pagingSize) <= totalMemorySize; i+=pagingSize) {
            // Verificamos si la celda actual en la memoria está libre
            if (memory.getPosition(i) == null) {
                framesIndexes.add(idx);

                missingSpace-=pagingSize;
                if (missingSpace <= 0) {
                    return framesIndexes;
                }
            }

            idx++;
        }

        throw new Exception("No hay espacio libre suficiente en los marcos disponibles de la Memoria Principal. " +
                            "Se requerían " + size + " celdas libres, es decir, " + (int) Math.ceil((double) size/pagingSize) + " marcos libres.");
    }

    /**
     * Busca un bloque continuo de celdas libres en la Memoria Principal (RAM) a partir de la
     * dirección reservada para programas de usuario.
     * 
     * @param size Cantidad de instrucciones consecutivas que requiere el programa.
     * @param memory La Memoria Principal donde se buscará el espacio libre.
     * @return La dirección de memoria RAM donde inicia el espacio libre encontrado.
     * @throws Exception Si la memoria está llena o no hay un bloque contiguo del tamaño requerido.
     */
    public static int getFreeSpaceInMemory(int size, MainMemory memory) throws Exception {
        
        switch(SystemConfig.getMemoryStrategyAssignment()) {
            case "fixed_equal":
                return getFreeSpaceInMemory_fixed_equal(size, memory);
            case "fixed_different":
                return getFreeSpaceInMemory_fixed_different(size, memory);
            case "paging":
                ArrayList<Integer> framesIndexes = getFreeSpaceInMemory_paging(size, memory);
                Loader.setTemporalFrames(framesIndexes, false);
                return framesIndexes.get(0)*SystemConfig.getPagingSize()+SystemConfig.getUserMemoryStart();
            default: // dynamic
                switch(SystemConfig.getMemoryFit()) {
                    case "next":
                        return getFreeSpaceInMemory_first_next_Fit(size, memory, "next");
                    case "best":
                        return getFreeSpaceInMemory_bestFit(size, memory);
                    default:
                        return getFreeSpaceInMemory_first_next_Fit(size, memory, "first");
                }
        }

        // Si después de recorrer toda la memoria no se encontró un bloque contiguo suficiente
        //throw new Exception("No hay espacio libre contiguo suficiente en la Memoria Principal. " +
        //                    "Se requerían " + size + " celdas libres.");
    }

    
    /**
     * Busca un bloque continuo de celdas libres en la Memoria Principal (RAM) a partir de la
     * dirección reservada para programas de usuario.
     * 
     * @param size Cantidad de instrucciones consecutivas que requiere el programa.
     * @param memory La Memoria Principal donde se buscará el espacio libre.
     * @return La dirección de memoria RAM donde inicia el espacio libre encontrado.
     * @throws Exception Si la memoria está llena o no hay un bloque contiguo del tamaño requerido.
     */
    public static int getFreeSpaceInMemoryForPCB(MainMemory memory) throws Exception {
        int startAddress = SystemConfig.getPcbMemoryStart();
        int totalKernelSize = SystemConfig.getUserMemoryStart();

        int size = SystemConfig.PCB_SIZE;
        int consecutiveFree = 0;
        int startIndex = -1;

        for (int i = startAddress; i < totalKernelSize; i++) {
            // Verificamos si la celda actual en la memoria está libre
            if (memory.getPosition(i) == null) {
                if (consecutiveFree == 0) {
                    startIndex = i; // marca el inicio si no había uno
                }
                consecutiveFree++;

                // si se encuentra bloque continuo con la cantidad de celdas necesarias
                if (consecutiveFree == size) {
                    return startIndex;
                }
            } else {
                // si se rompe la continuidad, reiniciamos el conteo
                consecutiveFree = 0;
                startIndex = -1;
            }
        }

        // Si después de recorrer toda la memoria no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre contiguo suficiente en la Memoria Principal para el PCB. " +
                            "Se requerían " + size + " celdas libres.");
    }

    /**
     * Busca un espacio libre en el disco para almacenar un índice de archivo.
     * @param disk El disco donde se buscará el espacio libre.
     * @return La dirección de memoria del disco donde se puede almacenar el índice de archivo.
     * @throws Exception Si no hay espacio libre en el disco para almacenar el índice de archivo.
     */
    public static int getFreeSpaceInFileIndex(Disk disk) throws Exception {
        int startAddress = 0;
        int totalSpaceForIndex = SystemConfig.getStartDiskForPrograms();

        for (int i = startAddress; i < totalSpaceForIndex; i++) {
            // Verificamos si la celda actual en el disco está libre
            if (disk.getPosition(i) == null) {
                return i; // marca el inicio si no hatten uno
            }
        }
        
        // Si después de recorrer todo el disco no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre en el Disco para índices de archivos.");
    }

    public static int getFreeSpaceForJobInfo(MainMemory memory) throws Exception {
        int startAddress = 0;
        int totalSpaceForJobs = SystemConfig.getPcbMemoryStart(); //PENDIENTE

        int consecutiveFree = 0;
        for (int i = startAddress; i < totalSpaceForJobs; i++) {
            // Verificamos si la celda actual en el disco está libre
            if (memory.getPosition(i) == null) {
                consecutiveFree++;
            }
            if (consecutiveFree == 2) {
                return i-1; // marca el inicio si no tienen uno
            }
        }
        
        // Si después de recorrer todo el disco no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre en el Disco para trabajos.");
    }

    /* 
        SECCIÓN DE MEMORIA VIRTUAL:
    */

    public static int getFreeSpaceInVirtualMemory_first_next_Fit(int size, Disk disk, String type) throws Exception {
        int startAddress = SystemConfig.getDiskMemoryVirtualStart();
        int diskMemoryVirtualStart = startAddress;
        int totalVirtualMemorySize = SystemConfig.getDiskSize();

        if (type.equals("next")) {
            startAddress = Loader.getNextFitIndex(true);
        }

        int consecutiveFree = 0;
        int startIndex = -1;

        for (int i = startAddress; i < totalVirtualMemorySize; i++) {
            // verifica si la celda actual en la memoria virtual está libre
            if (disk.getPosition(i) == null) {
                if (consecutiveFree == 0) {
                    startIndex = i; // marca el inicio si no había uno
                }
                consecutiveFree++;

                // si se encuentra bloque continuo con la cantidad de celdas necesarias
                if (consecutiveFree == size) {
                    return startIndex;
                }
            } else {
                // si se rompe la continuidad, reiniciamos el conteo
                consecutiveFree = 0;
                startIndex = -1;
            }
        }

        // no se encontró desde el puntero, se reinicia el conteo para dar la vuelta hasta donde habíamos empezado
        consecutiveFree = 0;
        startIndex = -1;

        if (type.equals("next")) {
            for (int i = diskMemoryVirtualStart; i < totalVirtualMemorySize; i++) {
                if (disk.getPosition(i) == null) {
                    if (consecutiveFree == 0) {
                        startIndex = i; // marca el inicio si no había uno
                    }
                    consecutiveFree++;

                    // si se encuentra bloque continuo con la cantidad de celdas necesarias
                    if (consecutiveFree == size) {
                        return startIndex;
                    }
                } else {
                    // si se rompe la continuidad, reiniciamos el conteo
                    consecutiveFree = 0;
                    startIndex = -1;

                    // si ya dimos toda la vuelta, salir
                    if (i >= startAddress) {
                        break;
                    }
                }
            }
        }

        // Si después de recorrer toda la memoria virtual no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre contiguo suficiente en la Memoria Virtual. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInVirtualMemory_bestFit(int size, Disk disk) throws Exception {
        int startAddress = SystemConfig.getDiskMemoryVirtualStart();
        int diskMemoryVirtualStart = startAddress;
        int totalVirtualMemorySize = SystemConfig.getDiskSize();

        int consecutiveFree = 0;
        int startIndex = -1;

        int bestFitIndex = -1;
        int bestFitSize = -1;

        for (int i = startAddress; i < totalVirtualMemorySize; i++) {
            // Verificamos si la celda actual en la memoria está libre
            if (disk.getPosition(i) == null) {
                if (consecutiveFree == 0) {
                    startIndex = i; // marca el inicio si no había uno
                }
                consecutiveFree++;
            } else {
                // si se rompe la continuidad, reiniciamos el conteo
                if ((bestFitIndex == -1 || consecutiveFree < bestFitSize) && consecutiveFree >= size) {
                    bestFitIndex = startIndex;
                    bestFitSize = consecutiveFree;
                }

                consecutiveFree = 0;
                startIndex = -1;
            }
        }

        if ((bestFitIndex == -1 || consecutiveFree < bestFitSize) && consecutiveFree >= size) {
            bestFitIndex = startIndex;
            bestFitSize = consecutiveFree;
        }

        if (bestFitIndex != -1) {
            return bestFitIndex;
        }

        // Si después de recorrer toda la memoria no se encontró un bloque contiguo suficiente
        throw new Exception("No hay espacio libre contiguo suficiente en la Memoria Virtual. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInVirtualMemory_fixed_equal(int size, Disk disk) throws Exception {
        int diskMemoryVirtualStart = SystemConfig.getDiskMemoryVirtualStart();
        int totalVirtualMemorySize = SystemConfig.getDiskSize();
        int partitionSize = SystemConfig.getPartitionFixedEqualSize();

        if (partitionSize < size) {
            throw new Exception("El proceso es más grande que las particiones de la Memoria Virtual. " +
                            "Se requerían " + size + " celdas libres y las particiones son de " + partitionSize + " celdas.");
        }

        for (int i = diskMemoryVirtualStart; (i+partitionSize) <= totalVirtualMemorySize; i+=partitionSize) {
            // Verificamos si la celda actual en la memoria está libre
            if (disk.getPosition(i) == null) {
                return i;
            }
        }

        throw new Exception("No hay espacio libre contiguo suficiente en las particiones de la Memoria Virtual. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static int getFreeSpaceInVirtualMemory_fixed_different(int size, Disk disk) throws Exception {
        int diskMemoryVirtualStart = SystemConfig.getDiskMemoryVirtualStart();
        int totalVirtualMemorySize = SystemConfig.getDiskSize();
        int[] partitionsSize = SystemConfig.getPartitionFixedDifferentSize();

        int bestIndex = -1;
        int bestDiffSize = -1;

        int row = diskMemoryVirtualStart;

        for (int i = 0; i < partitionsSize.length; i++) {
            if (row + partitionsSize[i] > totalVirtualMemorySize) {
                break;
            }

            // Verificamos si la celda actual en la memoria está libre
            if (disk.getPosition(row) == null && partitionsSize[i] >= size) {
                int diff = partitionsSize[i] - size;
                if (bestIndex == -1 || diff < bestDiffSize) {
                    bestIndex = row;
                    bestDiffSize = diff;
                }
            }
            row += partitionsSize[i];
        }

        if (bestIndex != -1) {
            return bestIndex;
        }

        throw new Exception("No hay espacio libre contiguo suficiente en las particiones de la Memoria Virtual. " +
                            "Se requerían " + size + " celdas libres.");
    }

    private static ArrayList<Integer> getFreeSpaceInVirtualMemory_paging(int size, Disk disk) throws Exception {
        int diskMemoryVirtualStart = SystemConfig.getDiskMemoryVirtualStart();
        int totalVirtualMemorySize = SystemConfig.getDiskSize();
        int pagingSize = SystemConfig.getPagingSize();

        int missingSpace = size;
        ArrayList<Integer> framesIndexes = new ArrayList<>();

        int idx = 0;
        for (int i = diskMemoryVirtualStart; (i+pagingSize) <= totalVirtualMemorySize; i+=pagingSize) {
            // Verificamos si la celda actual en la memoria está libre
            if (disk.getPosition(i) == null) {
                framesIndexes.add(idx);

                missingSpace-=pagingSize;
                if (missingSpace <= 0) {
                    return framesIndexes;
                }
            }

            idx++;
        }

        throw new Exception("No hay espacio libre suficiente en los marcos disponibles de la Memoria Virtual. " +
                            "Se requerían " + size + " celdas libres, es decir, " + (int) Math.ceil((double) size/pagingSize) + " marcos libres.");
    }

    public static int getFreeSpaceInVirtualMemory(int size, Disk disk) throws Exception {
        
        switch(SystemConfig.getMemoryStrategyAssignment()) {
            case "fixed_equal":
                return getFreeSpaceInVirtualMemory_fixed_equal(size, disk);
            case "fixed_different":
                return getFreeSpaceInVirtualMemory_fixed_different(size, disk);
            case "paging":
                ArrayList<Integer> framesIndexes = getFreeSpaceInVirtualMemory_paging(size, disk);
                Loader.setTemporalFrames(framesIndexes, true);
                return framesIndexes.get(0)*SystemConfig.getPagingSize()+SystemConfig.getDiskMemoryVirtualStart();
            default: // dynamic
                switch(SystemConfig.getMemoryFit()) {
                    case "next":
                        return getFreeSpaceInVirtualMemory_first_next_Fit(size, disk, "next");
                    case "best":
                        return getFreeSpaceInVirtualMemory_bestFit(size, disk);
                    default:
                        return getFreeSpaceInVirtualMemory_first_next_Fit(size, disk, "first");
                }
        }
    }

    /**
     * Verifica si un proceso cabe en la Memoria Principal (RAM), dependiendo de la estrategia de asignación.
     * Sirve para descartar programas que nunca pueden ser cargados en la memoria principal por su tamaño.
     * @param size Cantidad de instrucciones consecutivas que requiere el programa.
     * @return Verdadero si cabe, falso si no.
     */
    public static boolean fitsInEmptyRAM(int size) {
        int userMemoryStart = SystemConfig.getUserMemoryStart();
        int totalMemorySize = SystemConfig.getMemorySize();
        int userSize = totalMemorySize - userMemoryStart;

        switch (SystemConfig.getMemoryStrategyAssignment()) {
            case "fixed_equal":
                return size <= SystemConfig.getPartitionFixedEqualSize() && SystemConfig.getPartitionFixedEqualSize() <= userSize;
            case "fixed_different":
                int row = userMemoryStart;
                for (int partition : SystemConfig.getPartitionFixedDifferentSize()) {
                    if (row + partition > totalMemorySize) break;
                    if (partition >= size) return true;
                    row += partition;
                }
                return false;
            case "paging":
                int pageSize = SystemConfig.getPagingSize();
                int frames = userSize / pageSize;
                return (int) Math.ceil((double) size / pageSize) <= frames;
            default: // dynamic
                return size <= userSize;
        }
    }
}