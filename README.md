# Mini PC Simulator

<p align="left">
  <img src="https://res.cloudinary.com/dpuuo4mfh/image/upload/v1789428731/simulatorimgt1so2_r6cqry.png" width="1000" alt="Vista previa del simulador">
</p>

## Información del proyecto

**Estudiantes:** 

2024178835 - Johnsy Steven López Aguilar  
2023344598 - Sergio Montoya Badilla

**Curso:** Principios de Sistemas Operativos  

**Proyecto:** Proyecto #2 - Gestor de Procesos y Memoria

**Estado del proyecto:** 5 (Excelente)

**Enlace del video:** [Ver en YouTube](https://youtu.be/PWzR9dX3KOQ)


> [!IMPORTANT]
> Los objetivos alcanzados y no alcanzados se encuentran [aquí](#objetivos-alcanzados-y-no-alcanzados), la estrategia de seguridad se encuentra [aquí](#estrategia-de-seguridad), el diagrama de paquetes se encuentra [aquí](#diagrama-de-paquetes) y la explicación del diseño del sistema se encuentra ampliamente explicada en este README.

---

## Descripción

Mini PC Simulator es un simulador básico de un sistema operativo, desarrollado con Java, cuyo objetivo es representar de manera visual y práctica algunos de los conceptos fundamentales relacionados con el curso de Principios de Sistemas Operativos.

Este proyecto es la continuación del Proyecto #1 y permite observar el proceso que ocurre desde que un programa es cargado hasta que sus instrucciones son ejecutadas por uno o varios procesadores, aplicando algoritmos de planificación de la CPU y estrategias de asignación de memoria.

El simulador trabaja con programas escritos en un tipo de ensamblador mediante archivos `.asm`. Estos programas son analizados y validados antes de ser cargados en memoria. Posteriormente, las instrucciones válidas son traducidas a su representación interna y almacenadas en el disco, desde donde se cargan a la memoria RAM o a la memoria virtual según la estrategia de asignación seleccionada. Se pueden cargar varios programas a la vez, cada uno con su propio tiempo de llegada y prioridad.

Una vez cargados los programas, el usuario puede ejecutar las instrucciones utilizando dos modalidades:

- **Paso a paso:** permite observar individualmente cada segundo (tick) de la ejecución en todos los CPU.
- **Ejecutar todo:** procesa automáticamente las instrucciones, avanzando un segundo a la vez.

Durante la ejecución es posible observar el estado de los principales componentes del sistema, incluyendo la memoria RAM, el disco, la memoria virtual, los registros de cada CPU, el estado de cada proceso y la tabla de ejecución por núcleo.

---

## Objetivo

El objetivo principal del proyecto es construir una representación funcional y visual de una Mini PC que permita comprender cómo un sistema operativo orquesta sus recursos internos para ejecutar uno o varios programas.

En particular, el simulador busca representar:

- El almacenamiento de instrucciones en memoria principal.
- La lectura de instrucciones desde la memoria.
- El ciclo de búsqueda y ejecución por el CPU.
- El funcionamiento de registros del procesador.
- La administración de procesos mediante un PCB (Process Control Block).
- La función del Kernel como controlador del sistema.
- La carga de programas desde archivos externos.
- El manejo del disco y la memoria virtual.
- El manejo de interrupciones e I/O.
- La planificación de la CPU mediante distintos algoritmos.
- La ejecución en hasta 5 CPU simultáneamente.
- Los tiempos de llegada y el tamaño de ráfaga de cada proceso.
- Las estrategias de asignación de memoria física y virtual: paginación, particiones fijas y asignación dinámica.
- Las estadísticas de ejecución por proceso.

Nuestro proyecto no pretende ser una implementación de un sistema operativo real, sino una herramienta que simula el comportamiento de un sistema operativo y permite comprender conceptos fundamentales relacionados con el curso de Principios de Sistemas Operativos.

---

## Objetivos alcanzados y no alcanzados

### Alcanzados

- Carga de múltiples archivos `.asm` con detección de errores de sintaxis y de tamaño.
- Configuración del tamaño de la memoria principal, el Kernel, el disco y la memoria virtual.
- Configuración del algoritmo de planificación y sus parámetros.
- Algoritmos de planificación: FCFS, SRT, SPN, Round Robin, HRRN, Selfish Round Robin, Lottery, Por Prioridad, Guaranteed Scheduling y Multilevel Feedback Queue.
- Asignación y liberación de memoria con paginación, particiones fijas iguales, particiones fijas de diferente tamaño y asignación dinámica (First, Next y Best Fit), aplicadas tanto en la RAM como en la memoria virtual.
- Tiempos de llegada y prioridad asignables a cada proceso, con tiempo inicial cero.
- Ejecución automática (segundo a segundo) y manual.
- Hasta 5 CPU, cada uno con su propia cola de listos.
- Visualización del estado de los procesos y de los cambios de contexto en el BCP.
- Visualización de la ejecución de los procesos por núcleo y por segundo.
- Estadísticas al finalizar cada ejecución.

### No alcanzados

- Todos los objetivos se alcanzaron en este proyecto.

---

# Manual de Ejecución y Usuario

## Requisitos previos

Para ejecutar el proyecto se requiere:

- **JDK 11 o superior.**
- **Apache NetBeans**, únicamente si se desea ejecutar el proyecto desde el IDE.
- Sistema operativo compatible con Java.
- La biblioteca **FlatLaf 3.7.2**, incluida dentro del proyecto (se encuentra en `lib/flatlaf-3.7.2.jar`).

No es necesario instalar herramientas adicionales para utilizar la aplicación una vez que el proyecto ha sido descargado con todas sus dependencias.

---

## Descargar el proyecto

El repositorio puede clonarse mediante Git:

```bash
git clone https://github.com/johnsydev/p2-principios-sistemas-operativos-gestor-procesos-memoria.git
```

Después de clonar el repositorio, ingresar a la carpeta del proyecto:

```bash
cd p2-principios-sistemas-operativos-gestor-procesos-memoria
```

---

## Opción 1: Ejecutar desde NetBeans

1. Abrir **Apache NetBeans**.
2. Seleccionar **File → Open Project**.
3. Buscar la carpeta del proyecto clonado.
4. Abrir el proyecto.
5. Localizar la clase principal:

```text
MiniPCSimulator.java
```

6. Ejecutar el proyecto utilizando **F6** o la opción **Run Project**.

Al iniciar la aplicación se crea la ventana principal del simulador y se puede interactuar con ella.

---

## Opción 2: Ejecutar desde la línea de comandos

Ingresar a la carpeta donde se encuentra el proyecto:

```bash
cd Programa/MiniPCSimulator
```

### Compilar

En Windows PowerShell se puede utilizar:

```bash
./compilar
```

El comando compila las clases Java ubicadas dentro de `src` y coloca los archivos `.class` generados en ```build/classes```.

Además, se incluye la biblioteca FlatLaf necesaria para la interfaz gráfica.

### Ejecutar

Una vez compilado el proyecto:

```bash
./ejecutar
```

La aplicación debería abrir la ventana principal del simulador.

No cierre la consola hasta que termine de utilizar el simulador.

Los scripts de compilación y ejecución se encuentran en el archivo `compilar.bat` y `ejecutar.bat` dentro de la carpeta del proyecto.

---

# Guía de uso

## 1. Configurar el sistema

Al iniciar la aplicación, el usuario observa una ventana con todos los componentes visuales y sin ningún programa cargado. Inmediatamente se abrirá la ventana de **Configuración de Sistema**, que también puede abrirse con el botón **Configuración**.

La ventana está dividida en tres secciones:

* **Memoria:** tamaño de la RAM, espacio del Kernel, tamaño del disco y tamaño de la memoria virtual.
* **CPU:** cantidad de CPU, algoritmo de planificación y sus parámetros. Solo se habilitan los parámetros que utiliza el algoritmo seleccionado.
* **Asignación de memoria:** estrategia de asignación (Paginación, Partición fija iguales, Partición fija diferentes o Dinámica) y sus parámetros (ajuste, tamaño de página, tamaño de partición o lista de particiones). Debajo de los campos de particiones se muestra una nota indicando si sobran celdas o si los valores son inválidos.

Después de seleccionar los valores deseados, se debe presionar: **Guardar**

**Presionar el botón es necesario** para aplicar los cambios realizados, ya que **simula** la acción de cambiar la memoria principal física por otra de otro tamaño. Si se presiona **Cancelar**, se descartan los cambios.

La configuración puede editarse **únicamente cuando no haya un programa cargado**.

La configuración predeterminada es:

* **Memoria principal:** 512 celdas (definida en `config.json`).
* **Reservado para Kernel:** 128 celdas.
* **Tamaño del disco:** 1024 celdas (definido en `config.json`).
* **Tamaño de la memoria virtual:** 256 celdas.
* **CPU:** 1, con el algoritmo FCFS.
* **Asignación de memoria:** Dinámica con First Fit.

### Límites de configuración

- El tamaño de la memoria principal debe estar entre 128 y 65536 celdas.
- El espacio del Kernel debe estar entre el 25 % y el 75 % de la memoria principal (se redondea a par).
- El tamaño del disco debe estar entre 256 y 65536 celdas.
- El tamaño de la memoria virtual debe estar entre el 25 % y el 75 % del disco.
- La cantidad de CPU debe estar entre 1 y 5.
- El tamaño de partición y la suma de las particiones diferentes no pueden ser mayores que la memoria de usuario.
- En Selfish Round Robin, a no puede ser menor que b.

El Kernel guarda la información de los trabajos y los BCP de los procesos (22 celdas cada uno), por lo que su tamaño limita cuántos procesos pueden estar admitidos al mismo tiempo, con un máximo de 5.

---

## 2. Seleccionar los programas

El simulador trabaja con archivos de código fuente que utilizan la extensión:

```text
.asm
```

Para seleccionar los programas se utiliza el botón **Seleccionar .asm**, que abre el explorador de archivos y permite escoger varios archivos a la vez.

En la carpeta `Ejemplo` se encuentran ejemplos de programas válidos, que contienen la totalidad de instrucciones posibles para el simulador, se debe seguir esa sintaxis.

Al seleccionar los archivos ocurre una serie de procesos:
1. Se lee el contenido de cada archivo seleccionado (FileManager).
2. Se analizan sus líneas, descartando las vacías y validando la sintaxis de las instrucciones (AsmParser).
3. Se verifica que el programa no sea más grande que la memoria de usuario y que pueda caber en la RAM con la estrategia de asignación seleccionada. Si no cabe, se muestra un error y ese archivo no se agrega.
4. Los programas válidos quedan en espera de su tiempo de llegada (`ArrivalQueue`), con llegada 0 y prioridad 1 por defecto.

---

## 3. Asignar tiempos de llegada y prioridades

Con el botón **Tiempos de llegada** se abre una tabla con cada programa, su ráfaga (cantidad de líneas), su tiempo de llegada y su prioridad. Los dos últimos se pueden editar, y la tabla se reordena automáticamente por tiempo de llegada.

La prioridad se utiliza en los algoritmos Por Prioridad y Lottery, donde un número mayor representa mayor prioridad.

---

## 4. Ejecutar los programas

Al presionar **Paso a paso** o **Ejecutar todo** por primera vez, se muestra un resumen con los programas ordenados, su ráfaga, llegada, prioridad y el CPU asignado. Los CPU se asignan en orden, de forma circular (programa 1 al CPU 1, programa 2 al CPU 2, etc.). Desde este resumen se puede iniciar la simulación o volver a cambiar los valores.

En cada segundo (tick) de la simulación ocurre lo siguiente:

1. Los programas cuyo tiempo de llegada ya se cumplió se guardan en el disco, se registran en el Índice de Archivos y se crea su trabajo (`Job`) en estado `NEW` dentro de la `JobList`.
2. El planificador de largo plazo (`Scheduler`) admite los trabajos en orden de llegada, crea su BCP en el Kernel y los carga en la RAM (`READY`) o en la memoria virtual (`READY_SUSPENDED`) según el espacio disponible.
3. Cada CPU libre toma un proceso de su cola de listos según el algoritmo seleccionado. Si el algoritmo es expulsivo o se agotó el quantum, el proceso en ejecución puede volver a la cola.
4. Cada CPU ejecuta una instrucción de su proceso, ya que cada instrucción dura 1 segundo.
5. Se actualizan la RAM, el disco, la memoria virtual, la lista de procesos, los registros de cada CPU y la tabla de ejecución.

### Paso a paso

La opción **Paso a paso** permite avanzar un solo segundo, ejecutando una instrucción en cada CPU ocupado.

Esta modalidad resulta especialmente útil para observar cómo cambian:

* El `PC` (Program Counter) y el `IR` (Instruction Register) de cada CPU, cada uno en su propia pestaña.
* El acumulador `AC` y los registros generales `AX`, `BX`, `CX` y `DX`.
* El contenido de las celdas de la RAM y la memoria virtual, con la instrucción en ejecución resaltada con el color de su CPU.
* Las transiciones de estado en la lista de procesos y los cambios de contexto en el BCP.
* La tabla de ejecución, donde cada fila es un proceso de un núcleo y cada columna es un segundo.

### Ejecutar todo

La opción **Ejecutar todo** procesa automáticamente las instrucciones, avanzando un segundo a la vez. La ejecución se puede detener con el botón **Pausar**.

Al finalizar todos los procesos, el simulador muestra un panel de **Estadísticas** con la configuración utilizada y los tiempos de cada proceso.

---

# Documentación solicitada

## Estrategia de seguridad

### Protección de programas

Se implementaron validaciones para evitar que un programa pueda saltar a una dirección fuera de su espacio de memoria. Dada la arquitectura del proyecto, con implementar esta validación en el JMP es suficiente para que también se valide para JE y JNE. Los saltos se calculan con la dirección lógica del programa (número de instrucción), que luego se traduce a la dirección física con la dirección base o con la tabla de páginas del BCP, por lo que la validación funciona igual con cualquier estrategia de asignación de memoria. Cualquier intento de acceso fuera del programa emitiría el error, cerrando el programa con el estado EXIT y notificando el error en la consola, para posteriormente continuar con la ejecución de otros programas.

## Diagrama de paquetes

<p align="left">
  <img src="https://res.cloudinary.com/dpuuo4mfh/image/upload/v1791374705/diagramapaquetes_pfwyaq.png" width="1000" alt="Diagrama de paquetes del proyecto">
</p>

---

# Arquitectura del sistema

El proyecto utiliza una separación por responsabilidades entre los diferentes componentes. La lógica principal se encuentra distribuida entre el modelo de hardware simulado (CPU, RAM y Disco), el controlador que representa al Kernel, los servicios encargados de procesar programas y la interfaz gráfica.

## Estructura de archivos

```text
p2-principios-sistemas-operativos-gestor-procesos-memoria/
├── compilar.bat
├── ejecutar.bat
├── README.md
├── Ejemplo/
│   └── file.asm ... file7.asm
└── Programa/
    └── MiniPCSimulator/
        ├── build.xml
        ├── config.json
        ├── manifest.mf
        ├── build/          (Clases compiladas)
        ├── lib/
        │   └── flatlaf-3.7.2.jar
        ├── nbproject/      (Configuración de NetBeans)
        ├── src/
        │   └── minipcsimulator/
        │       ├── MiniPCSimulator.java
        │       ├── controller/
        │       │   └── MiniPCController.java
        │       ├── gui/
        │       │   └── VentanaPrincipal.java
        │       ├── model/
        │       │   ├── AlgorithmFactory.java
        │       │   ├── ArrivalQueue.java
        │       │   ├── CPU.java
        │       │   ├── Disk.java
        │       │   ├── Dispatcher.java
        │       │   ├── FCFSAlgorithm.java
        │       │   ├── FileIndex.java
        │       │   ├── FileSystem.java
        │       │   ├── GuaranteedAlgorithm.java
        │       │   ├── HRRNAlgorithm.java
        │       │   ├── Instruction.java
        │       │   ├── InterruptHandler.java
        │       │   ├── Job.java
        │       │   ├── JobList.java
        │       │   ├── Loader.java
        │       │   ├── LotteryAlgorithm.java
        │       │   ├── MainMemory.java
        │       │   ├── MemoryRegister.java
        │       │   ├── MLFQAlgorithm.java
        │       │   ├── PCB.java
        │       │   ├── PriorityAlgorithm.java
        │       │   ├── Process.java
        │       │   ├── ProcessList.java
        │       │   ├── RoundRobinAlgorithm.java
        │       │   ├── Scheduler.java
        │       │   ├── SchedulingAlgorithm.java
        │       │   ├── SPNAlgorithm.java
        │       │   ├── SRTAlgorithm.java
        │       │   └── SystemClock.java
        │       ├── services/
        │       │   ├── AsmParser.java
        │       │   └── FileManager.java
        │       └── utils/
        │           └── SystemConfig.java
        └── test/

```

## Funcionalidades de cada componente

| Componente | Capa | Responsabilidad |
| --- | --- | --- |
| `MiniPCSimulator.java` | Principal | Punto de entrada de la aplicación. Inicializa la interfaz gráfica y configura el tema visual utilizando FlatLaf. |
| `MiniPCController.java` | Controlador | Actúa como el Kernel principal del sistema. Coordina la llegada de programas, la cola de trabajos, el planificador, la ejecución de todos los CPU en cada segundo, las expulsiones, las interrupciones y los eventos de la interfaz gráfica. |
| `VentanaPrincipal.java` | Vista | Implementa la interfaz gráfica con Swing y FlatLaf. Permite configurar el sistema, cargar programas, asignar tiempos de llegada y visualizar el estado de la RAM, Disco, Memoria Virtual, cada CPU, la tabla de ejecución por núcleo y las estadísticas. |
| `CPU.java` | Modelo | Representa un procesador. Aplica el ciclo de búsqueda (*fetch*) y ejecución (*execute*), gestiona los registros (`AX`, `BX`, `CX`, `DX`, `AC`, `PC`, `IR`, `PSW`), la pila y la conversión entre direcciones lógicas y físicas. |
| `MainMemory.java` | Modelo | Representa la memoria RAM del sistema. Está dividida entre el área del Kernel (información de los trabajos y BCP) y el área de Usuario. |
| `Disk.java` | Modelo | Representa el almacenamiento secundario. Está dividido en áreas para el Índice de Archivos, el área de programas/archivos de usuario y la Memoria Virtual (área de Swap). |
| `MemoryRegister.java` | Modelo | Representa una celda individual de memoria (tanto en RAM como en Disco), permitiendo almacenar instrucciones, atributos del BCP o datos numéricos. |
| `Process.java` | Modelo | Representa un proceso activo en el sistema, vinculando sus instrucciones con la información del BCP, su CPU asignado, su tiempo de llegada, su ráfaga y su ciclo de vida. |
| `PCB.java` | Modelo | Representa el Bloque de Control de Proceso (BCP), almacenando el ID, estado, PC, dirección base o tabla de páginas, tamaño, registros guardados, pila, prioridad y métricas de tiempo. |
| `ProcessList.java` | Modelo | Administra la lista de procesos activos en el sistema que se encuentran en RAM o en Memoria Virtual, y libera su memoria al terminar. |
| `Job.java` | Modelo | Representa un trabajo que ha sido leído desde un archivo `.asm` y está listo para ser evaluado por el planificador. |
| `JobList.java` | Modelo | Gestiona la cola de trabajos pendientes (`NEW`) que esperan ser admitidos en el sistema. |
| `ArrivalQueue.java` | Modelo | Parte del simulador (no del SO). Guarda los programas seleccionados que todavía no han llegado y los entrega al Kernel cuando el reloj alcanza su tiempo de llegada. |
| `Scheduler.java` | Modelo | Implementa el planificador de largo y mediano plazo. Admite los trabajos en orden de llegada, decide si entran a la RAM o a la Memoria Virtual (`READY_SUSPENDED`), gestiona el swap cuando se libera RAM y mantiene una cola de listos por CPU. |
| `SchedulingAlgorithm.java` | Modelo | Interfaz que define cómo un algoritmo de planificación de corto plazo escoge el siguiente proceso, si es expulsivo y si usa quantum. |
| `AlgorithmFactory.java` | Modelo | Crea el algoritmo de planificación seleccionado en la configuración con sus parámetros. |
| `*Algorithm.java` | Modelo | Implementaciones de cada algoritmo de planificación: FCFS, SRT, SPN, Round Robin (también usado por Selfish RR), HRRN, Lottery, Por Prioridad, Guaranteed Scheduling y Multilevel Feedback Queue. |
| `Dispatcher.java` | Modelo | Encargado del cambio de contexto. Selecciona el siguiente proceso de la cola de un CPU según el algoritmo, le asigna el CPU y guarda el estado del proceso en su BCP. |
| `Loader.java` | Modelo | Copia el código fuente traducido al Disco, crea su entrada en el Índice de Archivos y realiza la carga de instrucciones hacia la RAM o la Memoria Virtual según la estrategia de asignación seleccionada. |
| `InterruptHandler.java` | Modelo | Atiende las interrupciones de software y de I/O (`INT 10H`, `INT 20H`, `INT 21H`), gestionando la salida en pantalla, la terminación de procesos y las llamadas al sistema de archivos. |
| `FileSystem.java` | Modelo | Modela el sistema de archivos sobre el Disco simulado. Permite crear, abrir, escribir, leer y cerrar archivos mediante llamadas al sistema. |
| `FileIndex.java` | Modelo | Gestiona la tabla de metadatos del Disco (índice de archivos), registrando nombres, tamaños y direcciones iniciales de los archivos almacenados. |
| `SystemClock.java` | Modelo | Simula el reloj del sistema, incrementando los *ticks* de tiempo en cada ciclo para medir la duración de los procesos. |
| `AsmParser.java` | Servicio | Analiza los archivos `.asm`, valida la sintaxis de las instrucciones tradicionales e interrupciones, verifica rangos y descarta líneas vacías. |
| `FileManager.java` | Servicio | Administra la lectura y selección de múltiples archivos desde el sistema operativo anfitrión. |
| `SystemConfig.java` | Utilidad | Centraliza los parámetros configurables del sistema, como el tamaño de la RAM, el área del Kernel, el Disco, la Memoria Virtual, la cantidad de CPU y la estrategia de asignación de memoria. |

---

# Funcionamiento interno

## Carga y Planificación de programas

El flujo comienza cuando el usuario selecciona uno o varios archivos `.asm`.

El `FileManager` lee los archivos y `AsmParser` los valida. Cada programa válido queda en la `ArrivalQueue` hasta que se cumple su tiempo de llegada. En ese momento se guarda en la sección de código del Disco simulado mediante el `Loader`, se registra en el Índice de Archivos (`FileIndex`) y se crea su trabajo (`Job`) en la `JobList` con el estado `NEW`.

En cada segundo entra en acción el `Scheduler` como planificador de largo plazo, admitiendo los trabajos en **orden de llegada**. Toma el primer trabajo de la cola, crea su BCP en el espacio reservado para el Kernel en la RAM y evalúa el espacio disponible en la RAM de usuario según la estrategia de asignación:

* **Si hay espacio suficiente en RAM:** El `Loader` copia las instrucciones a la RAM y el proceso pasa al estado `READY`, entrando a la cola de listos de su CPU.
* **Si la RAM no es suficiente pero hay espacio en Memoria Virtual (Swap):** Las instrucciones se guardan en el área de Swap del Disco y el proceso pasa al estado `READY_SUSPENDED`. Mientras esté suspendido no entra a la cola de ningún CPU.
* **Si no hay espacio en RAM ni en Memoria Virtual:** El trabajo permanece en la `JobList` esperando a que se libere espacio.

Mientras exista un proceso en `READY_SUSPENDED`, ningún trabajo nuevo puede ocupar la RAM antes que él, evitando que un proceso grande se quede esperando indefinidamente.

Cuando un proceso en ejecución termina y pasa al estado `EXIT`, libera su espacio en la RAM. El `Scheduler` detecta esto inmediatamente y realiza un proceso de **Swap-In**, tomando los procesos en estado `READY_SUSPENDED` en orden de admisión, trasladando sus instrucciones desde la Memoria Virtual a la RAM liberada, cambiando su estado a `READY` y agregándolos a la cola de su CPU.

---

## Planificación de la CPU

Cada CPU tiene su propia cola de listos. Cuando un CPU está libre, el `Dispatcher` escoge el siguiente proceso de su cola según el algoritmo seleccionado. En los algoritmos expulsivos, en cada segundo el proceso en ejecución vuelve a competir con los de la cola, y si otro es mejor, se guarda su contexto en el BCP y regresa a la cola.

| Algoritmo | Preemptive | Criterio |
| --- | --- | --- |
| FCFS | No | El primero que llegó a la cola. |
| SRT | Sí | El que tiene menor ráfaga restante. |
| SPN | No | El que tiene menor ráfaga. |
| Round Robin | Por quantum | Turnos en orden de llegada, cada uno con un quantum configurable. |
| HRRN | No | El de mayor tasa de respuesta: (espera + ráfaga) / ráfaga. |
| Selfish Round Robin | Por quantum | Los procesos nuevos esperan en una cola aparte y su prioridad aumenta a por segundo, mientras que la de los aceptados aumenta b. Un nuevo entra a la cola de aceptados cuando alcanza la prioridad de estos. |
| Lottery | Sí | Se sortea el CPU entre los procesos, con 10 boletos por cada nivel de prioridad. |
| Por Prioridad | No | El de mayor prioridad; en caso de empate, el que llegó primero. |
| Guaranteed Scheduling | Sí | El que tiene menor relación entre el tiempo de CPU recibido y el tiempo que le correspondía. |
| Multilevel Feedback Queue | Por quantum | Se escoge el de la cola de mayor prioridad; si agota su quantum y hay alguien más esperando, baja una cola. La cantidad de colas es configurable. |

La ráfaga de cada proceso corresponde a la cantidad de líneas de su archivo, y cada instrucción tarda 1 segundo en ejecutarse.

---

## Asignación de memoria

La estrategia de asignación seleccionada se aplica tanto en la RAM de usuario como en la Memoria Virtual:

* **Paginación:** la memoria se divide en marcos del tamaño de página configurado. Un proceso ocupa los marcos libres que necesite, aunque no sean contiguos, y su tabla de páginas se guarda en el BCP. El `PC` sigue siendo la celda real de la RAM, y el CPU convierte entre dirección lógica y física con la tabla de páginas.
* **Partición fija (iguales):** la memoria se divide en particiones del mismo tamaño, y cada proceso ocupa una partición completa.
* **Partición fija (diferentes):** la memoria se divide según la lista de tamaños configurada, y cada proceso ocupa la partición libre más pequeña en la que cabe.
* **Dinámica:** el proceso ocupa un bloque contiguo del tamaño exacto, buscando el primer bloque libre (First Fit), el siguiente desde la última asignación (Next Fit) o el más pequeño en el que cabe (Best Fit).

En la memoria virtual el intercambio se realiza por proceso completo: todas las páginas o celdas de un proceso están en la RAM o en la Memoria Virtual, por lo que el estado `READY_SUSPENDED` indica dónde se encuentran.

Para facilitar su visualización, en las tablas de la RAM y la Memoria Virtual cada partición o marco se pinta con un color distinto, y las celdas sobrantes que no completan una partición o marco se pintan de gris.

Cuando un proceso termina, se liberan sus celdas o marcos en la RAM y su BCP en el Kernel.

---

## Ciclo de ejecución e Interrupciones

La ejecución se basa en el ciclo fundamental de:

```text
FETCH -> EXECUTE

```

En cada segundo, todos los CPU ocupados ejecutan una instrucción de su proceso.

Durante el **FETCH**, la CPU utiliza el `PC` del proceso activo para leer la instrucción directamente desde la RAM.

Durante el **EXECUTE**, el procesador interpreta la instrucción y ejecuta la operación. Si la instrucción modifica registros (`AX`, `BX`, `CX`, `DX`, `AC`), banderas (`PSW`) o la pila, los cambios se reflejan inmediatamente en el procesador.

Si la CPU encuentra una instrucción de interrupción (`INT`), delega la atención al `InterruptHandler`:

* **`INT 10H` (Salida en pantalla):** Muestra el valor almacenado en los registros directamente en la consola visual de la GUI, sin necesidad de presionar ENTER para continuar.
* **`INT 20H` (Terminar programa):** Finaliza el proceso, libera su memoria, registra su tiempo final y marca su estado como `EXIT`.
* **`INT 21H` (Sistema de Archivos):** Permite interactuar con el `FileSystem` para crear (`AH=3CH`), abrir (`AH=3DH`), escribir (`AH=40H`), leer (`AH=41H`) o cerrar (`AH=4DH`) archivos en el disco simulado.

En este proyecto ya no se utiliza la interrupción de entrada desde el teclado.

---

# Memoria y Disco

El sistema organiza la memoria simulada en dos estructuras físicas separadas: **RAM** y **Disco**, ambas estructuradas mediante objetos `MemoryRegister` para almacenar tanto instrucciones como datos.

### Memoria RAM (`MainMemory`)

Se divide en dos áreas principales:

* **Espacio del Kernel:** Reservado para almacenar la información de los trabajos y los BCP (PCB) de los procesos admitidos en el sistema.
* **Espacio de Usuario:** Disponible para cargar las instrucciones de los programas que están listos para ejecutarse (`READY` o `RUNNING`), organizado según la estrategia de asignación.

### Disco (`Disk`)

Se divide conceptualmente en tres zonas:

* **Índice de archivos (File Index):** Almacena la tabla con la lista de archivos guardados en el disco, sus tamaños y sus direcciones físicas de inicio.
* **Área de archivos y programas:** Almacena las instrucciones originales de los programas cargados y los archivos creados dinámicamente mediante la interrupción `INT 21H`.
* **Memoria Virtual (Área de Swap):** Espacio reservado para almacenar el código de los procesos en estado `READY_SUSPENDED` que no cupieron en la RAM al momento de ser admitidos, organizado con la misma estrategia de asignación que la RAM.

---

# Registros y CPU

El simulador cuenta con hasta 5 procesadores, cada uno con los siguientes elementos de hardware:

| Registro | Función |
| --- | --- |
| `AX` | Registro general A. Utilizado para operaciones aritméticas y para pasar códigos de función en interrupciones. |
| `BX` | Registro general B. Utilizado para almacenar valores o direcciones auxiliares. |
| `CX` | Registro general C. Utilizado como contador o registro de propósito general. |
| `DX` | Registro general D. Utilizado en operaciones de datos e I/O. |
| `AC` | Acumulador. Almacena los resultados de las operaciones procesadas por la ALU. |
| `PC` | Program Counter. Indica la dirección de memoria de la siguiente instrucción a ejecutar. |
| `IR` | Instruction Register. Guarda la instrucción actual que está siendo procesada. |
| `PSW` | Program Status Word. Guarda las banderas del sistema (como la bandera de Cero `Z` para saltos condicionales). |
| `Pila` | Pila del proceso para operaciones `PUSH` y `POP`. |

Cada CPU tiene un color que lo identifica en su pestaña, en la tabla de ejecución y en la fila de la RAM que está ejecutando.

---

# Proceso, BCP y Estadísticas

Cada programa admitido en el sistema es gestionado a través de su **BCP (Bloque de Control de Proceso)**.

El ciclo de vida de un proceso pasa por los siguientes estados:

* **NEW:** El archivo fue leído y está en la `JobList` esperando a ser evaluado por el planificador.
* **READY:** El proceso está cargado en la memoria RAM y espera su turno en la cola de su CPU.
* **READY_SUSPENDED:** El proceso fue admitido, pero sus instrucciones se guardaron en la Memoria Virtual a la espera de espacio libre en RAM.
* **RUNNING:** El proceso tiene el control de su CPU y sus instrucciones están siendo ejecutadas.
* **BLOCKED:** El proceso se pausó mientras se atiende una operación de E/S.
* **BLOCKED_SUSPENDED:** El proceso se pausó a la espera de una operación de E/S y sus instrucciones se guardaron en la Memoria Virtual.
* **EXIT:** El proceso terminó su ejecución, liberó su espacio en memoria y guardó sus estadísticas finales.

### Almacenamiento del BCP en RAM

Los atributos de cada BCP se escriben físicamente dentro de las celdas del área del Kernel en la RAM simulada. Cada campo (ID, estado, PC, registros generales, pila, CPU, tiempos, siguiente BCP, dirección base, tamaño y prioridad) ocupa una celda de memoria, para un total de 22 celdas por BCP. Cuando se utiliza paginación, la celda de la dirección base guarda la tabla de páginas completa (`bcp_pages_table`), indicando los marcos de la RAM o de la Memoria Virtual donde se encuentra el proceso. Esto permite observar visualmente cómo el Kernel guarda el estado administrativo de cada proceso y cada cambio de contexto.

### Registro de Estadísticas

A través del `SystemClock`, el sistema registra el tiempo (en *ticks*) en que cada proceso realiza sus transiciones. Al finalizar la ejecución de todos los procesos, el simulador muestra la configuración utilizada (algoritmo, CPU, parámetros y estrategia de memoria) y, por cada proceso, su tiempo de inicio, tiempo final, tiempo ocioso, tiempo de estancia (*Turnaround*, tiempo final - tiempo de llegada) y la relación Tr / Ts. Los cálculos siguen las definiciones de Stallings.

---

# Validación de programas

Antes de almacenar un programa en el Disco, el archivo `.asm` es validado por la clase `AsmParser`.

Entre las comprobaciones realizadas se incluyen:

* Sintaxis correcta de las instrucciones tradicionales (`MOV`, `ADD`, `SUB`, `JMP`, `CMP`, etc.).
* Validación de las nuevas instrucciones e interrupciones (`INT 10H`, `INT 20H`, `INT 21H`, `PUSH`, `POP`, etc.).
* Verificación del número y tipo de operandos permitidos (registros, constantes o direcciones).
* Verificación de rangos numéricos permitidos para evitar valores fuera de límite.
* Eliminación y descarte automático de líneas vacías o comentarios.
* Verificación de que el programa pueda caber en la RAM vacía con la estrategia de asignación seleccionada, ya que de lo contrario nunca podría ejecutarse.

Descartar las líneas vacías antes de guardar el programa evita reservar espacio innecesario en el Disco y en la RAM, garantizando que cada celda de memoria contenga únicamente instrucciones válidas y procesables.

---

# Manejo de Overflow

El simulador representa el comportamiento de una arquitectura basada en un rango limitado de bits.

Por esta razón, el sistema no bloquea por software una operación cuando el resultado sobrepasa el límite permitido, sino que permite que el valor se comporte según las limitaciones del desbordamiento físico. Esto permite observar cómo afecta el *overflow* a los registros durante las operaciones aritméticas.

---

# Decisiones de diseño

## Interfaz gráfica

Se utilizó **Swing** junto a la librería **FlatLaf** con un tema oscuro moderno. La interfaz fue estructurada en paneles separados para observar simultáneamente la RAM, el Disco, la Memoria Virtual, los registros de cada CPU, la lista de procesos, la consola y la tabla de ejecución, facilitando la comprensión del flujo sin depender de una consola. Se decidió no utilizar una tabla aparte para la asignación de memoria, sino resaltar en la misma RAM la fila que ejecuta cada CPU con su color y pintar cada partición o marco con un color distinto.

## Planificador de largo plazo y Lista de Trabajos

Se implementó una `JobList` previa a la carga en memoria para modelar adecuadamente la admisión de procesos. El `Scheduler` admite los trabajos en estricto orden de llegada, independientemente del algoritmo de planificación de la CPU, ya que este último solo decide entre los procesos que ya están en la RAM. La admisión es global para todos los CPU porque la memoria es compartida, por lo que un CPU puede quedar ocioso mientras un proceso espera memoria.

## Algoritmos de planificación

Cada algoritmo se implementó como una clase que cumple la interfaz `SchedulingAlgorithm`, de forma que el Kernel siempre ejecuta el mismo flujo y solo cambia el criterio para escoger el siguiente proceso, si el algoritmo es expulsivo y si utiliza quantum. Los procesos se asignan a los CPU de forma circular según el orden de llegada, y cada CPU administra su propia cola de listos.

## Memoria Virtual y Swapping

Para manejar el exceso de procesos sin rechazarlos inmediatamente, se asignó una sección dedicada del Disco como Memoria Virtual, organizada con la misma estrategia de asignación que la RAM. Se optó por intercambiar procesos completos en lugar de paginación por demanda, por lo que no existen fallos de página ni algoritmos de reemplazo, y el estado `READY_SUSPENDED` cumple la función del bit para saber en dónde se encuentra el proceso.

## Tabla de páginas en el BCP

Para mantener la información de paginación dentro de la RAM sin gastar una celda del Kernel por cada página, la tabla de páginas completa se guarda en una sola celda del BCP, en la posición de la dirección base.

## Sistema de Archivos simulado

Se diseñó la clase `FileSystem` respaldada por `FileIndex` para simular la gestión de archivos en Disco. A través de la interrupción `INT 21H`, los programas en ensamblador pueden crear y manipular archivos dentro del propio entorno del simulador, conectando los conceptos de I/O con la tabla de metadatos del disco. Se asume, por lo que se habló en clases, que los archivos solo tienen números dentro de los límites definidos, y que no se contemplarán errores de flujo, por ejemplo, la apertura de archivos inexistentes.

## Separación de responsabilidades

El proyecto sigue el patrón MVC (Modelo-Vista-Controlador). El modelo administra el hardware y las estructuras del SO (`CPU`, `MainMemory`, `Disk`, `PCB`, `Scheduler`, algoritmos), los servicios procesan los archivos externos (`AsmParser`, `FileManager`), el controlador coordina las interacciones del Kernel y la vista muestra visualmente todos los cambios en tiempo real.

---

# Consideraciones y limitaciones

El simulador está diseñado con fines estrictamente académicos, por lo que abstrae ciertas características de un sistema real:

* La arquitectura trabaja con valores y registros con rangos numéricos limitados.
* Cada proceso se asigna a un CPU al iniciar la simulación y no cambia de CPU durante su ejecución.
* Se admiten como máximo 5 procesos al mismo tiempo (en RAM o en Memoria Virtual), y esa cantidad también está limitada por el espacio del Kernel para los BCP.
* La Memoria Virtual intercambia procesos completos, sin paginación por demanda ni algoritmos de reemplazo de páginas.
* El sistema de archivos es de estructura plana (no soporta jerarquía de carpetas/directorios).
* Las interrupciones son simuladas mediante software y atendidas secuencialmente por el Kernel.

---

# Tecnologías utilizadas

* **Java (JDK 11+)**
* **Java Swing**
* **FlatLaf 3.7.2**
* **Apache NetBeans / IDE Java**
* **Git / GitHub**

La aplicación se ejecuta de manera local y no requiere dependencias ni servicios externos adicionales.

---

# Conclusión

Esta versión de Mini PC Simulator integra la planificación de la CPU con diez algoritmos distintos, la ejecución en varios núcleos, las estrategias de asignación de memoria física y virtual, la gestión de almacenamiento secundario en disco, la atención de interrupciones de I/O y la interacción con un sistema de archivos simple.

La posibilidad de observar en tiempo real cómo cada núcleo ejecuta sus procesos, cómo se reparten la RAM y la memoria virtual según la estrategia seleccionada, cómo se guardan los BCP en el área del Kernel y cómo se registran las estadísticas de ejecución convierte a este simulador en una herramienta práctica y clara para comprender los conceptos del curso de Principios de Sistemas Operativos.

---

*JohnsyDev, 2026*