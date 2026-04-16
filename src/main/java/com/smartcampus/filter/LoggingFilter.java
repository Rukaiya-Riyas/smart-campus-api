package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    // Property key used to pass the start time between request and response filters
    private static final String START_TIME_PROPERTY = "requestStartTime";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Record start time for response-time calculation
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());

        logger.info(String.format(
                "[REQUEST]  %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
        long duration = System.currentTimeMillis() - startTime;

        logger.info(String.format(
                "[RESPONSE] %s %s -> Status: %d (%dms)",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus(),
                duration
        ));
    }
}