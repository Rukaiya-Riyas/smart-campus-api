package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException e) {
        return Response.status(409)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(409, "Conflict",
                        "Cannot delete room '" + e.getRoomId() + "' — it still has " + e.getSensorCount() + " sensor(s) assigned. Remove all sensors first."))
                .build();
    }
}