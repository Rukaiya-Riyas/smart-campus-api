package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";
    public static final long START_TIME = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {
        ResourceConfig config = new ResourceConfig().packages("com.smartcampus");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
        System.out.println("===========================================");
        System.out.println(" Smart Campus API is running!");
        System.out.println(" URL: http://localhost:8080/api/v1");
        System.out.println("===========================================");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.shutdown();
    }
}