package com.nems.common;

import com.englishtown.vertx.hk2.BootstrapBinder;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

/**
 * Created by NE281900 on 4/9/2016.
 */
public class JerseyVerticle extends AbstractVerticle {
    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
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

        vertx.deployVerticle("java-hk2:" + JerseyVerticle.class.getCanonicalName(), options, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.completeExceptionally(result.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}
