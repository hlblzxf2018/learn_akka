package com.lightbend.akka.sample.httpsample;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class PriceHttpApp extends HttpApp {
    final private Map<String, Map<JsonNode, JsonNode>> state = new ConcurrentHashMap<>();
    @Override
    protected Route routes() {
        return createRoute();
    }
    private Route createRoute() {
        // fixed route to update state
        Route fixedRoute = post(() ->
                pathSingleSlash(() ->
                        entity(Jackson.unmarshaller(MockDefinition.class), mock -> {
                            Map<JsonNode, JsonNode> mappings = new HashMap<>();
                            int size = Math.min(mock.getRequests().size(), mock.getResponses().size());
                            for (int i = 0; i < size; i++) {
                                mappings.put(mock.getRequests().get(i), mock.getResponses().get(i));
                            }
                            state.put(mock.getPath(), mappings);
                            return complete("ok");
                        })
                )
        );

        // dynamic routing based on current state
        Route dynamicRoute = post(() ->
                state.entrySet().stream().map(mock ->
                        path(mock.getKey(), () ->
                                entity(Jackson.unmarshaller(JsonNode.class), input ->
                                        complete(StatusCodes.OK, mock.getValue().get(input), Jackson.marshaller())
                                )
                        )
                ).reduce(reject(), Route::orElse)
        );

        return route(fixedRoute, dynamicRoute);
    }

    /**
     * Content-Type : application/json
     *   {
     *      "path": "getPrice",
     *      "requests": [
     *        {"itemId": "1001"},
     *        {"itemId": "1002"}
     *      ],
     *      "responses": [
     *        {"price": "1000"},
     *        {"price": "2000"}
     *      ]
     *    }
     */

    private static class MockDefinition {
        private final String path;
        private final List<JsonNode> requests;
        private final List<JsonNode> responses;

        public MockDefinition(@JsonProperty("path") String path,
                              @JsonProperty("requests") List<JsonNode> requests,
                              @JsonProperty("responses") List<JsonNode> responses) {
            this.path = path;
            this.requests = requests;
            this.responses = responses;
        }

        public String getPath() {
            return path;
        }

        public List<JsonNode> getRequests() {
            return requests;
        }

        public List<JsonNode> getResponses() {
            return responses;
        }
    }

    public static void main(String[] args){
        final PriceHttpApp priceServer = new PriceHttpApp();
        try {
            priceServer.startServer("localhost", 8180);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
