package com.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * @Author xuxiaobing
 * @Description 单向认证客户端
 * @Date 2023/2/14 4:59 下午
 * @Version 1.0
 */
public class OneWayClient {

    public static void main(String[] args) {
        SSLSocket socket = null;
        String password = "6EcYSe9PrqT7gkChEYMG0MQG4wrY1qXb";
        try {
            // 加载信任库
            KeyStore trustStore = KeyStore.getInstance("BKS");
            // 证书和服务端保持一致
            trustStore.load(new FileInputStream(filePath("/VehicleAndroidBKS.bks")), password.toCharArray());

            // 初始化信任管理器
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            // 初始化 SSL 上下文， 因为是单向认证，客户端验证服务端，因此只需要填写trustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // 创建 SSL 套接字并连接服务器
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            socket = (SSLSocket) sslSocketFactory.createSocket("localhost", 8883);

            // 在这里可以使用 socket 进行数据传输

            // 发送请求
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // 处理响应
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Hello, Server!");

            String s = in.readLine();
            System.out.println("Received from server: " + s);
            // 关闭连接
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String filePath(String file) {
        URL resource = OneWayClient.class.getResource(file);
        if (resource != null) {
            return resource.getFile();
        }
        return null;
    }
}
