package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PullMessagesThread extends Thread {

    private boolean go = true;
    private String userFrom;

    public PullMessagesThread(String userFrom) {
        this.userFrom = userFrom;
    }

    @Override
    public void run() {

        while (go){
            Main.request(new HandleRequest() {
                @Override
                public void handleOutputStream(OutputStream outputStream) throws IOException {
                    outputStream.write(Main.PULL_MESSAGES);
                    Main.writeString(Main.userName, outputStream);
                    Main.writeString(Main.password, outputStream);
                    Main.writeString(userFrom, outputStream);
                }
                @Override
                public void handleInputStream(InputStream inputStream) throws IOException {
                    int result = inputStream.read();
                    if (result != Main.OKAY) {
                        System.out.println("User does'nt exist.");
                    } else {
                        int howManyNewMessages = inputStream.read();
                        for (int i = 0; i < howManyNewMessages; i++) {
                            String sender = Main.readString(inputStream);
                            String content = Main.readString(inputStream);
                            System.out.println(sender + ": " + content);
                        }
                    }

                }
                @Override
                public void postExecute() {

                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
        }
    }

    public void stopPulling(){
        go = false;
        interrupt();
    }
}
