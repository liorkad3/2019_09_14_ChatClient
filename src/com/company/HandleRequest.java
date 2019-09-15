package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface HandleRequest {
    void handleInputStream(InputStream inputStream) throws IOException;
    void handleOutputStream(OutputStream outputStream) throws IOException;
    void  postExecute();
}
