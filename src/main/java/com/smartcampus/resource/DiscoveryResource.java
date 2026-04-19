package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.Main;

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

    @GET
    public Response discover() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api", "Smart Campus Sensor & Room Management API");
        info.put("version", "1.0");
        info.put("status", "UP");
        info.put("contact", "admin@smartcampus.ac.uk");
        info.put("timestamp", System.currentTimeMillis());
        info.put("uptimeMs", System.currentTimeMillis() - Main.START_TIME);

        // Live statistics
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("totalRooms", DataStore.rooms.size());
        stats.put("totalSensors", DataStore.sensors.size());
        info.put("stats", stats);

        // HATEOAS-style navigable links
        Map<String, Object> roomLink = new LinkedHashMap<>();
        roomLink.put("href", "/api/v1/rooms");
        roomLink.put("method", "GET");

        Map<String, Object> sensorLink = new LinkedHashMap<>();
        sensorLink.put("href", "/api/v1/sensors");
        sensorLink.put("method", "GET");

        Map<String, Object> filterLink = new LinkedHashMap<>();
        filterLink.put("href", "/api/v1/sensors?type={type}&status={status}");
        filterLink.put("method", "GET");

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",    Map.of("href", "/api/v1", "method", "GET"));
        links.put("rooms",   roomLink);
        links.put("sensors", sensorLink);
        links.put("filteredSensors", filterLink);
        info.put("_links", links);

        return Response.ok(info).build();
    }
}