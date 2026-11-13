package minipcsimulator.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase que contiene la configuración del sistema.
 */
public class SystemConfig {

    // VALORES POR DEFECTO, EDITABLES
    private static int memorySize = 256;
    private static int userMemoryStart;

    //p2
    private static int numCPUs = 1;
    private static String algorithm = "FCFS";
    private static int quantum = 1;

    private static int diskSize = 512;
    private static int diskMemoryVirtualSize;
    private static int diskFileIndexSize;

    private static int jobListCapacity = 8;
    private static int jobListMemorySize;

    // VALORES MÍNIMOS Y MÁXIMOS
    public static final int MEMORY_SIZE_MIN = 128;
    public static final int MEMORY_SIZE_MAX = 65536;

    public static final int DISK_SIZE_MIN = 256;
    public static final int DISK_SIZE_MAX = 65536;

    public static final int NUM_CPUS_MIN = 1;
    public static final int NUM_CPUS_MAX = 5;

    //public static final int USER_MEMORY_START_MIN = 16;

    // POSICIONES BASE
    public static final int KERNEL_MEMORY_START = 0;

    // TAMAÑOS DEL SISTEMA
    public static final int STACK_SIZE = 5;
    public static final int REGISTERS_COUNT = 4;
    public static final int PCB_SIZE = 22;

    public static final int FIRST_PROCESS_ID = 101;
    public static final int MAX_PROCESSES_IN_MEMORY = 5;

    // PORCENTAJES
    public static final double MIN_KERNEL_MEMORY_PERCENTAGE = 0.25;
    public static final double MAX_KERNEL_MEMORY_PERCENTAGE = 0.75;

    public static final double KERNEL_JOB_LIST_PERCENTAGE = 0.15;

    public static final double MIN_VIRTUAL_MEMORY_PERCENTAGE = 0.25;
    public static final double MAX_VIRTUAL_MEMORY_PERCENTAGE = 0.75;

    public static final double MIN_DISK_FILE_INDEX_PERCENTAGE = 0.05;

    static {
        recalculateCalculatedValues();
        loadFromJSON("config.json");
    }

    /**
     * Carga la configuración desde un archivo JSON.
     */
    public static void loadFromJSON(String filePath) {
        File configFile = new File(filePath);

        if (!configFile.exists()) {
            System.out.println(
                "Archivo de configuración JSON no encontrado ("
                + filePath
                + "). Usando valores predeterminados."
            );
            return;
        }

        try {
            String content = new String(
                Files.readAllBytes(Paths.get(filePath))
            );

            Integer parsedMemorySize =
                parseJsonInt(content, "memorySize");

            Integer parsedDiskSize =
                parseJsonInt(content, "diskSize");

            // Validar primero los valores recibidos.
            if (parsedMemorySize != null
                    && (parsedMemorySize < MEMORY_SIZE_MIN
                    || parsedMemorySize > MEMORY_SIZE_MAX)) {
                throw new IllegalArgumentException(
                    "El tamaño de memoria debe estar entre "
                    + MEMORY_SIZE_MIN
                    + " y "
                    + MEMORY_SIZE_MAX
                );
            }

            if (parsedDiskSize != null
                    && (parsedDiskSize < DISK_SIZE_MIN
                    || parsedDiskSize > DISK_SIZE_MAX)) {
                throw new IllegalArgumentException(
                    "El tamaño de disco debe estar entre "
                    + DISK_SIZE_MIN
                    + " y "
                    + DISK_SIZE_MAX
                );
            }

            if (parsedMemorySize != null) {
                memorySize = parsedMemorySize;
            }

            if (parsedDiskSize != null) {
                diskSize = parsedDiskSize;
            }

            recalculateCalculatedValues();

            System.out.println(
                "Configuración cargada exitosamente desde "
                + filePath
            );

        } catch (Exception e) {
            System.err.println(
                "Error al leer el archivo JSON de configuración: "
                + e.getMessage()
            );

            // Restaurar valores predeterminados.
            memorySize = 256;
            diskSize = 512;

            recalculateCalculatedValues();
        }
    }

    /**
     * Obtiene un entero del archivo JSON.
     */
    private static Integer parseJsonInt(String json, String key) {
        Pattern pattern = Pattern.compile(
            "\"" + key + "\"\\s*:\\s*(\\d+)"
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    /**
     * Recalcula los valores que dependen del tamaño de RAM y disco.
     */
    private static void recalculateCalculatedValues() {

        // RAM: tamaño del kernel.
        userMemoryStart = getMinKernelSize();


        jobListMemorySize = (int) Math.ceil(
            userMemoryStart * KERNEL_JOB_LIST_PERCENTAGE
        );

        if (jobListMemorySize % 2 != 0) {
            jobListMemorySize++;
        }

        // Disco: tamaño del índice de archivos.
        diskFileIndexSize = (int) Math.ceil(
            diskSize * MIN_DISK_FILE_INDEX_PERCENTAGE
        );

        if (diskFileIndexSize < 2) {
            diskFileIndexSize = 2;
        }

        // Disco: memoria virtual.
        diskMemoryVirtualSize = getMinVirtualSize();
    }

    // RAM

    /**
     * Obtiene el tamaño mínimo del kernel.
     */
    public static int getMinKernelSize() {
        return (int) Math.ceil(
            memorySize * MIN_KERNEL_MEMORY_PERCENTAGE
        );
    }

    /**
     * Obtiene el tamaño máximo permitido para el kernel.
     */
    public static int getMaxKernelSize() {
        return (int) Math.floor(
            memorySize * MAX_KERNEL_MEMORY_PERCENTAGE
        );
    }

    /**
     * Obtiene el inicio de la región de los PCB.
     */
    public static int getPcbMemoryStart() {
        return KERNEL_MEMORY_START + jobListMemorySize;
    }

    /**
     * Obtiene el inicio de la memoria de usuario.
     */
    public static int getUserMemoryStart() {
        return userMemoryStart;
    }

    /**
     * Obtiene el tamaño total de la memoria RAM.
     */
    public static int getMemorySize() {
        return memorySize;
    }

    /**
     * Obtiene el tamaño disponible para los procesos de usuario.
     */
    public static int getUserMemorySize() {
        return memorySize - userMemoryStart;
    }

    /**
     * Establece el tamaño de la RAM.
     */
    public static void setMemorySize(int memorySize) {
        if (memorySize < MEMORY_SIZE_MIN
                || memorySize > MEMORY_SIZE_MAX) {
            throw new IllegalArgumentException(
                "El tamaño de memoria debe estar entre "
                + MEMORY_SIZE_MIN
                + " y "
                + MEMORY_SIZE_MAX
            );
        }

        SystemConfig.memorySize = memorySize;
        recalculateCalculatedValues();
    }

    // =========================================================
    // JOB LIST
    // =========================================================

    /**
     * Obtiene la cantidad de trabajos que puede almacenar el JobList.
     */
    public static int getJobListCapacity() {
        return jobListCapacity;
    }

    /**
     * Establece la capacidad del JobList.
     */
    public static void setJobListCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(
                "La capacidad del JobList debe ser mayor que cero."
            );
        }

        jobListCapacity = capacity;
    }

    /**
     * Obtiene el tamaño reservado para el JobList en memoria.
     */
    public static int getJobListMemorySize() {
        return jobListMemorySize;
    }

    // =========================================================
    // DISCO
    // =========================================================

    /**
     * Obtiene el tamaño total del disco.
     */
    public static int getDiskSize() {
        return diskSize;
    }

    /**
     * Establece el tamaño total del disco.
     */
    public static void setDiskSize(int diskSize) {
        if (diskSize < DISK_SIZE_MIN
                || diskSize > DISK_SIZE_MAX) {
            throw new IllegalArgumentException(
                "El tamaño de disco debe estar entre "
                + DISK_SIZE_MIN
                + " y "
                + DISK_SIZE_MAX
            );
        }

        SystemConfig.diskSize = diskSize;
        recalculateCalculatedValues();
    }

    /**
     * Obtiene el tamaño del índice de archivos.
     */
    public static int getDiskFileIndexSize() {
        return diskFileIndexSize;
    }

    /**
     * Obtiene el inicio de la región de programas en disco.
     */
    public static int getStartDiskForPrograms() {
        return diskFileIndexSize;
    }

    /**
     * Obtiene el tamaño de la memoria virtual.
     */
    public static int getDiskMemoryVirtualSize() {
        return diskMemoryVirtualSize;
    }

    /**
     * Establece el tamaño de la memoria virtual.
     */
    public static void setDiskMemoryVirtualSize(
            int diskMemoryVirtualSize) {

        int minSize = 32;
        int maxSize = diskSize - diskFileIndexSize - 1;

        if (diskMemoryVirtualSize < minSize
                || diskMemoryVirtualSize > maxSize) {
            throw new IllegalArgumentException(
                "El tamaño de memoria virtual debe estar entre "
                + minSize
                + " y "
                + maxSize
            );
        }

        SystemConfig.diskMemoryVirtualSize =
            diskMemoryVirtualSize;
    }

    public static void setUserMemoryStart(int userMemoryStart) {
        if (userMemoryStart < getMinKernelSize()
                || userMemoryStart > getMaxKernelSize()) {
            throw new IllegalArgumentException(
                "El inicio de la memoria de usuario debe estar entre "
                + getMinKernelSize()
                + " y "
                + getMaxKernelSize()
            );
        }

        if (userMemoryStart % 2 != 0) {
            userMemoryStart++;
        }
        SystemConfig.userMemoryStart = userMemoryStart;
    }

    /**
     * Obtiene la posición inicial de la memoria virtual.
     */
    public static int getDiskMemoryVirtualStart() {
        return diskSize - diskMemoryVirtualSize;
    }

    /**
     * Obtiene el tamaño mínimo de memoria virtual.
     */
    public static int getMinVirtualSize() {
        return (int) Math.ceil(
            diskSize * MIN_VIRTUAL_MEMORY_PERCENTAGE
        );
    }

    /**
     * Obtiene el tamaño máximo permitido para la memoria virtual.
     */
    public static int getMaxVirtualSize() {
        return (int) Math.floor(
            diskSize * MAX_VIRTUAL_MEMORY_PERCENTAGE
        );
    }

    public static int getNumCPUs() {
        return numCPUs;
    }

    public static void setNumCPUs(int numCPUs) {
        if (numCPUs < NUM_CPUS_MIN
                || numCPUs > NUM_CPUS_MAX) {
            throw new IllegalArgumentException(
                "El número de CPUS debe estar entre "
                + NUM_CPUS_MIN
                + " y "
                + NUM_CPUS_MAX
            );
        }
        SystemConfig.numCPUs = numCPUs;
    }
}