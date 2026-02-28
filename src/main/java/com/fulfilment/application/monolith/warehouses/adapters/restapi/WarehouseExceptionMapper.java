package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(WarehouseExceptionMapper.class);

    @jakarta.inject.Inject
    com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.error("Error occurred in Warehouse API", exception);

        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = "Internal Server Error";

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            status = webEx.getResponse().getStatus();
            message = webEx.getMessage();
        } else if (exception instanceof RuntimeException) {
            message = "A runtime error occurred";
        }

        com.fasterxml.jackson.databind.node.ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("message", message);
        errorJson.put("code", status);

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorJson)
                .build();
    }
}
