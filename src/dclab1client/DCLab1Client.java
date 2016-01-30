package dclab1client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DCLab1Client
{

    private static final int PORT = 8000;
    private static String SERVER = "127.0.0.1";

    public static void main(String[] args) throws InterruptedException
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

    public DCLab1Client(String server, int port) throws InterruptedException
    {
        DataInputStream inputFromServer;
        DataOutputStream outputToServer;

        try {
            // create data input/output streams
            Socket socket = new Socket(server, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            PrintStream pout = new PrintStream(out);

            // create Scanner object to read radius from the keyboard
            Scanner sc = new Scanner(System.in);

            while (true) {
                // get command
                System.out.print(">");
                String cmd = sc.nextLine();

                if (cmd.startsWith("exit")) {
                    break;
                }
                // send radius to sever
                System.out.println("sending command " + cmd);
                pout.println(cmd);
                pout.flush();
                out.flush();

                System.out.println("before ready");
                int count = 0;
                while (!in.ready() && count < 3) { //Wait Server Answer 5 second
                    Thread.sleep(500);
                    count++;
                }
                System.out.println("after ready count is = " + count);
                if (count >= 3) {
                    System.out.println("Time run out");
                } else {
                    String res = in.readLine();
                    System.out.println("server msg is " + res);
                }
            }

            System.out.println("Client is closing");
            pout.close();
            in.close();
            out.close();
            socket.close();
            Thread.sleep(5000);
            System.out.println("Client closed");

        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
