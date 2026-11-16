package minipcsimulator.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda el resultado de una ejecución: la configuración usada y las estadísticas de cada proceso.
 * Sirve para llevar el historial acumulado de ejecuciones.
 */
public class Stat {

    private int numEjecucion;

    // CPU
    private String algoritmo;
    private int cantidadCPU;
    private String parametrosAlgoritmo;

    // Memoria
    private String estrategiaMemoria;
    private String parametrosMemoria;

    // filas de la tabla de estadísticas
    private List<Object[]> filas;

    private LocalDateTime fecha;

    public Stat(int numEjecucion, String algoritmo, int cantidadCPU, String parametrosAlgoritmo, String estrategiaMemoria, String parametrosMemoria, List<Object[]> filas) {
        this.numEjecucion = numEjecucion;
        this.algoritmo = algoritmo;
        this.cantidadCPU = cantidadCPU;
        this.parametrosAlgoritmo = parametrosAlgoritmo;
        this.estrategiaMemoria = estrategiaMemoria;
        this.parametrosMemoria = parametrosMemoria;
        this.filas = new ArrayList<>(filas);
        this.fecha = LocalDateTime.now();
    }

    public int getNumEjecucion() {
        return numEjecucion;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public int getCantidadCPU() {
        return cantidadCPU;
    }

    public String getParametrosAlgoritmo() {
        return parametrosAlgoritmo;
    }

    public String getEstrategiaMemoria() {
        return estrategiaMemoria;
    }

    public String getParametrosMemoria() {
        return parametrosMemoria;
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return "Ejecución #" + numEjecucion + " | " + algoritmo + " (" + parametrosAlgoritmo + ") | CPU: " + cantidadCPU
                + " | Memoria: " + estrategiaMemoria + " (" + parametrosMemoria + ") | Procesos: " + filas.size();
    }
}