package com.hexaware.cozy_heaven.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hexaware.cozy_heaven.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Integer> {
	public List<Room> findByHotelHotelId(int hotelId);

	@Query("SELECT r FROM Room r WHERE r.hotel.hotelId = :hotelId " + "AND r.available = true "
			+ "AND r.roomId NOT IN (SELECT b.room.roomId FROM Booking b " + "WHERE b.status = 'CONFIRMED' "
			+ "AND b.checkIn < :checkOut AND b.checkOut > :checkIn)")
	List<Room> findAvailableRooms(@Param("hotelId") int hotelId, @Param("checkIn") LocalDate checkIn,
			@Param("checkOut") LocalDate checkOut);

	@Query("SELECT COUNT(b) = 0 FROM Booking b WHERE b.room.roomId = :roomId " + "AND b.status = 'CONFIRMED' "
			+ "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
	boolean isRoomAvailableForDates(@Param("roomId") int roomId, @Param("checkIn") LocalDate checkIn,
			@Param("checkOut") LocalDate checkOut);

	boolean existsByHotelHotelIdAndRoomNumber(int hotelId, int roomNumber);
}
