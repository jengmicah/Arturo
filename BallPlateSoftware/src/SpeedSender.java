
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * This sends packets with the x-y coordinates (in inches) of the object being tracked to the indicated server port, which
 * is intended to be a userInput into SystemModeler
 * 
 * In order to find the command you need to enter into the terminal/cmd, 
 * 1) Open up Debug Perspective in Eclipse
 * 2) Right click "<terminated, exit value: 0>..."
 * 3) Click Properties
 * 4) Copy the Command Line statement
 * 
 * Command on Micah's computer:
 * "C:\Program Files\Java\jre1.8.0_91\bin\javaw.exe" -Djava.library.path=C:\lib\opencv\build\java\x64;C:\lib\opencv\build\java\x64 -Dfile.encoding=Cp1252 -classpath C:\Projects\Arturo\BallPlateSoftware\bin;C:\lib\opencv\build\java\opencv-310.jar SpeedSender 127.0.0.1 12345
 */

public class SpeedSender {
	// Comment out this main method to communicate with SysMo
	public static void main(String[] args){
		/*
		 * This main method runs without sending data over a network (used for testing purposes)
		 */
		Setup f = new Setup(); // Set up the windows and run Processor as well
		double[] userInput = new double[2];
		while(f.isRun() && f.getWebcam().isOpened() && f.getProc() != null){ // While the windows and webcam are open
			userInput = f.getProc().process();
			if(f.getProc().getFoundBall()) {
				f.getConsole().append("x: " + (float)userInput[0] + "     y: " + (float)userInput[1] + "\n");
			}
		}			
	}
//		public static void main(String[] args) throws IOException, InterruptedException {
//	
//			Setup f = new Setup(); //begin setup
//	
//			if (args.length != 2) { // Validate that the user input server IP and port
//				f.getConsole().append("Usage: java SpeedSender <host name> <port number>\n");
//				System.exit(1);
//			}
//			String host = args[0]; // First set of numbers (127.0.0.1) is the IP of the host that the server is running on
//			int port = Integer.parseInt(args[1]); // Second set (#####) is the port that the server is listening to
//	
//	
//			try (Socket socket = new Socket(host, port); // Establish connection to specified port	
//					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);){ // Open output stream to the socket
//	
//				f.getConsole().append("Connected to " + host + " on port " + port + "\n");
//				double[] userInput;
//				String hexString, command, tcppackage; // Variables for socket-server communication			
//	
//				while(f.isRun() && f.getWebcam().isOpened() && f.getProc() != null){ // Make sure all components are running
//					userInput = f.getProc().process(); // Get inputs
//					if(f.getProc().getFoundBall()) { // If ball is on screen, send coordinates
//	
//						/*
//						 * Send ball x coordinate
//						 */
//						tcppackage = "setInputValues({\"x\", " + userInput[0] + "})"; // Sets destination + input value
//						hexString = String.format("01030000%02X000000", tcppackage.length()); // Sets data in hex
//						command = hexToASCII(hexString)+ tcppackage; // Socket client sends data to server in ASCII codes
//						out.print(command); // Send data to server
//						out.flush(); // Make sure all data is sent (sends out all data in buffer)
//						f.getConsole().append("x: " + (float)userInput[0]);
//	
//						/*
//						 * Send ball y coordinate
//						 */
//						tcppackage = "setInputValues({\"y\", " + userInput[1] + "})";
//						hexString = String.format("01030000%02X000000", tcppackage.length());
//						command = hexToASCII(hexString)+ tcppackage;
//						out.print(command);
//						out.flush();
//						f.getConsole().append("     y: " + (float)userInput[1] + "\n");
//	
//						Thread.sleep(10);
//					}
//				}
//				out.close();
//				socket.close();
//				f.getConsole().append("Exiting");
//			} catch (UnknownHostException e) {
//				f.getConsole().append("Don't know about host " + host);
//				System.exit(1);
//			} catch (IOException e) {
//				f.getConsole().append("Couldn't get I/O for the connection to " + host);
//				System.exit(1);
//			} 
//		}

	private static String hexToASCII(String hexValue){ //uh.
		StringBuilder output = new StringBuilder("");
		for (int i = 0; i < hexValue.length(); i += 2)
		{
			String str = hexValue.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}
		return output.toString();
	}
}