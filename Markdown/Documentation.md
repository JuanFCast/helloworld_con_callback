# HelloWorld-ICE Implementation: Documentation

**Software Architecture Course**

## Participants
- Juan Felipe Castillo Gomez
- Alexander Echeverry Ramirez

## Assignment of Responsibilities

- **Juan Felipe Castillo Gomez**
  - Developed server and client deployment.
  - Made modifications to the original code.
  - Prepared the documentation.

- **Alexander Echeverry Ramirez**
  - Led the server and client deployment.
  - Worked on modifying and improving the new code.
  - Designed and implemented tests to measure quality.

## Project Description
HelloWorld-ICE is a client-server communication system based on ICE. Its goal is to allow the client to communicate with the server through a command line interface. Tests have been incorporated that assess various quality attributes of the communication system, such as performance, response time, and rates of lost or unprocessed events.

## Usage

### Initial Setup:
- Java SDK 11
- Downloaded ICE middleware tools
- Gradle

### Compilation:
First run the server and then the client.

---

# `Client` Class Documentation
This class serves as the main client that communicates with a server using the Ice middleware.

## Methods

### `main(String[] args)`
Initializes communication with the server using the `config.client` file.

### `menu(Demo.PrinterPrx twoway, Scanner scanner)`
Offers the user a menu with options, such as sending a message or performing tests.

### `send(Demo.PrinterPrx twoway, Scanner scanner)`
Allows the user to send a formatted message to the server.

### `send(Demo.PrinterPrx twoway, String message)`
Variant of the previous method that accepts a message directly.

### `evaluateThroughput(Demo.PrinterPrx twoway, Scanner scanner)`
Evaluates system performance by measuring the time it takes to send a specific number of requests.

### `evaluateResponseTime(Demo.PrinterPrx twoway)`
Calculates the time the server takes to respond to a request.

### `evaluateMissingRate(Demo.PrinterPrx twoway, Scanner scanner)`
Determines the rate of lost requests when sending a specific number of requests and seeing how many do not get a response.

### `evaluateUnprocessedRate(Demo.PrinterPrx twoway, Scanner scanner)`
Calculates the rate of requests the server couldn't process.

### `getHostname()`
Gets the system's local host name.

## Notes
The class uses the Ice middleware library, and messages are formatted before being sent.

---

# `Server` Class Documentation
This class acts as the main server, listening and processing client requests through the Ice middleware.

## Methods

### `main(String[] args)`
It's the server's initial method that sets up and starts using `config.server`.

1. Initializes communication with the client.
2. If there are extra arguments after initialization, displays them.
3. Creates an adapter object named `Printer`.
4. Adds a `PrinterI` type object to the adapter with the identity `SimplePrinter`.
5. Activates the adapter to start listening to requests.
6. The server waits until instructed to shut down.

### `f(String m)`
This method receives a command in the form of a text string, executes it, and returns its output.

1. Executes the provided command using the `exec()` method of the `Runtime` class.
2. Reads and returns the command's output.
3. If an exception occurs when executing the command, it returns an error message with the exception.

## Notes
The Ice middleware library is used, and the setup is based on the `config.server` file.

---

# `PrinterI` Class
Implementation of the `Printer` interface. Manages different types of messages and performs actions based on input.

## Methods

### `printString(String s, com.zeroc.Ice.Current current)`
Implements the method of the `Printer` interface. Analyzes and processes the input string and returns an appropriate response.

- **Parameters:**
  - `s`: String in the format `clientUsername:clientHostname:message`.
  - `current`: Information about the current method invocation (provided by the Ice framework).
- **Returns:** A response based on the input message.

### Private Methods

#### `handlePositiveInteger(...)`
Handles a message containing a positive integer and calculates its prime factors, adding the result to the response.

... [Other methods as described before, using similar markdown format]

## Notes
- This implementation of the `Printer` interface offers several specific functionalities such as listing network interfaces, open ports, and system command execution.
