package ru.nya.push.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class BusinessRuntimeExceptionMapper implements ExceptionMapper<BusinessRuntimeException> {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessRuntimeExceptionMapper.class);

    private static final int SHOW_ERROR_MESSAGE = 601;

    @Override
    public Response toResponse(BusinessRuntimeException e) {
        LOG.info("Business exception: {}", e.getMessage());
        LOG.debug("", e);
        return Response.status(SHOW_ERROR_MESSAGE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
    }
}