package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
                422,
                "Unprocessable Entity",
                "The roomId '" + e.getRoomId() + "' referenced in the request body does not exist. " +
                        "The JSON payload is syntactically valid, but the referenced resource cannot be found. " +
                        "Please create the room first before registering a sensor to it."
        );
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}