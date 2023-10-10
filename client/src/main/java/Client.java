import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;


public class Client {
    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client", extraArgs)) {
            Demo.PrinterPrx twoway = Demo.PrinterPrx.checkedCast(
                    communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);

            if (twoway == null) {
                throw new Error("Invalid proxy");
            }

            Scanner scanner = new Scanner(System.in);

            menu(twoway, scanner);
        }
    }

    private static void menu(Demo.PrinterPrx twoway, Scanner scanner){
        int option;

        do{
            System.out.println("Select an option: \n" +
                    "1. Send a message\n" +
                    "2. Do throughput test\n" +
                    "3. Do response time test\n" +
                    "4. Do missing rate test\n" +
                    "5. Do unprocessed rate test\n" +
                    "6. Exit");
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option){
                case 1:
                    send(twoway, scanner);
                    break;
                case 2:
                    System.out.println(evaluateThroughput(twoway, scanner));
                    break;
                case 3:
                    System.out.println(evaluateResponseTime(twoway,100));
                    break;
                case 4:
                    System.out.println(evaluateMissingRate(twoway, scanner));
                    break;
                case 5:
                    System.out.println(evaluateUnprocessedRate(twoway, scanner));
                    break;
            }
        }while(option >= 1 && option <=5);
    }

    private static void send(Demo.PrinterPrx twoway, Scanner scanner){
        if (twoway == null) {
            throw new Error("Invalid proxy");
        }

        String username = System.getProperty("user.name");
        String hostname = getHostname();

        while (true) {
            System.out.print("Enter a message or enter exit to get out ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("exit")) {
                break;
            }

            String formattedMessage = username + ":" + hostname + ":" + message;
            String response = twoway.printString(formattedMessage);
            System.out.println("Server Response: " + response);
        }
    }

    private static String send(Demo.PrinterPrx twoway, String message) throws Exception{
        if (twoway == null) {
            throw new Error("Invalid proxy");
        }

        String username = System.getProperty("user.name");
        String hostname = getHostname();

        String formattedMessage = username + ":" + hostname + ":" + message;
        return twoway.printString(formattedMessage);
    }

    private static String evaluateThroughput(Demo.PrinterPrx twoway, Scanner scanner){
        System.out.println("Enter the number of requests");
        int numberOfRequests = scanner.nextInt();
        scanner.nextLine();

        long startTime = System.currentTimeMillis();
        int successfulRequests = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            try {
                send(twoway, "!ls");
                successfulRequests++;
            } catch (Exception e) {
                // Log o manejo de error
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        double throughput = (double) successfulRequests / (elapsedTime / 1000.0);

        return "Elapsed time in ms: " + elapsedTime + "\n" +
                "Throughput (request per second): " + throughput;
    }

    private static String evaluateResponseTime(Demo.PrinterPrx twoway, int numberOfSamples){
        long totalResponseTime = 0;
        List<Long> responseTimes = new ArrayList<>(); // Para almacenar tiempos de respuesta individuales

        for (int i = 0; i < numberOfSamples; i++) {
            long startTime = System.nanoTime(); // Cambio a nanoTime para mayor precisión
            try {
                send(twoway, "!ls");
                long endTime = System.nanoTime();
                long responseTime = endTime - startTime;
                responseTimes.add(responseTime); // Agregar tiempo de respuesta a la lista
                System.out.println("Response time for iteration " + (i+1) + ": " + responseTime + " ns"); // Imprimir tiempo de respuesta de cada iteración
                totalResponseTime += responseTime;
            } catch (Exception e) {
                System.out.println("Error during iteration " + (i+1) + ": " + e.getMessage());
            }
        }

        double averageResponseTime = (double) totalResponseTime / numberOfSamples;
        return "Average response time: " + averageResponseTime + " ns";
    }

    private static String evaluateMissingRate(Demo.PrinterPrx twoway, Scanner scanner){
        System.out.println("Enter the number of requests");
        int numberOfRequests = scanner.nextInt();
        scanner.nextLine();

        int lostRequests = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            try {
                send(twoway, "!ls");
            }catch (Exception e){
                lostRequests++;
            }
        }

        double missingRate = (double) lostRequests / numberOfRequests;

        return "Lost requests: " + lostRequests + "\n" +
                "Missing rate: " + missingRate;
    }

    private static String evaluateUnprocessedRate(Demo.PrinterPrx twoway, Scanner scanner){
        System.out.println("Enter the number of requests");
        int numberOfRequests = scanner.nextInt();
        scanner.nextLine();

        int unprocessedRequest = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            try {
                String response = send(twoway, "!ls");
                if(response.contains("Error")){
                    unprocessedRequest++;
                }
            } catch (Exception e) {
                unprocessedRequest++;
                e.printStackTrace();
            }
        }

        double unprocessedRate = (double) unprocessedRequest / numberOfRequests;

        return "Unprocessed rate: " + unprocessedRate;
    }

    private static String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException ex) {
            return "Unknown";
        }
    }
}