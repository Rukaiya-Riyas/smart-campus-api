package com.smartcampus.resource;

import com.smartcampus.DataStore;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    // Track when the server started
    private static final long START_TIME = System.currentTimeMillis();

    @GET
    public Response discover() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api", "Smart Campus Sensor & Room Management API");
        info.put("version", "1.0");
        info.put("status", "UP");
        info.put("contact", "admin@smartcampus.ac.uk");
        info.put("timestamp", System.currentTimeMillis());
        info.put("uptimeMs", System.currentTimeMillis() - START_TIME);

        // Statistics snapshot
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRooms", DataStore.rooms.size());
        stats.put("totalSensors", DataStore.sensors.size());
        info.put("stats", stats);

        // HATEOAS-style resource links
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",    Map.of("href", "/api/v1",             "method", "GET"));
        links.put("ro"
                + "oms",   Map.of("href", "/api/v1/rooms",       "method", "GET"));
        links.put("sensors", Map.of("href", "/api/v1/sensors",     "method", "GET"));
        links.put("filteredSensors", Map.of("href", "/api/v1/sensors?type={type}", "method", "GET"));
        info.put("_links", links);

        return Response.ok(info).build();
    }
}