package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Cannot delete room — sensors still assigned");
        error.put("roomId", e.getRoomId());
        error.put("hint", "Remove or reassign all sensors before deleting this room");
        return Response.status(409).type(MediaType.APPLICATION_JSON).entity(error).build();
    }
}