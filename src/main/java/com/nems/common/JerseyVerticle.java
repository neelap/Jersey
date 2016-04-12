package com.nems.common;

import com.englishtown.vertx.hk2.BootstrapBinder;
import com.englishtown.vertx.hk2.HK2VerticleFactory;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by NE281900 on 4/9/2016.
 */
public class JerseyVerticle {

    private final static Logger logger = LoggerFactory.getLogger(JerseyVerticle.class);

    public static void main(String[] args) throws Exception {

        Vertx vertx = Vertx.vertx();

        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        Optional<VerticleFactory> factory = vertx.verticleFactories()
                .stream()
                .filter(f -> f instanceof HK2VerticleFactory)
                .findFirst();

        if (!factory.isPresent()) {
            throw new IllegalStateException("The HK2Verticle factory is not registered");
        }

        JsonObject config = new JsonObject()
                .put("hk2_binder", new JsonArray().add(BootstrapBinder.class.getCanonicalName()))
                .put("jersey", new JsonObject()
                        .put("host", "localhost")
                        .put("port", 9008)
                        .put("resources", new JsonArray()
                                .add("com.nems.revctx.resources.submission")
                        )
                );

        CompletableFuture<String> future = new CompletableFuture<>();
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        options.setWorker(true);

        vertx.deployVerticle("java-hk2:" + com.englishtown.vertx.jersey.JerseyVerticle.class.getCanonicalName(), options, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.completeExceptionally(result.cause());
            }
        });

        try {
            String deploymentID = future.get();
            logger.info("Deployed jersey verticle: " + deploymentID);

        } catch (Throwable t) {
            logger.error("Error deploying verticle: " + t.getMessage());
            t.printStackTrace();
        }

        httpServer.requestHandler(router::accept).listen(9000);

    }
}
