package com.hexaware.cozy_heaven.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.UserException;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.Role;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceImplTest {

	@Autowired
	UserService userService;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	UserDTO buildUserDTO(String email, Role role) {
		UserDTO dto = new UserDTO();

		dto.setName("Hari");
		dto.setEmail(email);
		dto.setContactNumber("9080501111");
		dto.setAddress("1/2 Velapadi, Vellore");
		dto.setGender(Gender.MALE);
		dto.setPassword("@Hari357");
		dto.setRole(role);

		return dto;
	}

	@Test
	@Order(1)
	void testAddUser() {

		UserDTO user = buildUserDTO("lee@gmail.com", Role.GUEST);

		User savedUser = userService.addUser(user);

		assertNotNull(savedUser);
		assertNotNull(savedUser.getUserId());
		assertEquals("lee@gmail.com", savedUser.getEmail());
		assertEquals(Role.GUEST, savedUser.getRole());

	}

	@Test
	@Order(2)
	void testAddUser_DuplicateEmail() {

		UserDTO user1 = buildUserDTO("hariharan@gmail.com", Role.GUEST);

		userService.addUser(user1);

		UserDTO user2 = buildUserDTO("hariharan@gmail.com", Role.GUEST);

		assertThrows(UserException.class, () -> userService.addUser(user2));

	}

	@Test
	@Order(3)
	void testAddUser_HotelOwnerRole() {
		UserDTO dto = buildUserDTO("owner_test@cozy.com", Role.HOTEL_OWNER);
		User saved = userService.addUser(dto);

		assertNotNull(saved);
		assertEquals(Role.HOTEL_OWNER, saved.getRole());
	}

	@Test
	@Order(4)
	void testGetUserById_Success() {

		UserDTO dto = buildUserDTO("getbyid@cozy.com", Role.GUEST);
		User created = userService.addUser(dto);

		User fetched = userService.getUserById(created.getUserId());

		assertNotNull(fetched);
		assertEquals(created.getUserId(), fetched.getUserId());
		assertEquals("getbyid@cozy.com", fetched.getEmail());
	}

	@Test
	@Order(5)
	void testGetUserById_NotFound_ThrowsUserException() {
		assertThrows(UserException.class, () -> userService.getUserById(999999));
	}

	@Test
	@Order(6)
	void testGetUserByEmail_Success() {
		UserDTO dto = buildUserDTO("byemail@cozy.com", Role.GUEST);
		userService.addUser(dto);

		User fetched = userService.getUserByEmail("byemail@cozy.com");

		assertNotNull(fetched);
		assertEquals("byemail@cozy.com", fetched.getEmail());
	}

	@Test
	@Order(7)
	void testGetUserByEmail_NotFound_ThrowsUserException() {
		assertThrows(UserException.class, () -> userService.getUserByEmail("nonexistent@nowhere.com"));
	}

	@Test
	@Order(8)
	void testUpdateUser_Success() {
		UserDTO dto = buildUserDTO("update_me@cozy.com", Role.GUEST);
		User created = userService.addUser(dto);

		UserDTO updateDTO = buildUserDTO("update_me@cozy.com", Role.GUEST);
		updateDTO.setName("Updated Name");
		updateDTO.setAddress("456 Updated Lane, Mumbai");

		User updated = userService.updateUser(created.getUserId(), updateDTO);

		assertNotNull(updated);
		assertEquals("Updated Name", updated.getName());
	}

	@Test
	@Order(9)
	void testUpdateUser_EmailConflict_ThrowsUserException() {
		// Create two users
		UserDTO dto1 = buildUserDTO("user_a@cozy.com", Role.GUEST);
		userService.addUser(dto1);

		UserDTO dto2 = buildUserDTO("user_b@cozy.com", Role.GUEST);
		User userB = userService.addUser(dto2);

		// Try to change userB's email to userA's email
		UserDTO conflictDTO = buildUserDTO("user_a@cozy.com", Role.GUEST);
		assertThrows(UserException.class, () -> userService.updateUser(userB.getUserId(), conflictDTO));
	}

	@Test
	@Order(10)
	void testGetAllUsers_ReturnsList() {
		List<User> users = userService.getAllUsers();
		assertNotNull(users);
		assertTrue(users.size() > 0);
	}

	@Test
	@Order(11)
	void testDeleteUser_Success() {
		UserDTO dto = buildUserDTO("delete_me@cozy.com", Role.GUEST);
		User created = userService.addUser(dto);

		boolean result = userService.deleteUser(created.getUserId());

		assertTrue(result);
		assertThrows(UserException.class, () -> userService.getUserById(created.getUserId()));
	}

	@Test
	@Order(12)
	void testDeleteUser_NotFound_ThrowsUserException() {
		assertThrows(UserException.class, () -> userService.deleteUser(999998));
	}

}
