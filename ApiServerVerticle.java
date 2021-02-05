public class ApiServerVerticle extends AbastractVerticle {
    logger
    httpPort = 58080;
    
    start {
        httpServer();
    }
    
    private void httpServer() {
        create httpserver
            .requestHandler(this::reqHandler)
            .listen()
    }
    
    private void reqHandler(HttpServerRequest request) {
        String uri = request.uri();
        
        request.bodyHandler(buf -> {
            if (uri.equals("/snmp")) {
                String packet = buf.toString();
                JsonObject ebParam = new JsonObject(packet);
                
                vertx.eventBus().<JsonObject>request("snmp.poller.walk", ebParam, reply -> {
                    if (reply.succeeded()) {
                        request.response().end(reply.result().body().encode());
                    } else {
                        ...end("error");
                    }
                });
            }
            
            if snmp-data-access {
                snmp-data-access eb send
                
                end ok
            }
            
            if snmp-scheduler-manual {
                snmp.scheduler.manual eb send
                
                end ok 
            }
        });
    }
}
