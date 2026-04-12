package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(DataStore.rooms.values())).build();
    }

    // GET /api/v1/rooms/{roomId}
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found");
            error.put("roomId", roomId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(room).build();
    }

    // POST /api/v1/rooms
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room ID is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room with this ID already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        // Safety check — cannot delete room if it still has sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        DataStore.rooms.remove(roomId);
        Map<String, String> msg = new HashMap<>();
        msg.put("message", "Room " + roomId + " deleted successfully");
        return Response.ok(msg).build();
    }
}