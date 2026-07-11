package com.hexaware.cozy_heaven.service;

import java.time.LocalDate;
import java.util.List;

import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.entity.Room;

public interface RoomService {

	Room addRoom(int hotelId, RoomDTO dto);

	Room updateRoom(int roomId, RoomDTO dto);

	Room getRoomById(int roomId);

	List<Room> getAllRooms();

	boolean deleteRoom(int roomId);

	List<Room> getRoomsByHotel(int hotelId);

	List<Room> getAvailableRooms(int hotelId, LocalDate checkIn, LocalDate checkOut);

	void blockRoom(int roomId);

	void unblockRoom(int roomId);
}
