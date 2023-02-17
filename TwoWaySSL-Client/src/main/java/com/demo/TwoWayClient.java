package com.demo;

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
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * @Author xuxiaobing
 * @Description 双向认证 客户端
 * @Date 2023/2/14 5:06 下午
 * @Version 1.0
 */
public class TwoWayClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;
        String password = "1234567890";
        try {
            // 加载证书库
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(filePath("/client_keystore.jks")), password.toCharArray());

            // 初始化信任库
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(filePath("/client_truststore.jks")), password.toCharArray());

            // 初始化SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            // 创建SSLSocket
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);

            // 发送请求
            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            // 处理响应
            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out.println("Hello, Server!");

            String s = in.readLine();
            System.out.println("Received from server: " + s);
            // 关闭连接
            sslSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String filePath(String file) {
        URL resource = TwoWayClient.class.getResource(file);
        if (resource != null) {
            return resource.getFile();
        }
        return null;
    }
}
