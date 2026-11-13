package minipcsimulator.model;

import java.util.ArrayList;
import minipcsimulator.utils.SystemConfig;

public class Job {
    private static int nextPID = SystemConfig.FIRST_PROCESS_ID;

    private String pathFile; //ruta
    private String nameFile;
    private ArrayList<ArrayList<String>> asmArray = new ArrayList<>(); //array de instrucciones
    private ArrayList<String> lines = new ArrayList<>(); // lineas originales del archivo .asm
 
    private int assignedPID;
    private int diskStartAddress;
    private int size;
    private boolean isLoadedInMemory = false;
    private int startAddressInRAM;
    private int assignedCpuId = 1;

    public Job(String pathFile, String nameFile, ArrayList<String> lines, ArrayList<ArrayList<String>> asmArray, int assignedCpuId) {
        this.pathFile = pathFile;
        this.nameFile = nameFile;
        this.lines = lines;
        this.asmArray = asmArray;
        this.size = asmArray.size();
        this.assignedPID = nextPID++;
        this.assignedCpuId = assignedCpuId;
    }

    public void setDiskStartAddress(int diskStartAddress) {
        this.diskStartAddress = diskStartAddress;
    }

    public void setStartAddressInRAM(int startAddressInRAM) {
        this.startAddressInRAM = startAddressInRAM;
    }

    public int getStartAddressInRAM() {
        return startAddressInRAM;
    }

    public int getDiskStartAddress() {
        return diskStartAddress;
    }

    public int getSize() {
        return size;
    }

    public int getAssignedPID() {
        if (assignedPID == 0) {
            assignedPID = nextPID++;
        }
        return assignedPID;
    }

    public int getAssignedCpuId() {
        return assignedCpuId;
    }

    public String getPathFile() {
        return pathFile;
    }

    public String getNameFile() {
        return nameFile;
    }

    public static void revertPID() {
        if (nextPID > SystemConfig.FIRST_PROCESS_ID) {
            nextPID--;
        }
    }

    public static void resetPIDs() {
        nextPID = SystemConfig.FIRST_PROCESS_ID;
    }
}
