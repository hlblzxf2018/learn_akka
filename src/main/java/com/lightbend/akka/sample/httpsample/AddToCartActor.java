package com.lightbend.akka.sample.httpsample;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.Status;

import java.util.ArrayList;
import java.util.List;

public class AddToCartActor extends AbstractLoggingActor {
    public final Order	order		= new Order();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Product.class, product -> {
            log().info("product add to cart success, productId : {}, product Price {}", product.getId(), product.getPrice());
            List<Product> products	= new ArrayList<>();
            products.add(product);
            order.setItems(products);
            order.setId(String.valueOf(product.hashCode()));
            sender().tell(new Status.Success("success"), self());
        }).matchAny(o -> {
            sender().tell(new Status.Failure(new ClassNotFoundException()), self());
        }).build();
    }
}
