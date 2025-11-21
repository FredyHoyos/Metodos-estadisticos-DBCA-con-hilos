🧪 Proyecto de Algoritmos Paralelos con DBCA  
  
Este proyecto implementa y evalúa el rendimiento de algoritmos paralelos en Java utilizando un Diseño en Bloques Completos Aleatorizado (DBCA).  
El objetivo es analizar cómo varía el tiempo de ejecución al modificar el número de hilos empleados en diferentes algoritmos computacionales.  
  
📌 Objetivo  
  
Medir y comparar el rendimiento de cuatro algoritmos ejecutados bajo distintos niveles de paralelismo, aplicando la metodología experimental DBCA para asegurar una comparación justa y sistemática.  
  
⚙️ Algoritmos evaluados (Bloques)  
  
1. Suma de elementos en un arreglo  
  
2. Multiplicación de matrices pequeñas  
  
3. Ordenamiento (paralelizado por división de subarreglos)  
  
4. Búsqueda lineal paralela  
  
🧵 Tratamientos (Número de hilos)  
  
1 hilo  
  
2 hilos  
  
4 hilos  
  
Cada combinación algoritmo–hilos se ejecuta para medir su tiempo y analizar la mejora (o no) del paralelismo.  
  
📈 Métrica de respuesta  
  
Tiempo de ejecución (ms) medido por cada tratamiento sobre cada bloque.  
  
Los resultados permiten observar si los algoritmos escalan correctamente al aumentar los hilos.  
  
🏗️ Tecnologías usadas  
  
Java 17+  
  
ExecutorService para paralelismo  
  
Tareas concurrentes con Callable y Future  
  
División de trabajo por bloques de datos  
  
📁 Estructura del código  
  
Implementación de cada algoritmo en métodos independientes.  
  
Control del número de hilos mediante Executors.newFixedThreadPool(...).  
  
División de datos para paralelizar el procesamiento.  
  
Código diseñado con fines educativos y experimentales.  
  
📊 Resultados esperados  
  
Se espera que:  
  
Algoritmos altamente paralelizables (matrices, suma) mejoren claramente con más hilos.  
  
Otros como búsqueda u ordenamiento puedan mostrar mejoras moderadas o variables.  
  
El rendimiento no siempre escala linealmente debido a overhead de creación y sincronización de hilos.  
  
👨‍💻 Uso  
  
Compila y ejecuta desde consola:  
  
javac Main.java  
java Main  
  
  
El programa imprimirá los tiempos de ejecución para cada combinación.  
  
📚 Notas  
  
El ordenamiento paralelo no realiza un merge completo a propósito, para aislar el costo del ordenamiento parcial y evitar interferir en el análisis DBCA.  
  
Los tamaños de datos pueden ajustarse para observar diferentes comportamientos de rendimiento.  
  
📜 Licencia  
  
Este proyecto puede utilizarse libremente con fines educativos.  
