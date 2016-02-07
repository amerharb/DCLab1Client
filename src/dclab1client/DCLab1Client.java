package dclab1client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DCLab1Client
{

    private static final int PORT = 8000;
    private static final String SERVER = "127.0.0.1";

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

        try {
            boolean reconnect = false;
            boolean connected = false;

            // create data input/output streams
            //we will asume at the first that these value are working server and port
            String prevWorkingServer = server;
            int prevWrokingPort = port;

            CommandReader R = new CommandReader();
            R.start();

            CONNECT_TO_SERVER:
            do {
                System.out.println("connecting to: " + server + ":" + port);

                reconnect = false;
                Socket socket = null;

                try {
                    socket = new Socket(server, port);
                    socket.setSoTimeout(10000); //10 sec max for read line wait time

                    //at this point the server and port seems working so we keep them for reconnect
                    prevWorkingServer = server;
                    prevWrokingPort = port;

                    connected = socket.isConnected();

                } catch (ConnectException | UnknownHostException e) {
                    System.out.println(e);
                    if (prevWorkingServer != null) {
                        server = prevWorkingServer;
                    }
                    if (prevWrokingPort != 0) {
                        port = prevWrokingPort;
                    }
                    connected = false;
                }

                // create Scanner object to read command
                ///Scanner sc = new Scanner(System.in);
                InputStream is = null;
                InputStreamReader isr;
                BufferedReader br = null;

                OutputStream os;
                DataOutputStream out = null;
                PrintStream ps = null;

                if (connected) {
                    is = socket.getInputStream();
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    os = socket.getOutputStream();
                    out = new DataOutputStream(os);
                    ps = new PrintStream(out);
                }

                COMMANDS:
                while (true) {
                    if (connected) {
                        System.out.print(server + ":" + port + ">");
                    } else {
                        System.out.print(">");
                    }

                    String cmd;
                    long lastRespons = System.currentTimeMillis(); // it start count from the moment we are waiting command
                    while (!R.hasCommand) {
                        if (connected && br.ready()) {
                            String r = br.readLine();
                            lastRespons = System.currentTimeMillis();
                            if (r.equals("PING")) {
                                ps.println("AKG");
                                ps.flush();
                            } else { //server has something else but i am not waiting anythign so its better to empty the buffer

                            }
                        }
                        if (connected && System.currentTimeMillis() - lastRespons > 15000) { //15 sec no ping
                            System.out.println("No reponse from Server for more than 15 sec");

                            reconnect = true;
                            break COMMANDS;
                        }
                        Thread.sleep(10);
                    }
                    cmd = R.getCommand();

                    //check internal command first
                    String res;
                    if (cmd.trim().isEmpty()) { //read from server if there is any msg
                        if (connected) {
                            if (!br.ready()) {
                                Thread.sleep(50);
                            }
                            while (br.ready()) {
                                res = br.readLine();
                                lastRespons = System.currentTimeMillis();
                                if (!br.ready()) {
                                    Thread.sleep(500);
                                }
                                System.out.println(res);
                            }
                        }
                    } else if (cmd.startsWith("exit")) {
                        reconnect = false;
                        break;
                    } else if (cmd.startsWith("connect")) {
                        String[] arg = cmd.substring(7).trim().split(" ");
                        if (arg.length > 0) {
                            if (!arg[0].isEmpty()) {
                                server = arg[0];
                            }
                        }
                        if (arg.length > 1) {
                            try {
                                port = Integer.parseInt(arg[1]);
                            } catch (NumberFormatException e) {
                                System.out.println("Port number is invalid, default or old port number will be used instead");
                                port = prevWrokingPort;
                            } catch (Exception e) {
                                System.out.println("test");
                            }
                        }
                        reconnect = true;
                        break COMMANDS;
                    } else if (connected) {
                        // send command to sever
                        ps.println(cmd);
                        ps.flush();
                        out.flush();
                        Thread.sleep(50); //wait unitll server send reply
                        GET:
                        if (cmd.startsWith("get ")) {

                            if (br.ready()) {
                                res = br.readLine();
                                lastRespons = System.currentTimeMillis();
                            } else {
                                break GET;
                            }
                            System.out.println(res);
                            COPYING:
                            if (res.startsWith("COPYING ")) {
                                long fileSize = Long.parseLong(res.substring(8));

                                String newFileName = "." + File.separator + cmd.substring(4) + ".bak";
                                String suffex = "";
                                int i = 1;
                                while (new File(newFileName + suffex).exists()) {
                                    suffex = String.valueOf(i);
                                    i++;
                                }
                                newFileName = newFileName + suffex;
                                File f = new File(newFileName);

                                FileOutputStream fos = new FileOutputStream(f);
                                DataInputStream dis = new DataInputStream(is);

//                        Option 2 to write its faster than old way
                                //wait for server to get ready
                                long timeOut = System.currentTimeMillis();
                                while (dis.available() < 1) {
                                    Thread.sleep(100);
                                    if ((System.currentTimeMillis() - timeOut) > 10000) {//wait for 10 sec
                                        System.out.println("time out no file comes from server");
                                        break COPYING;
                                    }
                                }

                                System.out.println("available: " + dis.available());
                                System.out.println(br.ready());

                                byte[] b;
                                final int defBufferSize = 8192;
                                if (fileSize < defBufferSize) {
                                    b = new byte[(int) fileSize];
                                } else {
                                    b = new byte[defBufferSize]; //max of buffer
                                }

                                int r;
                                long remainFileSize = fileSize;
                                int x = b.length;
                                while (remainFileSize > 0 && dis.available() > 0 && (r = dis.read(b, 0, x)) > 0) {
                                    lastRespons = System.currentTimeMillis();
                                    fos.write(b, 0, r);
                                    System.out.println("avaiilable: " + dis.available());
                                    remainFileSize -= r;
                                    x = remainFileSize < b.length ? (int) remainFileSize : b.length;
                                    Thread.sleep(10);
                                }

                                fos.close();
                                b = null;

//                      old way to write file does not work with big file
//                        int fileSize = 0;
//                        byte b;
//                        byte[] stack = new byte[1024];
//
//                        while (dis.available() > 0) {
//                            b = dis.readByte();
//                            stack[fileSize] = b;
//                            fileSize++;
//                            if (fileSize == stack.length) {//expand stack
//                                byte[] temp = new byte[stack.length * 2];
//                                System.arraycopy(stack, 0, temp, 0, fileSize);
//                                stack = new byte[stack.length * 2];
//                                System.arraycopy(temp, 0, stack, 0, fileSize);
//                                temp = null;
//                            }
//                        }
//
//                        byte[] fileData = new byte[fileSize];
//                        System.arraycopy(stack, 0, fileData, 0, fileSize);
//                        stack = null;
//                        //fileData = new byte[fileSize];
//                        //is.read(fileData, 0, fileSize);
//                        fos.write(fileData);
                            } else { // if the response is not equal COPYING then there must a String reply
                                if (!br.ready()) {
                                    Thread.sleep(50);
                                }
                                while (br.ready()) {
                                    res = br.readLine();
                                    lastRespons = System.currentTimeMillis();
                                    if (!br.ready()) {
                                        Thread.sleep(50);
                                    }
                                    if (res.equals("PING")) { //ignore ping signal
                                        ps.println("AKG");
                                        ps.flush();
                                    } else {
                                        System.out.println(res);
                                    }
                                }
                            }
                        } else {

                            if (!br.ready()) {
                                Thread.sleep(50);
                            }
                            while (br.ready()) {
                                res = br.readLine();
                                lastRespons = System.currentTimeMillis();
                                if (!br.ready()) {
                                    Thread.sleep(50);
                                }
                                if (res.equals("PING")) { //ignore ping signal
                                    ps.println("AKG");
                                    ps.flush();
                                } else {
                                    System.out.println(res);
                                }
                            }
                        }
                    } else { //not internal command and its not connacted
                        System.out.println("command not sent, you are not connacted!!");
                    }

                }

                System.out.println("Client is closing");
                if (connected) {
                    ps.close();
                    br.close();
                    out.close();
                    socket.close();
                    connected = false;
                }
            } while (reconnect);
            System.out.println("Client Shutdown");

        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
