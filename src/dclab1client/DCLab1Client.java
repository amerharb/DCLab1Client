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
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
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
                pout.println(cmd);
                pout.flush();
                out.flush();
                String res;
                if (cmd.startsWith("get ")) {
                    res = in.readLine();
                    System.out.println(res);
                    if (res.equals("COPYING")) {
                        File f = new File("." + File.separator + cmd.substring(4) + "01");
                        
                        FileOutputStream fos = new FileOutputStream(f);
                        DataInputStream dis = new DataInputStream(is);

                        int fileSize = dis.readInt();
                        byte[] fileData;
                        fileData = new byte[fileSize];
                        is.read(fileData, 0, fileSize);
                        fos.write(fileData, 0, fileSize);
                    } else {
                        Thread.sleep(100);
                        while (in.ready()) {
                            res = in.readLine();
                            Thread.sleep(100);
                            System.out.println(res);
                        }
                    }
                } else {
                    res = in.readLine();
                    System.out.println(res);
                    Thread.sleep(100);
                    while (in.ready()) {
                        res = in.readLine();
                        Thread.sleep(100);
                        System.out.println(res);
                    }
                }
            }

            System.out.println("Client is closing");
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
