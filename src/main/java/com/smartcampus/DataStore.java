package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    public static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    static {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("ENG-101", "Engineering Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        sensors.put(s1.getId(), s1);
        r1.getSensorIds().add(s1.getId());

        readings.put("TEMP-001", new ArrayList<>());
    }
}