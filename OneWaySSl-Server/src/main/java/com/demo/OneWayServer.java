package com.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * @Author xuxiaobing
 * @Description 单向认证服务端
 * @Date 2023/2/14 4:56 下午
 * @Version 1.0
 */
public class OneWayServer {

    public static void main(String[] args) {
        SSLServerSocket serverSocket = null;
        String password = "1234567890";
        try {
            // 加载密钥库
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(filePath("/server.jks")), password.toCharArray());

            // 初始化密钥管理器
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password.toCharArray());

            // 初始化 SSL 上下文  因为单向认证，服务端只需要填写keyManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // 创建 SSL 服务器套接字
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(8000);

            System.out.println("Waiting client connecting...");
            while(true) {
                // 等待客户端连接
                SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
                try {
                    // 处理客户端请求
                    BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received from client: " + inputLine);
                        out.println(inputLine);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 关闭连接
                    try {
                        sslSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String filePath(String file) {
        URL resource = OneWayServer.class.getResource(file);
        if (resource != null) {
            return resource.getFile();
        }
        return null;
    }
}
