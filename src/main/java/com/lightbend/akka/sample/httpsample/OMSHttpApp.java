package com.lightbend.akka.sample.httpsample;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OMSHttpApp extends HttpApp {
    protected Route routes() {
        return path("submitOrder", () ->
                post(() ->
                        complete("<h1>success submit to oms</h1>")
                )
        );
    }

    public static void main(String[] args) {
        final OMSHttpApp omsServer = new OMSHttpApp();
        try {
            omsServer.startServer("localhost", 8280);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
