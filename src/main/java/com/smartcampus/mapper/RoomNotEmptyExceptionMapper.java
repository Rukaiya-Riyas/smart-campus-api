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
        ErrorResponse error = new ErrorResponse(
                409,
                "Conflict",
                "Room '" + e.getRoomId() + "' cannot be deleted — it has " +
                        e.getSensorCount() + " active sensor(s) still assigned. " +
                        "Remove or reassign all sensors before decommissioning this room."
        );
        return Response.status(409)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}