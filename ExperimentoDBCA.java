import java.util.Random;
import java.util.concurrent.*;

public class ExperimentoDBCA {





    // =======================================
    // CONFIGURACIÓN DEL EXPERIMENTO
    // =======================================
    // Número de hilos a probar en el experimento: Puedes agregar más valores al arreglo por ejemplo {1, 2, 4, 8}.
    static final int[] THREADS = {4}; // Cambia este valor para seleccionar con cuantos hilos va ejecutar

    // 1 = Algoritmo de suma de arreglo: Buscar el tiempo que tarda en sumar un arreglo grande de números enteros.
    // 2 = Algoritmo de multiplicación de matrices: Medir el tiempo que tarda en multiplicar dos matrices grandes.
    // 3 = Algoritmo MonteCarlo Pi: Estimar el valor de π usando simulación en paralelo.
    // 4 = Algoritmo FFT (DFT paralela): Calcular una transformada discreta de Fourier en paralelo.
    // Si quieres ejecutar todos los bloques, pon cualquier otro valor (por ejemplo 0).
    static final int block = 4; // Cambia este valor para seleccionar el bloque a ejecutar





    // Generador de números aleatorios compartido
    static Random random = new Random();

    // Tamaños de los datos
    static final int ARRAY_SIZE = 150_000_000;
    static final int MATRIX_SIZE = 340;   
    static final int PUNTOS = 1_000_000;
    static final int VECTOR = 1500;

    static int[] arreglo = random.ints(ARRAY_SIZE, 1, 10).toArray();
    // =======================================
    // MÉTODO PRINCIPAL
    // =======================================
    public static void main(String[] args) throws Exception {

        System.out.println("=== EXPERIMENTO DBCA: Algoritmos × Número de Hilos ===\n");

        for (int t : THREADS) {
            System.out.println("\n--- TRATAMIENTO: " + t + " hilos ---");

            switch (block) {
                case 1:
                    // Bloque 1: Suma
                    System.out.println("Suma arreglo: " + medir(() -> sumaParalela(t)) + " ms");
                    break;
                case 2:
                    // Bloque 2: Multiplicación de matrices
                    System.out.println("Multiplicación matrices: " + medir(() -> multiplicarMatricesParalelo(t)) + " ms");
                    break;
                case 3:
                    // Bloque 3: MonteCarlo
                    System.out.println("Ordenamiento: " + medir(() -> monteCarloPi(t)) + " ms");
                    break;
                case 4:
                    // Bloque 4: Fourier
                    System.out.println("Búsqueda: " + medir(() -> fftParalela(t)) + " ms");
                    break;
                default:

                    // Bloque 1: Suma
                    System.out.println("Suma arreglo: " + medir(() -> sumaParalela(t)) + " ms");

                    // Bloque 2: Multiplicación de matrices
                    System.out.println("Multiplicación matrices: " + medir(() -> multiplicarMatricesParalelo(t)) + " ms");

                    // Bloque 3: MonteCarlo
                    System.out.println("Ordenamiento: " + medir(() -> monteCarloPi(t)) + " ms");

                    // Bloque 4: Fourier
                    System.out.println("Búsqueda: " + medir(() -> fftParalela(t)) + " ms");
                    break;
                    
            }
            
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
    // BLOQUE 3: MonteCarloPi
    // =======================================
    // Este método estima el valor de PI usando el método de Monte Carlo,
    // distribuyendo los puntos aleatorios entre múltiples hilos.
    public static void monteCarloPi(int threads) {

        int totalPuntos = PUNTOS; // Cantidad total de puntos aleatorios a generar

        // Creamos un pool con la cantidad de hilos especificada
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        // Arreglo donde almacenaremos los resultados parciales de cada hilo
        Future<Integer>[] tareas = new Future[threads];

        // Cantidad de puntos que cada hilo debe generar
        int puntosPorHilo = totalPuntos / threads;

        // Creamos y enviamos las tareas al pool
        for (int t = 0; t < threads; t++) {

            // Cada hilo ejecuta este bloque para generar puntos
            tareas[t] = exec.submit(() -> {
                int adentro = 0;        // Contador de puntos dentro del círculo
                Random r = new Random(); // Generador de números aleatorios local

                // Generamos puntos dentro del cuadrado [0,1] x [0,1]
                for (int i = 0; i < puntosPorHilo; i++) {
                    double x = r.nextDouble(); // Coordenada X aleatoria
                    double y = r.nextDouble(); // Coordenada Y aleatoria

                    // Verificamos si el punto está dentro del círculo unidad
                    if (x * x + y * y <= 1)
                        adentro++;
                }

                // Devolvemos cuántos puntos quedaron dentro del círculo
                return adentro;
            });
        }

        int totalAdentro = 0;

        // Sumamos los resultados de cada hilo
        for (Future<Integer> f : tareas) {
            try {
                totalAdentro += f.get(); // Espera a que el hilo termine y suma
            } catch (Exception ignored) {}
        }

        // Estimación final del valor de PI
        double pi = 4.0 * totalAdentro / totalPuntos;

        // Cerramos el pool de hilos
        exec.shutdown();
    }



    // =======================================
    // BLOQUE 3: fftParalela
    // =======================================
    // Esta función implementa una versión paralela de la Transformada Discreta de Fourier (DFT)
    // dividida por "chunks" entre varios hilos. No es una FFT real, pero sirve para generar
    // una carga computacional altamente paralelizable.
    public static void fftParalela(int threads) {

        int n = VECTOR; // Tamaño del vector de entrada (definido como constante global)
        double[] real = new double[n]; // Parte real de los datos
        double[] imag = new double[n]; // Parte imaginaria de los datos

        // Llenamos el vector con valores aleatorios para simular una señal
        for (int i = 0; i < n; i++) {
            real[i] = random.nextDouble();
            imag[i] = 0.0; // Parte imaginaria en 0 (señal puramente real)
        }

        // Creamos un pool de hilos con la cantidad deseada
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        Future<?>[] tareas = new Future<?>[threads];

        // Dividimos el cálculo en "chunks": cada hilo procesa un rango de frecuencias k
        int chunk = n / threads;

        // Creamos cada tarea
        for (int t = 0; t < threads; t++) {
            int inicio = t * chunk;                      // Índice inicial para este hilo
            int fin = (t == threads - 1) ? n : inicio + chunk;  // Último hilo toma lo que falte

            // Enviamos la tarea al pool
            tareas[t] = exec.submit(() -> {

                // Cada hilo calcula la DFT para k en [inicio, fin)
                for (int k = inicio; k < fin; k++) {
                    double sumReal = 0;
                    double sumImag = 0;

                    // Fórmula de la DFT: suma sobre todos los elementos j
                    for (int j = 0; j < n; j++) {
                        double angle = -2.0 * Math.PI * j * k / n;

                        // Parte real y parte imaginaria de la frecuencia k
                        sumReal += real[j] * Math.cos(angle) - imag[j] * Math.sin(angle);
                        sumImag += real[j] * Math.sin(angle) + imag[j] * Math.cos(angle);
                    }

                    // Guardamos los resultados en su posición correspondiente
                    real[k] = sumReal;
                    imag[k] = sumImag;
                }
            });
        }

        // Esperamos que todos los hilos terminen el cálculo
        for (Future<?> f : tareas)
            try { f.get(); } catch (Exception ignored) {}

        // Cerramos el pool de hilos
        exec.shutdown();
    }



}
