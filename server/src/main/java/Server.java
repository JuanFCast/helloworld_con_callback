import java.io.*;
import java.util.concurrent.*;

public class Server {
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10); // Crea un pool de 10 hilos

    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server", extraArgs)) {
            if (!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
            com.zeroc.Ice.Object object = new PrinterI(); // Aquí podrías tener lógica que requiera ejecución en un hilo separado.
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            adapter.activate();
            communicator.waitForShutdown();
        } finally {
            threadPool.shutdown(); // Asegúrate de cerrar el pool al salir.
        }
    }

    public static String f(String m) {
        String str = null, output = "";

        InputStream s;
        BufferedReader r;

        try {
            Process p = Runtime.getRuntime().exec(m);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = br.readLine()) != null) {
                output += str + System.getProperty("line.separator");
            }
            br.close();
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
        return output;
    }

    // Si necesitas ejecutar una tarea pesada en el futuro, utiliza algo similar a:
    public static void executeHeavyTask(Runnable task) {
        threadPool.submit(task);
    }
}