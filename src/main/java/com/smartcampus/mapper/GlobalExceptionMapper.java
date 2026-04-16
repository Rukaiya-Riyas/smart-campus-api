package com.smartcampus.mapper;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace SERVER-SIDE only — never expose it to the client
        logger.log(Level.SEVERE, "Unhandled exception caught by GlobalExceptionMapper", exception);

        // Return a generic, non-revealing error message to the client
        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator. " +
                        "No internal details are exposed for security reasons."
        );
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}