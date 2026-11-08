# Mini PC Simulator

<p align="left">
  <img src="https://res.cloudinary.com/dpuuo4mfh/image/upload/v1789428731/simulatorimgt1so2_r6cqry.png" width="1000" alt="Vista previa del simulador">
</p>

## Información del proyecto

**Estudiantes:** 

2024178835 - Johnsy Steven López Aguilar  
2023344598 - Sergio Montoya Badilla

**Curso:** Principios de Sistemas Operativos  

**Estado del proyecto:** 1 (Completo)

**Enlace del video:** [Ver en YouTube](https://youtu.be/PWzR9dX3KOQ)


> [!IMPORTANT]
> La estrategia de seguridad se encuentra [aquí](#estrategia-de-seguridad), el diagrama de paquetes se encuentra [aquí](#diagrama-de-paquetes) y la explicación del diseño del sistema se encuentra ampliamente explicada en este README.

---

## Descripción

Mini PC Simulator es un simulador básico de un sistema operativo, desarrollado con Java, cuyo objetivo es representar de manera visual y práctica algunos de los conceptos fundamentales relacionados con el curso de Principios de Sistemas Operativos.

Esta tarea permite observar el proceso que ocurre desde que un programa es cargado hasta que sus instrucciones son ejecutadas por el procesador.

El simulador trabaja con programas escritos en un tipo de ensamblador mediante archivos `.asm`. Estos programas son analizados y validados antes de ser cargados en memoria. Posteriormente, las instrucciones válidas son traducidas a su representación interna y almacenadas en la memoria RAM simulada. De momento, solo se puede cargar un programa a la vez.

Una vez cargado el programa, el usuario puede ejecutar las instrucciones utilizando dos modalidades:

- **Paso a paso:** permite observar individualmente cada ciclo de ejecución de cada instrucción.
- **Ejecutar todo:** procesa automáticamente todas las instrucciones del programa.

Durante la ejecución es posible observar el estado de los principales componentes del sistema, incluyendo la memoria, los registros del CPU y el estado del proceso.

---

## Objetivo

El objetivo principal de la tarea es construir una representación funcional y visual de una Mini PC que permita comprender cómo interactúan diferentes componentes de un sistema computacional.

En particular, el simulador busca representar:

- El almacenamiento de instrucciones en memoria principal.
- La lectura de instrucciones desde la memoria.
- El ciclo de búsqueda y ejecución por el CPU.
- El funcionamiento de registros del procesador.
- La administración básica de un proceso mediante un PCB (Process Control Block).
- La función del Kernel como controlador del sistema.
- La carga de programas desde archivos externos.arios.
- El manejo de memoria disponible para los programas.
- El manejo del disco y memoria virtual básico.
- El manejo del planificador de procesos simple.
- El manejo de interrupciones e I/O.

Esta tarea no pretende ser una implementación de un sistema operativo real, sino una herramienta que simula el comportamiento de un sistema operativo y permite comprender conceptos fundamentales relacionados con el curso de Principios de Sistemas Operativos.

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
git clone https://github.com/johnsydev/p1-principios-sistemas-operativos-gestor-procesos.git
````

Después de clonar el repositorio, ingresar a la carpeta del proyecto:

```bash
cd p1-principios-sistemas-operativos-gestor-procesos
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

## 1. Configurar la memoria

Al iniciar la aplicación, el usuario observa una ventana con todos los componentes visuales y sin ningún programa cargado. Inmediatamente se abrirá la ventana de configuración del sistema.

El usuario puede establecer la cantidad de memoria RAM y el espacio reservado para el Kernel en la sección ubicada en la parte inferior derecha. Además, se puede elegir el tamaño del disco y la memoria virtual.

Después de seleccionar los valores deseados, se debe presionar: **Aplicar configuración**

**Presionar el botón es necesario** para aplicar los cambios realizados, ya que **simula** la acción de cambiar la memoria principal física por otra de otro tamaño.

El simulador utiliza estos valores para determinar qué parte de la memoria estará disponible para el sistema y qué parte podrá utilizar el programa.

La configuración puede editarse **únicamente cuando no haya un programa cargado**.

La configuración predeterminada es:

* **Memoria principal:** 256 celdas.
* **Reservado para Kernel:** 64 celdas.
* **Tamaño del disco:** 512 celdas.
* **Tamaño de la memoria virtual:** 64 celdas.

### Límites de memoria  CAMBIAR

- El tamaño máximo para la memoria principal es de 65536 celdas.
- El tamaño mínimo para la memoria principal es de 128 celdas.
- El tamano maximo establecido para el Kernel es de (memoria principal - 16).
- El tamaño mínimo establecido para el Kernel es de 16 celdas.

Los límites se establecen de esta forma debido a que para esta tarea solo se requiere un proceso a la vez y el PCB solo necesita 8 celdas.

El usuario puede establecer el tamaño de la memoria principal y el espacio reservado para el Kernel según sus necesidades y criterios técnicos.

---

## 2. Seleccionar un programa

El simulador trabaja con archivos de código fuente que utilizan la extensión:

```text
.asm
```

Para seleccionar un programa se utiliza el explorador de archivos disponible en la interfaz.

El archivo seleccionado contiene las instrucciones que posteriormente serán procesadas por el simulador.

En la carpeta `Ejemplo` se encuentran ejemplos de programa válidos, que contienen la totalidad de instrucciones posibles para el simulador, se debe seguir esa sintaxis.

Al seleccionar los archivos ocurre una serie de procesos:
1. Se lee el contenido de cada archivo seleccionado (FileManager).
2. Se analizan sus líneas, descartando las vacías y validando la sintaxis de las instrucciones (AsmParser).
3. Se verifican los rangos numéricos permitidos y el formato correcto de las nuevas interrupciones (AsmParser).
4. Las instrucciones válidas se convierten a su representación interna y se almacenan de forma persistente en la sección de programas del Disco (Loader).
5. Se crea una entrada en el Índice de Archivos del disco para registrar la ubicación del programa (Loader).
6. Se genera un trabajo (`Job`) que representa al programa en estado `NEW` y se añade a la cola de la Lista de Trabajos (`JobList`).
7. Se actualizan las tablas de procesos, trabajos y el disco en la interfaz gráfica (VentanaPrincipal).

---

## 3. Cargar los programas

Después de seleccionar los archivos `.asm`, se debe presionar:

**Cargar programas**

En esta etapa básicamente inicia la simulación, donde se encuentra en acción el Planificador (`Scheduler`) aplicando la política FCFS (First-Come, First-Served):

1. El planificador toma los trabajos (`Jobs`) almacenados en la `JobList`.
2. Para cada trabajo, crea su Bloque de Control de Proceso (PCB) en la región reservada para el Kernel en la RAM.
3. Intenta asignar espacio para las instrucciones del programa en la memoria RAM de usuario:
   - **Si hay espacio en RAM:** Las instrucciones se cargan a la RAM y el proceso cambia su estado a `READY`.
   - **Si la RAM está llena pero hay espacio en Memoria Virtual (Swap):** Las instrucciones se guardan en la sección de memoria virtual del disco y el proceso cambia su estado a `READY_SUSPENDED`.
4. Si la RAM y la Memoria Virtual están totalmente llenas, el trabajo permanece en espera en la cola hasta que se libere espacio.
5. Se actualizan la Lista de Procesos activos, las celdas de la RAM, la Memoria Virtual y las tablas de la interfaz gráfica.

---

## 4. Ejecutar el programa

Una vez que los programas han sido admitidos y sus procesos creados, existen dos modalidades para ejecutarlos.

### Paso a paso

La opción **Paso a paso** permite ejecutar una sola instrucción a la vez correspondiente al proceso que se encuentra en ejecución (`RUNNING`).

Esta modalidad resulta especialmente útil para observar cómo cambian:

* El `PC` (Program Counter) y el `IR` (Instruction Register).
* El acumulador `AC` y los registros generales `AX`, `BX`, `CX` y `DX`.
* La bandera de estado `PSW` (Program Status Word) y la pila (Stack).
* El contenido de las celdas de la RAM y la Memoria Virtual.
* Las transiciones de estado en la Lista de Procesos (por ejemplo, cuando un proceso finaliza en `EXIT` y el planificador mueve automáticamente a un proceso de `READY_SUSPENDED` a `READY`).
* Las llamadas al sistema a través de interrupciones (`INT`).

De esta manera se puede analizar en detalle el ciclo de búsqueda y ejecución de las instrucciones, así como la gestión interna del Kernel.

### Ejecutar todo

La opción **Ejecutar todo** procesa automáticamente las instrucciones de forma secuencial y continua.

Al finalizar todos los procesos, el simulador muestra un panel de **Estadísticas**, detallando los tiempos de llegada, ejecución y finalización (expresados en *ticks* del reloj del sistema) de cada proceso.

---

# Documentación solicitada

## Estrategia de seguridad

### Protección de programas

Se implementaron validaciones para evitar que un programa pueda saltar a una dirección fuera de su espacio de memoria. Dada la arquitectura del proyecto, con implementar esta validación en el JMP es suficiente para que también se valide para JE y JNE. Dado a que los programas se almacenan continuamente, el sistema conoce en cual posición de memoria inicia cada programa, su alcance (tamaño) y por ende, donde debería de terminar su alcance. Cualquier intento de acceso fuera del programa emitiría el error, cerrando el programa con el estado EXIT y notificando el error en la consola, para posteriormente continuar con la ejecución de otros programas.

## Diagrama de paquetes

<p align="left">
  <img src="https://res.cloudinary.com/dpuuo4mfh/image/upload/v1791374705/diagramapaquetes_pfwyaq.png" width="1000" alt="Diagrama de paquetes del proyecto">
</p>

---

# Arquitectura del sistema

El proyecto utiliza una separación por responsabilidades entre los diferentes componentes. La lógica principal se encuentra distribuida entre el modelo de hardware simulado (CPU, RAM y Disco), el controlador que representa al Kernel, los servicios encargados de procesar programas y la interfaz gráfica.

## Estructura de archivos

```text
t1-principios-sistemas-operativos-mini-pc/
├── compilar.bat
├── ejecutar.bat
├── README.md
├── Ejemplo/
│   └── file.asm
└── Programa/
    └── MiniPCSimulator/
        ├── build.xml
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
        │       │   ├── CPU.java
        │       │   ├── Disk.java
        │       │   ├── Dispatcher.java
        │       │   ├── FileIndex.java
        │       │   ├── FileSystem.java
        │       │   ├── Instruction.java
        │       │   ├── InterruptHandler.java
        │       │   ├── Job.java
        │       │   ├── JobList.java
        │       │   ├── Loader.java
        │       │   ├── MainMemory.java
        │       │   ├── MemoryRegister.java
        │       │   ├── PCB.java
        │       │   ├── Process.java
        │       │   ├── ProcessList.java
        │       │   ├── Scheduler.java
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
| `MiniPCController.java` | Controlador | Actúa como el Kernel principal del sistema. Coordina la cola de trabajos, el planificador, la ejecución de la CPU, las interrupciones y los eventos de la interfaz gráfica. |
| `VentanaPrincipal.java` | Vista | Implementa la interfaz gráfica con Swing y FlatLaf. Permite configurar el sistema, cargar programas, ingresar datos por teclado y visualizar el estado de la RAM, Disco, Memoria Virtual, CPU y estadísticas. |
| `CPU.java` | Modelo | Representa el procesador. Aplica el ciclo de búsqueda (*fetch*) y ejecución (*execute*), gestiona los registros (`AX`, `BX`, `CX`, `DX`, `AC`, `PC`, `IR`, `PSW`) y el puntero de la Pila (*Stack Pointer*). |
| `MainMemory.java` | Modelo | Representa la memoria RAM del sistema. Está dividida entre el área del Kernel (donde se guardan los BCP) y el área de Usuario. |
| `Disk.java` | Modelo | Representa el almacenamiento secundario. Está dividido en áreas para el Índice de Archivos, el área de programas/archivos de usuario y la Memoria Virtual (área de Swap). |
| `MemoryRegister.java` | Modelo | Representa una celda individual de memoria (tanto en RAM como en Disco), permitiendo almacenar instrucciones, atributos del BCP o datos numéricos. |
| `Process.java` | Modelo | Representa un proceso activo en el sistema, vinculando sus instrucciones con la información del BCP y su ciclo de vida. |
| `PCB.java` | Modelo | Representa el Bloque de Control de Proceso (BCP), almacenando el ID, estado, PC, dirección base, tamaño, registros guardados, puntero de pila y métricas de tiempo. |
| `ProcessList.java` | Modelo | Administra la lista de procesos activos en el sistema que se encuentran en RAM o en Memoria Virtual. |
| `Job.java` | Modelo | Representa un trabajo que ha sido leído desde un archivo `.asm` y está listo para ser evaluado por el planificador. |
| `JobList.java` | Modelo | Gestiona la cola de trabajos pendientes (`NEW`) que esperan ser admitidos en el sistema. |
| `Scheduler.java` | Modelo | Implementa el planificador a largo y mediano plazo utilizando la política FCFS. Decide si un trabajo entra a la RAM o pasa a la Memoria Virtual (`READY_SUSPENDED`), y gestiona el swap cuando se libera RAM. |
| `Dispatcher.java` | Modelo | Encargado del cambio de contexto. Restaura los registros del CPU con los datos del BCP del proceso entrante y guarda el estado al pausar o suspender. |
| `Loader.java` | Modelo | Copia el código fuente traducido al Disco, crea su entrada en el Índice de Archivos y realiza la carga de instrucciones hacia la RAM o la Memoria Virtual según disponga el Scheduler. |
| `InterruptHandler.java` | Modelo | Atiende las interrupciones de software y de I/O (`INT 10H`, `INT 20H`, `INT 21H`), pausando la ejecución cuando se requiere entrada del usuario o gestionando llamadas al sistema de archivos. |
| `FileSystem.java` | Modelo | Modela el sistema de archivos sobre el Disco simulado. Permite crear, abrir, escribir, leer y cerrar archivos mediante llamadas al sistema. |
| `FileIndex.java` | Modelo | Gestiona la tabla de metadatos del Disco (índice de archivos), registrando nombres, tamaños y direcciones iniciales de los archivos almacenados. |
| `SystemClock.java` | Modelo | Simula el reloj del sistema, incrementando los *ticks* de tiempo en cada ciclo para medir la duración de los procesos. |
| `AsmParser.java` | Servicio | Analiza los archivos `.asm`, valida la sintaxis de las instrucciones tradicionales e interrupciones, verifica rangos y descarta líneas vacías. |
| `FileManager.java` | Servicio | Administra la lectura y selección de múltiples archivos desde el sistema operativo anfitrión. |
| `SystemConfig.java` | Utilidad | Centraliza los parámetros configurables del sistema, como el tamaño de la RAM, el área del Kernel, el tamaño del Disco y el espacio de Memoria Virtual. |

---

# Funcionamiento interno

## Carga y Planificación de programas

El flujo comienza cuando el usuario selecciona uno o varios archivos `.asm`.

El `FileManager` lee los archivos y `AsmParser` los valida. Cada programa válido es guardado en la sección de código del Disco simulado mediante el `Loader` y se registra en el Índice de Archivos (`FileIndex`). Posteriormente, cada programa crea un trabajo (`Job`) en la `JobList` con el estado `NEW`.

Cuando se presiona **Cargar programa**, entra en acción el `Scheduler` con la política **FCFS (First-Come, First-Served)**. El planificador toma el primer trabajo de la cola, crea su BCP en el espacio reservado para el Kernel en la RAM y evalúa el espacio disponible en la RAM de usuario:

* **Si hay espacio suficiente en RAM:** El `Loader` copia las instrucciones a la RAM y el proceso pasa al estado `READY`.
* **Si la RAM no es suficiente pero hay espacio en Memoria Virtual (Swap):** Las instrucciones se guardan en el área de Swap del Disco y el proceso pasa al estado `READY_SUSPENDED`.
* **Si no hay espacio en RAM ni en Memoria Virtual:** El trabajo permanece en la `JobList` esperando a que se libere espacio.

Cuando un proceso en ejecución termina y pasa al estado `EXIT`, libera su espacio en la RAM. El `Scheduler` detecta esto inmediatamente y realiza un proceso de **Swap-In**, tomando un proceso en estado `READY_SUSPENDED`, trasladando sus instrucciones desde la Memoria Virtual a la RAM liberada y cambiando su estado a `READY`.

---

## Ciclo de ejecución e Interrupciones

La ejecución se basa en el ciclo fundamental de:

```text
FETCH -> EXECUTE

```

Durante el **FETCH**, la CPU utiliza el `PC` del proceso activo para leer la instrucción directamente desde la RAM.

Durante el **EXECUTE**, el procesador interpreta la instrucción y ejecuta la operación. Si la instrucción modifica registros (`AX`, `BX`, `CX`, `DX`, `AC`), banderas (`PSW`) o la pila, los cambios se reflejan inmediatamente en el procesador.

Si la CPU encuentra una instrucción de interrupción (`INT`), delega la atención al `InterruptHandler`:

* **`INT 10H` (Salida en pantalla):** Muestra el valor almacenado en los registros directamente en la consola visual de la GUI.
* **`INT 20H` (Terminar programa):** Finaliza el proceso, calcula sus estadísticas finales de tiempo y marca su estado como `EXIT`.
* **`INT 21H` (Sistema de Archivos):** Permite interactuar con el `FileSystem` para crear (`AH=3CH`), abrir (`AH=3DH`), escribir (`AH=40H`), leer (`AH=41H`) o cerrar (`AH=4DH`) archivos en el disco simulado.

---

# Memoria y Disco

El sistema organiza la memoria simulada en dos estructuras físicas separadas: **RAM** y **Disco**, ambas estructuradas mediante objetos `MemoryRegister` para almacenar tanto instrucciones como datos.

### Memoria RAM (`MainMemory`)

Se divide en dos áreas principales:

* **Espacio del Kernel:** Reservado para almacenar los BCP (PCB) de los procesos admitidos en el sistema.
* **Espacio de Usuario:** Disponible para cargar las instrucciones de los programas que están listos para ejecutarse (`READY` o `RUNNING`).

### Disco (`Disk`)

Se divide conceptualmente en tres zonas:

* **Índice de archivos (File Index):** Almacena la tabla con la lista de archivos guardados en el disco, sus tamaños y sus direcciones físicas de inicio.
* **Área de archivos y programas:** Almacena las instrucciones originales de los programas cargados y los archivos creados dinámicamente mediante la interrupción `INT 21H`.
* **Memoria Virtual (Área de Swap):** Espacio reservado para almacenar el código de los procesos en estado `READY_SUSPENDED` que no cupieron en la RAM al momento de ser admitidos.

---

# Registros y CPU

El procesador simulado cuenta con los siguientes elementos de hardware:

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

---

# Proceso, BCP y Estadísticas

Cada programa admitido en el sistema es gestionado a través de su **BCP (Bloque de Control de Proceso)**.

El ciclo de vida de un proceso pasa por los siguientes estados:

* **NEW:** El archivo fue leído y está en la `JobList` esperando a ser evaluado por el planificador.
* **READY:** El proceso está cargado en la memoria RAM y espera su turno en la CPU.
* **READY_SUSPENDED:** El proceso fue admitido, pero sus instrucciones se guardaron en la Memoria Virtual a la espera de espacio libre en RAM.
* **RUNNING:** El proceso tiene el control de la CPU y sus instrucciones están siendo ejecutadas.
* **BLOCKED:** El proceso se pausó a la espera de una entrada I/O.
* **BLOCKED_SUSPENDED:** El proceso se pausó a la espera de espacio libre en RAM, pero sus instrucciones se guardaron en la Memoria Virtual a la espera de espacio libre en RAM.
* **EXIT:** El proceso terminó su ejecución, liberó su espacio en memoria y guardó sus estadísticas finales.

### Almacenamiento del BCP en RAM

Los atributos de cada BCP se escriben físicamente dentro de las celdas del área del Kernel en la RAM simulada. Cada campo (ID, estado, PC, dirección base, tamaño, registros generales, puntero de pila, etc.) ocupa una celda de memoria, lo que permite observar visualmente cómo el Kernel guarda el estado administrativo de cada proceso.

### Registro de Estadísticas

A través del `SystemClock`, el sistema registra el tiempo (en *ticks*) en que cada proceso realiza sus transiciones. Al finalizar la ejecución de todos los procesos, el simulador muestra un resumen de duración de cada proceso.

---

# Validación de programas

Antes de almacenar un programa en el Disco, el archivo `.asm` es validado por la clase `AsmParser`.

Entre las comprobaciones realizadas se incluyen:

* Sintaxis correcta de las instrucciones tradicionales (`MOV`, `ADD`, `SUB`, `JMP`, `CMP`, etc.).
* Validación de las nuevas instrucciones e interrupciones (`INT 10H`, `INT 20H`, `INT 21H`, `PUSH`, `POP`, etc.).
* Verificación del número y tipo de operandos permitidos (registros, constantes o direcciones).
* Verificación de rangos numéricos permitidos para evitar valores fuera de límite.
* Eliminación y descarte automático de líneas vacías o comentarios.

Descartar las líneas vacías antes de guardar el programa evita reservar espacio innecesario en el Disco y en la RAM, garantizando que cada celda de memoria contenga únicamente instrucciones válidas y procesables.

---

# Manejo de Overflow

El simulador representa el comportamiento de una arquitectura basada en un rango limitado de bits.

Por esta razón, el sistema no bloquea por software una operación cuando el resultado sobrepasa el límite permitido, sino que permite que el valor se comporte según las limitaciones del desbordamiento físico. Esto permite observar cómo afecta el *overflow* a los registros durante las operaciones aritméticas.

---

# Decisiones de diseño

## Interfaz gráfica

Se utilizó **Swing** junto a la librería **FlatLaf** con un tema oscuro moderno. La interfaz fue estructurada en paneles separados para observar simultáneamente la RAM, el Disco, la Memoria Virtual, los registros de la CPU, la lista de procesos y los logs del sistema, facilitando la comprensión del flujo sin depender de una consola.

## Planificación FCFS y Lista de Trabajos

Se implementó una `JobList` previa a la carga en memoria para modelar adecuadamente la admisión de procesos. El `Scheduler` procesa los trabajos en estricto orden de llegada (FCFS), lo que permite simular escenarios reales de contención de memoria cuando múltiples programas compiten por el espacio disponible. Mientras un proceso está `BLOCKED` no se ingresa ningún otro proceso debido a lo indicado por el profesor en clase.

## Memoria Virtual y Swapping

Para manejar el exceso de procesos sin rechazarlos inmediatamente, se asignó una sección dedicada del Disco como Memoria Virtual. Esto permitió implementar el estado `READY_SUSPENDED` y el mecanismo de swap simple, donde el Kernel traslada automáticamente el código desde el disco a la RAM cuando un proceso saliente libera espacio.

## Sistema de Archivos simulado

Se diseñó la clase `FileSystem` respaldada por `FileIndex` para simular la gestión de archivos en Disco. A través de la interrupción `INT 21H`, los programas en ensamblador pueden crear y manipular archivos dentro del propio entorno del simulador, conectando los conceptos de I/O con la tabla de metadatos del disco. Se asume, por lo que se habló en clases, que los archivos solo tienen números dentro de los límites definidos, y que no se contemplarán errores de flujo, por ejemplo, la apetura de archivos inexistentes.

## Separación de responsabilidades

El proyecto sigue el patrón MVC (Modelo-Vista-Controlador). El modelo administra el hardware y las estructuras del SO (`CPU`, `MainMemory`, `Disk`, `PCB`, `Scheduler`), los servicios procesan los archivos externos (`AsmParser`, `FileManager`), el controlador coordina las interacciones del Kernel y la vista muestra visualmente todos los cambios en tiempo real.

---

# Consideraciones y limitaciones

El simulador está diseñado con fines estrictamente académicos, por lo que abstrae ciertas características de un sistema real:

* La arquitectura trabaja con valores y registros con rangos numéricos limitados.
* La planificación de procesos está centrada en la política FCFS sin desalojo, sin prioridades.
* El sistema de archivos es de estructura plana (no soporta jerarquía de carpetas/directorios).
* La Memoria Virtual funciona solo cuando no se puede ingresar un programa por falta de memoria de usuario pero sí cabe su PCB, y no por paginación u otras estrategias.
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

Esta versión extendida de Mini PC Simulator ofrece una visión integral sobre el funcionamiento de un sistema operativo. Integra la gestión de almacenamiento secundario en disco, la memoria virtual con swapping, la planificación de trabajos bajo FCFS, la atención de interrupciones de I/O y la interacción con un sistema de archivos simple.

La posibilidad de observar en tiempo real cómo los procesos pasan de la memoria virtual a la RAM, cómo se guardan los BCP en el área del Kernel y cómo se registran las estadísticas de ejecución convierte a este simulador en una herramienta práctica y clara para comprender los conceptos del curso de Principios de Sistemas Operativos.

---

*JohnsyDev, 2026*