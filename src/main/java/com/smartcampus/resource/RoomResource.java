package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(DataStore.rooms.values())).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(new ErrorResponse(404, "Not Found", "Room '" + roomId + "' does not exist"))
                    .build();
        }
        return Response.ok(room).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(400)
                    .entity(new ErrorResponse(400, "Bad Request", "Room ID is required"))
                    .build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(400)
                    .entity(new ErrorResponse(400, "Bad Request", "Room name is required"))
                    .build();
        }
        if (room.getCapacity() <= 0) {
            return Response.status(400)
                    .entity(new ErrorResponse(400, "Bad Request", "Capacity must be greater than zero"))
                    .build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(409)
                    .entity(new ErrorResponse(409, "Conflict", "Room '" + room.getId() + "' already exists"))
                    .build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(201)
                .header("Location", "/api/v1/rooms/" + room.getId())
                .entity(room)
                .build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(new ErrorResponse(404, "Not Found", "Room '" + roomId + "' does not exist"))
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }

    @GET
    @Path("/crash")
    public Response triggerCrash() {
        String s = null;
        s.length();
        return Response.ok().build();
    }
}
