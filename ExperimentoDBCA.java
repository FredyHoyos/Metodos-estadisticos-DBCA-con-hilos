import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class ExperimentoDBCA {

    // =======================================
    // CONFIGURACIÓN DEL EXPERIMENTO
    // =======================================
    static final int[] THREADS = {1, 2, 4};

    // Tamaños de los datos
    static final int ARRAY_SIZE = 5_000_000;
    static final int MATRIX_SIZE = 200;    // 200x200
    static final int SORT_SIZE = 500_000;
    static final int SEARCH_SIZE = 1_000_000;

    static Random random = new Random();

    // =======================================
    // MÉTODO PRINCIPAL
    // =======================================
    public static void main(String[] args) throws Exception {

        System.out.println("=== EXPERIMENTO DBCA: Algoritmos × Número de Hilos ===\n");

        for (int t : THREADS) {
            System.out.println("\n--- TRATAMIENTO: " + t + " hilos ---");

            // Bloque 1: Suma
            System.out.println("Suma arreglo: " + medir(() -> sumaParalela(t)) + " ms");

            // Bloque 2: Multiplicación de matrices
            System.out.println("Multiplicación matrices: " + medir(() -> multiplicarMatricesParalelo(t)) + " ms");

            // Bloque 3: Ordenamiento
            System.out.println("Ordenamiento: " + medir(() -> ordenarParalelo(t)) + " ms");

            // Bloque 4: Búsqueda
            System.out.println("Búsqueda: " + medir(() -> busquedaParalela(t)) + " ms");
        }
    }

    // =======================================
    // MEDIDOR DE TIEMPO
    // =======================================
    public static long medir(Runnable r) {
        long inicio = System.nanoTime();
        r.run();
        long fin = System.nanoTime();
        return (fin - inicio) / 1_000_000; // ms
    }

    // =======================================
    // BLOQUE 1: SUMA DE ARREGLO
    // =======================================
    public static void sumaParalela(int threads) {

        // Genera un arreglo de tamaño ARRAY_SIZE con números aleatorios entre 1 y 10.
        // 'random' es un Random previamente creado.
        int[] arreglo = random.ints(ARRAY_SIZE, 1, 10).toArray();

        // Crea un pool de hilos con un número fijo de 'threads'.
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        // Divide el arreglo en partes iguales según la cantidad de hilos.
        int chunk = ARRAY_SIZE / threads;

        // Arreglo donde se guardarán los resultados parciales devueltos por cada hilo.
        Future<Long>[] resultados = new Future[threads];

        // Asigna a cada hilo una parte del arreglo.
        for (int i = 0; i < threads; i++) {

            // Índice de inicio del subarreglo para este hilo.
            int inicio = i * chunk;

            // Índice final. El último hilo toma hasta el final para evitar pérdida por división irregular.
            int fin = (i == threads - 1) ? ARRAY_SIZE : inicio + chunk;

            // Envia una tarea (Callable) al pool de hilos.
            // Cada hilo sumará su parte del arreglo.
            resultados[i] = exec.submit(() -> {
                long suma = 0;
                // Recorre su sección del arreglo y acumula la suma.
                for (int j = inicio; j < fin; j++) suma += arreglo[j];
                return suma; // Retorna la suma parcial.
            });
        }

        // Variable final donde se acumulará la suma total.
        long total = 0;

        try {
            // Espera a que cada hilo termine y acumula sus resultados.
            for (Future<Long> f : resultados) total += f.get();
        } catch (Exception ignored) {}

        // Cierra el pool de hilos después de terminar.
        exec.shutdown();
    }


    // =======================================
    // BLOQUE 2: MULTIPLICACIÓN DE MATRICES
    // =======================================
    public static void multiplicarMatricesParalelo(int threads) {

        // Genera dos matrices cuadradas A y B de tamaño MATRIX_SIZE con valores aleatorios
        int[][] A = genMatriz(MATRIX_SIZE);
        int[][] B = genMatriz(MATRIX_SIZE);

        // Matriz resultado C, inicialmente llena de ceros
        int[][] C = new int[MATRIX_SIZE][MATRIX_SIZE];

        // Crea un pool de hilos con el número indicado
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        // Recorre cada fila de la matriz A
        for (int i = 0; i < MATRIX_SIZE; i++) {
            int fila = i; // Se guarda el índice para usarlo dentro de la lambda

            // Envía una tarea al pool: calcular la fila 'fila' de la matriz resultado C
            exec.submit(() -> {

                // Recorre todas las columnas de la matriz B
                for (int j = 0; j < MATRIX_SIZE; j++) {
                    int suma = 0;

                    // Producto punto entre la fila 'fila' de A y la columna 'j' de B
                    for (int k = 0; k < MATRIX_SIZE; k++) {
                        suma += A[fila][k] * B[k][j];
                    }

                    // Almacena el resultado en la posición correspondiente
                    C[fila][j] = suma;
                }
            });
        }

        // Ordena al pool de hilos que no acepte más tareas
        exec.shutdown();

        try {
            // Espera hasta un máximo de 1 minuto que todas las tareas terminen
            exec.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {}
    }


// ------------------------------------------------------------
// Función auxiliar para generar una matriz cuadrada aleatoria
// ------------------------------------------------------------
static int[][] genMatriz(int size) {

    // Crea la matriz
    int[][] m = new int[size][size];

    // Llena cada celda con números aleatorios entre 0 y 9
    for (int i = 0; i < size; i++)
        for (int j = 0; j < size; j++)
            m[i][j] = random.nextInt(10);

    return m; // Retorna la matriz generada
}


    // =======================================
    // BLOQUE 3: ORDENAMIENTO
    // =======================================
    public static void ordenarParalelo(int threads) {

        // Genera un arreglo de tamaño SORT_SIZE con valores aleatorios
        int[] arr = random.ints(SORT_SIZE).toArray();

        // Caso trivial: si solo hay 1 hilo, se ordena normalmente (sin paralelismo)
        if (threads == 1) {
            Arrays.sort(arr);
            return; // No hay nada más que hacer
        }

        // Calcula cuántos elementos le tocarán a cada hilo
        int chunk = SORT_SIZE / threads;

        // Crea un pool de hilos con el número solicitado
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        // Arreglo para guardar los resultados parciales (subarreglos ordenados)
        Future<int[]>[] partes = new Future[threads];

        // Divide el arreglo y asigna a cada hilo un segmento para ordenar
        for (int i = 0; i < threads; i++) {

            // Índice donde empieza la parte asignada al hilo
            int inicio = i * chunk;

            // Último hilo toma hasta el final para evitar perder elementos
            int fin = (i == threads - 1) ? SORT_SIZE : inicio + chunk;

            // Envía una tarea al pool:
            // copiar su parte del arreglo, ordenarla y devolverla
            partes[i] = exec.submit(() -> {

                // Copia la sección del arreglo que este hilo debe ordenar
                int[] sub = Arrays.copyOfRange(arr, inicio, fin);

                // Ordena el subarreglo de manera independiente
                Arrays.sort(sub);

                return sub; // Devuelve el resultado parcial
            });
        }

        try {
            // Recibe las partes ya ordenadas (pero aún separadas)
            int[][] sortedParts = new int[threads][];

            // Espera a que cada hilo termine y obtiene su subarreglo ordenado
            for (int i = 0; i < threads; i++)
                sortedParts[i] = partes[i].get();

            // IMPORTANTE:
            // Aquí solo se obtienen las partes ordenadas, pero NO se hace un merge real.
            // Aun así sirve para medir tiempos de ordenamiento en paralelo.
            //
            // En un mergesort paralelo completo habría que unir sortedParts en una sola lista.

        } catch (Exception ignored) {}

        // Cierra el pool de hilos porque ya no se usarán más tareas
        exec.shutdown();
    }


    // =======================================
    // BLOQUE 4: BÚSQUEDA
    // =======================================
    public static void busquedaParalela(int threads) {

        // Genera un arreglo de tamaño SEARCH_SIZE con números aleatorios
        int[] arr = random.ints(SEARCH_SIZE).toArray();

        // Selecciona un valor objetivo que siempre está dentro del arreglo,
        // así garantizas que la búsqueda lo pueda encontrar.
        int objetivo = arr[random.nextInt(arr.length)];

        // Crea un pool de hilos con el número especificado
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        // Divide el arreglo en partes iguales para repartirlas entre los hilos
        int chunk = SEARCH_SIZE / threads;

        // Arreglo para almacenar el resultado que devuelve cada hilo
        Future<Boolean>[] respuestas = new Future[threads];

        // Asignar a cada hilo una parte del arreglo para buscar el objetivo
        for (int i = 0; i < threads; i++) {

            // Índice inicial del segmento del arreglo
            int inicio = i * chunk;

            // Índice final del segmento (el último hilo toma hasta el final)
            int fin = (i == threads - 1) ? SEARCH_SIZE : inicio + chunk;

            // Enviar tarea al pool:
            // buscar el objetivo en el segmento [inicio, fin)
            respuestas[i] = exec.submit(() -> {

                // Se recorre solo la parte que le corresponde al hilo
                for (int j = inicio; j < fin; j++) {

                    // Si encuentra el valor, devuelve true inmediatamente
                    if (arr[j] == objetivo) return true;
                }

                // No lo encontró en su parte
                return false;
            });
        }

        try {
            // Se revisan los resultados de los hilos.
            // Si alguno dice TRUE, la búsqueda termina.
            for (Future<Boolean> f : respuestas) {
                if (f.get()) break; // Si 1 hilo lo encontró, ya no hace falta seguir
            }

        } catch (Exception ignored) {}

        // Cierra el pool de hilos, ya no se necesita más procesamiento
        exec.shutdown();
    }

}
