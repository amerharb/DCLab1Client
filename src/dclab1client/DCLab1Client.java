package dclab1client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DCLab1Client
{

    private static final int PORT = 8000;
    private static String SERVER = "127.0.0.1";

    public static void main(String[] args)
    {
        String server = SERVER;
        int port = PORT;

        if (args.length >= 1) {
            server = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        new DCLab1Client(server, port);
    }

    public DCLab1Client(String server, int port)
    {
        DataInputStream inputFromServer;
        DataOutputStream outputToServer;

        try {
            // create a socket to connect to the server
            Socket socket = new Socket(server, port);

            // create data input/output streams
            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());

            // create Scanner object to read radius from the keyboard
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter radius = ");
            while (sc.hasNextDouble()) {
                // get a radius
                double radius = sc.nextDouble();

                // send radius to sever
                outputToServer.writeDouble(radius);
                outputToServer.flush();

                // get area from server
                //double area = inputFromServer.readDouble();
                String s = inputFromServer.readUTF();
                System.out.print("server msg is " + s);
            }
            inputFromServer.close();
            outputToServer.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
