package com.demo;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * @Author xuxiaobing
 * @Description 双向认证 服务端
 * @Date 2023/2/14 5:05 下午
 * @Version 1.0
 */
public class TwoWayServer {
    public static void main(String[] args) {
        int port = 1234;
        String password = "1234567890";
        try {
            System.out.println("TwoWay Server starting...");
            // 加载证书库
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(filePath("/server_keystore.jks")), password.toCharArray());

            // 初始化信任库
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(filePath("/server_truststore.jks")), password.toCharArray());

            // 初始化SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            // 创建SSLServerSocket
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

            // 设置需要客户端认证
            sslServerSocket.setNeedClientAuth(true);
            System.out.println("Waiting client connecting...");
            while(true) {
                // 等待客户端连接
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
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
                    break;
                } finally {
                    // 关闭连接
                    sslSocket.close();
                }
            }
            sslServerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public static String filePath(String file) {
        URL resource = TwoWayServer.class.getResource(file);
        if (resource != null) {
            return resource.getFile();
        }
        return null;
    }
}

