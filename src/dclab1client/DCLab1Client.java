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

        try (Socket socket = new Socket(server, port)){
            // create data input/output streams
            //inputFromServer = new DataInputStream(socket.getInputStream());
            //outputToServer = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            PrintStream pout = new PrintStream(out);

            // create Scanner object to read radius from the keyboard
            Scanner sc = new Scanner(System.in);
            System.out.print(">");

            while (sc.hasNext()) {
                // get command
                String cmd = sc.nextLine();
                
                // send radius to sever
                System.out.println("sending command " + cmd);
                pout.println(cmd);
                pout.flush();
                out.flush();

                // get area from server
                //String res = in.readLine();
                //System.out.print("server msg is " + res);
            }
            pout.close();
            in.close();
            out.close();
            socket.close();
            System.out.println("Client closed");
        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
