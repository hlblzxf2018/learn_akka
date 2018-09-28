package com.lightbend.akka.sample.httpsample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import com.alibaba.fastjson.JSON;
import scala.concurrent.duration.FiniteDuration;

public class HttpServer extends AllDirectives {
    private final ActorRef userActor;
    private final ActorRef addToCartActor;


    public HttpServer(ActorRef userActor, ActorRef addToCartActor) {
        this.addToCartActor = addToCartActor;
        this.userActor = userActor;
    }


    public static void main(String[] args) throws IOException {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("routes");
        final ActorRef userActor = system.actorOf(LoginActor.props("admin", "admin"), "user");
        final ActorRef addToCartActor = system.actorOf(Props.create(AddToCartActor.class), "addToCartActor");
        // HttpApp.bindRoute expects a route being provided by HttpApp.createRoute
        final HttpServer app = new HttpServer(userActor, addToCartActor);

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Press 'ENTER' to terminate the server");
        System.in.read();

        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());
    }


    /**
     * Method that creates the routes which the server should serve. This could also
     * be defined in any other class
     *
     * @return a {@link Route} with the serving routes
     */
    Route createRoute() {
        return route(
                pathPrefix("addToCart", () -> get(() -> parameter("productId", productId -> addToCart(productId)))),
                pathPrefix("submitOrder", () -> get(() -> parameter("orderId", orderId -> submitOrder(orderId)))),
                get(() -> pathPrefix(
                        PathMatchers.segment("login").slash(PathMatchers.segment()).slash(PathMatchers.segment()),
                        (password, username) -> pathEndOrSingleSlash(() -> login(username, password)))));
    }


    //Content-Type: application/json" -X POST -d '{"items":[{"name":"hhgtg","id":42}]}'
    private Route addToCart(final String productId) {
        ActorSystem system = ActorSystem.create("httpClient");
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        //ItemBean
        ItemBean item = new ItemBean(productId);
        String productPrice = null;
        final CompletionStage<Price> postResponse = Http.get(system)
                .singleRequest(HttpRequest.create("http://127.0.0.1:8180/getPrice")
                        .withMethod(HttpMethods.POST)
                        .withEntity(HttpEntities.create(ContentTypes.APPLICATION_JSON, JSON.toJSONString(item))))
                .thenCompose(response -> {
                    final CompletionStage<HttpEntity.Strict> strictEntity = response.entity()
                            .toStrict(FiniteDuration.create(3, TimeUnit.SECONDS).toMillis(), materializer);
                    final CompletionStage<Price> price = strictEntity.thenCompose(strict -> strict.getDataBytes()
                            .runFold(ByteString.empty(), (acc, b) -> acc.concat(b), materializer)
                            .thenApply(this::parse));
                    return price;
                });
        return onSuccess(() -> postResponse, response -> {
            Price price = JSON.parseObject(response.getPrice(), Price.class);
            if (null == price) {
                return complete("add to cart failed because of get price failed");
            }
            Product product = new Product(productId, price.getPrice());
            addToCartActor.tell(product, ActorRef.noSender());
            return complete("product add to cart success, productId: " + productId + "price: " + price.getPrice());
        });
    }

    private Route login(String username, String password) {
        User user = new User(username, password);
        CompletionStage<String> result = PatternsCS.ask(userActor, user, 2000).thenApply(obj -> {
            String response = (String) obj;
            return response;
        });
        return onSuccess(() -> result, responseMessage -> {
            if (responseMessage.equals("success")) {
                return complete(StatusCodes.OK, responseMessage, Jackson.marshaller());
            } else {
                return complete(responseMessage);
            }
        });

    }

    public Route submitOrder(String orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setItems(new ArrayList<Product>());
        ActorSystem system = ActorSystem.create("httpClient");
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final CompletionStage<HttpResponse> postResponse = Http.get(system)
                .singleRequest(HttpRequest.create("http://127.0.0.1:8180/submitOrder")
                        .withMethod(HttpMethods.POST)
                        .withEntity(HttpEntities.create(ContentTypes.APPLICATION_JSON, JSON.toJSONString(order))));
        return onSuccess(() -> postResponse, response -> complete("place order success"));
    }

    public Price parse(ByteString line) {
        return new Price(line.utf8String());
    }

}
