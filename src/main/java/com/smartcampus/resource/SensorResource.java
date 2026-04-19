package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors
    // GET /api/v1/sensors?type=CO2
    // GET /api/v1/sensors?status=ACTIVE
    // GET /api/v1/sensors?type=CO2&status=ACTIVE
    @GET
    public Response getAllSensors(
            @QueryParam("type") String type,
            @QueryParam("status") String status) {

        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        return Response.ok(result).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(new ErrorResponse(404, "Not Found", "Sensor '" + sensorId + "' does not exist"))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // POST /api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(400)
                    .entity(new ErrorResponse(400, "Bad Request", "Sensor ID is required"))
                    .build();
        }
        if (sensor.getRoomId() == null || !DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }
        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(409)
                    .entity(new ErrorResponse(409, "Conflict", "Sensor '" + sensor.getId() + "' already exists"))
                    .build();
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        DataStore.sensors.put(sensor.getId(), sensor);
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        DataStore.readings.put(sensor.getId(), new ArrayList<>());
        return Response.status(201)
                .header("Location", "/api/v1/sensors/" + sensor.getId())
                .entity(sensor)
                .build();
    }

    // PUT /api/v1/sensors/{sensorId}
    // Allows updating a sensor's status or roomId (e.g. change from MAINTENANCE back to ACTIVE)
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updates) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(new ErrorResponse(404, "Not Found", "Sensor '" + sensorId + "' does not exist"))
                    .build();
        }
        if (updates.getStatus() != null && !updates.getStatus().isBlank()) {
            sensor.setStatus(updates.getStatus());
        }
        if (updates.getType() != null && !updates.getType().isBlank()) {
            sensor.setType(updates.getType());
        }
        return Response.ok(sensor).build();
    }

    // Sub-resource locator - delegates reading management to SensorReadingResource
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}