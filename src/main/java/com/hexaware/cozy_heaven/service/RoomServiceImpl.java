package com.hexaware.cozy_heaven.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.entity.Hotel;
import com.hexaware.cozy_heaven.entity.Room;
import com.hexaware.cozy_heaven.exception.HotelException;
import com.hexaware.cozy_heaven.exception.RoomException;
import com.hexaware.cozy_heaven.repository.HotelRepository;
import com.hexaware.cozy_heaven.repository.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class RoomServiceImpl implements RoomService {

	final HotelRepository hotelRepo;

	final RoomRepository repo;

	RoomServiceImpl(RoomRepository repo, HotelRepository hotelRepo) {
		this.repo = repo;
		this.hotelRepo = hotelRepo;
	}

	@Override
	public Room addRoom(int hotelId, RoomDTO roomDTO) {

		Hotel hotel = hotelRepo.findById(hotelId)
				.orElseThrow(() -> new HotelException("No Hotel found with id: " + hotelId));

		if (repo.existsByHotelHotelIdAndRoomNumber(hotelId, roomDTO.getRoomNumber())) {
			log.error("Room Number: {} already Exists in this Hotel", roomDTO.getRoomNumber());
			throw new RoomException("Room number " + roomDTO.getRoomNumber() + " already exists in this hotel");
		}

		Room room = new Room();

		room.setHotel(hotel);
		room.setBedSize(roomDTO.getBedSize());
		room.setCapacity(roomDTO.getCapacity());
		room.setFare(roomDTO.getFare());
		room.setRoomNumber(roomDTO.getRoomNumber());
		room.setRoomType(roomDTO.getRoomType());
		room.setSizeSQM(roomDTO.getSizeSQM());
		room.setAc(roomDTO.isAc());

		Room savedRoom = repo.save(room);

		log.info("Room Created Successfully: {} and Added to Hotel: {}", savedRoom.getRoomId(), hotelId);

		return savedRoom;
	}

	@Override
	public Room updateRoom(int roomId, RoomDTO roomDTO) {
		Room room = getRoomById(roomId);

		room.setBedSize(roomDTO.getBedSize());
		room.setCapacity(roomDTO.getCapacity());
		room.setFare(roomDTO.getFare());
		room.setRoomNumber(roomDTO.getRoomNumber());
		room.setRoomType(roomDTO.getRoomType());
		room.setSizeSQM(roomDTO.getSizeSQM());
		room.setAc(roomDTO.isAc());

		Room savedRoom = repo.save(room);

		log.info("Room Updated Successfully: {}", roomId);

		return savedRoom;
	}

	@Override
	public Room getRoomById(int roomID) {
		Room room = repo.findById(roomID).orElseThrow(() -> {
			log.error("No room Found on the given id: {}", roomID);
			return new RoomException("No room Found on the given id:" + roomID);
		});

		log.info("Getting Room For RoomID : {}", roomID);
		return room;
	}

	@Override
	public List<Room> getAllRooms() {
		List<Room> rooms = repo.findAll();
		log.info("Getting All Rooms");
		return rooms;
	}

	@Override
	public boolean deleteRoom(int roomId) {
		Room room = getRoomById(roomId);
		repo.delete(room);
		log.info("Room Deleted Successfully");
		return true;
	}

	@Override
	public List<Room> getRoomsByHotel(int hotelId) {
		List<Room> rooms = repo.findByHotelHotelId(hotelId);
		log.info("Finding All Rooms For Hotel :{}", hotelId);
		return rooms;
	}

	@Override
	public List<Room> getAvailableRooms(int hotelId, LocalDate checkIn, LocalDate checkOut) {
		List<Room> rooms = repo.findAvailableRooms(hotelId, checkIn, checkOut);
		
		log.info(rooms.isEmpty() ? "No Available Rooms." : "Getting All Available Rooms.");
		
		return rooms;
	}

	public void blockRoom(int roomId) {
		Room room = getRoomById(roomId);

		room.setAvailable(false);

		repo.save(room);
		
		log.info("Room Blocked");
	}

	public void unblockRoom(int roomId) {
		Room room = getRoomById(roomId);

		room.setAvailable(true);

		repo.save(room);
		
		log.info("Room Unblocked");
	}
}
