import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implementación de la interfaz Printer definida en el paquete Demo.
 * Gestiona varios tipos de mensajes y realiza acciones específicas según la entrada.
 */
public class PrinterI implements Demo.Printer {

	/**
	 * Implementa el método printString para procesar una cadena dada y devuelve una respuesta apropiada.
	 * El método analiza la cadena de entrada para obtener el nombre de usuario del cliente, el nombre del host y el mensaje.
	 * Luego procesa el mensaje para determinar la acción o respuesta apropiada a generar.
	 *
	 * @param s       Cadena de entrada en el formato: clientUsername:clientHostname:message
	 * @param current Información sobre la invocación del método actual (proporcionado por el framework Ice)
	 * @return Respuesta adecuada basada en el mensaje de entrada
	 */
	@Override
	public String printString(String s, com.zeroc.Ice.Current current) {
		String[] parts = s.split(":", 3);
		String clientUsername = parts[0];
		String clientHostname = parts[1];
		String message = parts[2];
		StringBuilder response = new StringBuilder();

		if (isPositiveInteger(message)) {
			handlePositiveInteger(clientUsername, clientHostname, message, response);
		} else if (message.startsWith("listifs")) {
			handleListInterfaces(clientUsername, clientHostname, response);
		} else if (message.startsWith("listports ")) {
			handleListPorts(clientUsername, clientHostname, message, response);
		} else if (message.startsWith("!")) {
			handleExecuteCommand(clientUsername, clientHostname, message, response);
		} else {
			handleDefaultMessage(clientUsername, clientHostname, message, response);
		}

		System.out.println(response.toString());
		return response.toString();
	}

	/**
	 * Maneja un mensaje que contiene un número entero positivo.
	 * Calcula los factores primos del número y añade el resultado a la respuesta.
	 */
	private void handlePositiveInteger(String clientUsername, String clientHostname, String message, StringBuilder response) {
		int num = Integer.parseInt(message);
		System.out.println(formatLogMessage(clientUsername, clientHostname, "sent a positive integer. Calculating prime factors..."));
		List<Integer> factors = primeFactors(num);
		response.append("Prime factors of ").append(num).append(": ").append(String.join(", ", factors.toString()));
	}

	/**
	 * Maneja una solicitud para listar interfaces lógicas de red.
	 * Obtiene las interfaces lógicas y las añade a la respuesta.
	 */
	private void handleListInterfaces(String clientUsername, String clientHostname, StringBuilder response) {
		System.out.println(formatLogMessage(clientUsername, clientHostname, "requested logical interfaces."));
		response.append("Logical interfaces: \n").append(listInterfaces());
	}

	/**
	 * Maneja una solicitud para listar puertos abiertos en una dirección IP específica.
	 * Obtiene directamente los puertos abiertos para la dirección IP proporcionada sin realizar verificación previa.
	 * Añade los resultados a la respuesta.
	 */
	private void handleListPorts(String clientUsername, String clientHostname, String message, StringBuilder response) {
		String ipAddress = message.substring("listports ".length()).trim();
		System.out.println(formatLogMessage(clientUsername, clientHostname, "requested open ports for IP: " + ipAddress));
		response.append("Open ports for IP ").append(ipAddress).append(": \n").append(listOpenPorts(ipAddress));
	}


	/**
	 * Maneja una solicitud para ejecutar un comando del sistema.
	 * Ejecuta el comando y añade la salida del comando o un mensaje de error a la respuesta.
	 */
	private void handleExecuteCommand(String clientUsername, String clientHostname, String message, StringBuilder response) {
		String command = message.substring(1).trim();
		System.out.println(formatLogMessage(clientUsername, clientHostname, "requested execution of command: " + command));
		response.append(executeSystemCommand(command));
	}

	/**
	 * Maneja cualquier otro tipo de mensaje predeterminado.
	 * Construye una respuesta que contiene el nombre de usuario del cliente, el nombre del host y el mensaje.
	 */
	private void handleDefaultMessage(String clientUsername, String clientHostname, String message, StringBuilder response) {
		response.append("Message from ").append(clientUsername).append(":").append(clientHostname).append(": ").append(message);
	}

	/**
	 * Formatea un mensaje de registro utilizando el nombre de usuario del cliente, el nombre del host y la acción realizada.
	 * @return Una cadena de mensaje de registro formateada.
	 */
	private String formatLogMessage(String clientUsername, String clientHostname, String action) {
		return clientUsername + ":" + clientHostname + " " + action;
	}

	/**
	 * Verifica si una cadena dada es un número entero positivo.
	 *
	 * @param s Cadena a verificar
	 * @return Verdadero si 's' es un entero positivo, falso en caso contrario
	 */
	private boolean isPositiveInteger(String s) {
		try {
			int num = Integer.parseInt(s);
			return num > 0;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	/**
	 * Calcula los factores primos de un entero.
	 *
	 * @param n Entero para el cual calcular los factores primos
	 * @return Lista de factores primos de 'n'
	 */
	private java.util.List<Integer> primeFactors(int n) {
		java.util.List<Integer> factors = new java.util.ArrayList<>();
		for (int i = 2; i <= n / i; i++) {
			while (n % i == 0) {
				factors.add(i);
				n /= i;
			}
		}
		if (n > 1) {
			factors.add(n);
		}
		return factors;
	}

	/**
	 * Lista las interfaces de red lógicas en el sistema anfitrión.
	 *
	 * @return Lista de interfaces de red lógicas o mensaje de error
	 */
	private String listInterfaces() {
		StringBuilder output = new StringBuilder();
		String line;
		try {
			Process p;

			// Detectar el sistema operativo para ejecutar el comando adecuado
			String os = System.getProperty("os.name").toLowerCase();

			if (os.contains("win")) {
				p = Runtime.getRuntime().exec("ipconfig");
			} else {
				p = Runtime.getRuntime().exec("ifconfig");
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));


			while ((line = br.readLine()) != null) {
				output.append(line).append("\n");
			}
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error fetching logical interfaces.";
		}
		return output.toString();
	}

	/**
	 * Lista los puertos abiertos en una dirección IP dada.
	 *
	 * @param ipAddress Dirección IP para la cual listar los puertos abiertos
	 * @return Lista de puertos abiertos o mensaje de error
	 */
	private String listOpenPorts(String ipAddress) {
		StringBuilder output = new StringBuilder();
		String line;
		try {
			Process p = Runtime.getRuntime().exec("nmap -Pn " + ipAddress);

			// Leer la salida estándar de nmap (la lista de puertos abiertos)
			BufferedReader stdBr = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
			while ((line = stdBr.readLine()) != null) {
				output.append(line).append("\n");
			}
			stdBr.close();

			// Leer la salida de error de nmap
			BufferedReader errorBr = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));
			while ((line = errorBr.readLine()) != null) {
				System.out.println("Error: " + line); // Imprime la salida de error para diagnóstico
			}
			errorBr.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error fetching open ports.";
		}
		return output.toString();
	}

	/**
	 * Ejecuta un comando del sistema y devuelve su salida.
	 *
	 * @param command Comando a ejecutar
	 * @return Salida del comando o mensaje de error
	 */
	private String executeSystemCommand(String command) {
		StringBuilder output = new StringBuilder();
		String line;
		try {
			Process p = Runtime.getRuntime().exec(command);

			// Leer la salida estándar del comando
			BufferedReader stdBr = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
			while ((line = stdBr.readLine()) != null) {
				output.append(line).append("\n");
			}
			stdBr.close();

			// Leer la salida de error del comando
			BufferedReader errorBr = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));
			while ((line = errorBr.readLine()) != null) {
				output.append("Error: ").append(line).append("\n"); // Agrega salida de error al output
			}
			errorBr.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error executing system command.";
		}
		return output.toString();
	}
}