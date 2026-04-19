package com.smartcampus.mapper;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException e) {
        return Response.status(403)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(403, "Forbidden",
                        "Sensor '" + e.getSensorId() + "' is under MAINTENANCE and cannot accept new readings."))
                .build();
    }
}