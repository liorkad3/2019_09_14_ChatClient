package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {

    public static final String SERVER_IP = "127.0.0.1";
    public static final int PORT = 3000;
    public static final int SIGNUP = 100;
    public static final int LOGIN = 101;
    public static final int SEND_MESSAGE = 102;
    public static final int PULL_MESSAGES = 103;
    public static final int PULL_ALLMESSAGES = 104;
    public static final int OKAY = 200;
    public static final int FAILURE = 201;
    public static boolean valid = false;
    public static String userName, password;

    public static void main(String[] args) {
        menu();

    }

    public static void menu(){
        System.out.print("type 1 for signup or 2 for login: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        final boolean signupOrLogin = input.equals("1");
        System.out.print("type username: ");
        userName = scanner.nextLine();
        System.out.print("type password: ");
        password = scanner.nextLine();
        if(userName.length() == 0 || password.length() == 0){
            return;
        }
        request(new HandleRequest() {

            @Override
            public void handleOutputStream(OutputStream outputStream) throws IOException {
                outputStream.write(signupOrLogin ? SIGNUP : LOGIN);
                writeString(userName, outputStream);
                writeString(password, outputStream);
            }

            @Override
            public void handleInputStream(InputStream inputStream) throws IOException {
                int result = inputStream.read();
                if(result == OKAY){
                    valid = true;
                }else{
                    System.out.println("invalid username or password");
                    valid = false;
                }
            }

            @Override
            public void postExecute() {
                if(valid)
                    chatListMenu();
                else
                    menu();
            }
        });
    }


    public static void chatListMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Type 'username' to start chat with: ");
        String userTo = scanner.nextLine();
        if (userTo.equals("-e")) {
            menu();
        }
        request(new HandleRequest() {
            @Override
            public void handleOutputStream(OutputStream outputStream) throws IOException {
                outputStream.write(PULL_ALLMESSAGES);
                writeString(userName, outputStream);
                writeString(password, outputStream);
                writeString(userTo, outputStream);
            }

            @Override
            public void handleInputStream(InputStream inputStream) throws IOException {
                int result = inputStream.read();
                if (result != OKAY) {
                    System.out.println("User does'nt exist.");
                    valid = false;
                } else {
                    valid = true;
                    int howManyNewMessages = inputStream.read();
                    for (int i = 0; i < howManyNewMessages; i++) {
                        String sender = readString(inputStream);
                        String content = readString(inputStream);
                        System.out.println(sender + ": " + content);
                    }
                }

            }

            @Override
            public void postExecute() {
                if (valid)
                    chatWindow(userTo);
                else
                    chatListMenu();
            }
        });
    }

    public static void chatWindow(String userTo){
        PullMessagesThread pullMessagesThread = new PullMessagesThread(userTo);
        pullMessagesThread.start();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Message: ");
        String message = scanner.nextLine();
        if(message.equals("-e")) {
            chatListMenu();
            pullMessagesThread.stopPulling();
        }

        request(new HandleRequest() {
            @Override
            public void handleOutputStream(OutputStream outputStream) throws IOException {
                outputStream.write(SEND_MESSAGE);
                writeString(userName, outputStream);
                writeString(password, outputStream);
                writeString(userTo, outputStream);
                writeString(message, outputStream);
            }

            @Override
            public void handleInputStream(InputStream inputStream) throws IOException {
                int result = inputStream.read();
                if(result != OKAY){
                    System.out.println("error sending message");
                }
            }

            @Override
            public void postExecute() {
                chatWindow(userTo);
            }
        });
    }



        public static void request(HandleRequest handleRequest){
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
        socket = new Socket(SERVER_IP, PORT);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        handleRequest.handleOutputStream(outputStream);
        handleRequest.handleInputStream(inputStream);
        } catch (IOException e) {
        e.printStackTrace();
        }finally {
        if (inputStream != null) {
        try {
        inputStream.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        if (outputStream != null) {
        try {
        outputStream.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        if (socket != null) {
        try {
        socket.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        }
        handleRequest.postExecute();

        }


        public static String readString(InputStream inputStream) throws IOException{
        int length = inputStream.read();
        if(length == -1)
        throw new IOException("expected string length");
        byte[] buffer = new byte[length];
        int actuallyRead = inputStream.read(buffer);
        if(actuallyRead != length)
        throw new IOException("expected " + length + " bytes.");
        return new String(buffer);
        }

        public static int readInt(InputStream inputStream) throws IOException{
        byte[] intBytes = new byte[4];
        int actuallyRead = inputStream.read(intBytes);
        if(actuallyRead != 4)
        throw new IOException("expected four bytes");
        return ByteBuffer.wrap(intBytes).getInt();
        }

        public static void writeInt(OutputStream outputStream, int x) throws IOException{
        byte[] xBytes = new byte[4];
        ByteBuffer.wrap(xBytes).putInt(x);
        outputStream.write(xBytes);
        }

        public static void writeString(String s, OutputStream outputStream) throws IOException {
        byte[] bytes = s.getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
        }


}
