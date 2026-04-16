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
        ErrorResponse error = new ErrorResponse(
                403,
                "Forbidden",
                "Sensor '" + e.getSensorId() + "' is currently under MAINTENANCE and cannot accept new readings. " +
                        "Update the sensor status to ACTIVE before posting readings."
        );
        return Response.status(403)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}