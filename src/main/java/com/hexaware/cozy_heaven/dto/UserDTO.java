package com.hexaware.cozy_heaven.dto;

import org.hibernate.annotations.NotFound;

import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
	
	@NotBlank(message = "Name Required")
	@Size(min = 2, max = 26)
	private String name;
	
	@NotBlank(message = "Email Required")
	@Email
	private String email;
	
	@NotBlank(message = "Password Required")
	@Size(min = 8)
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).+$", message = "Password must contain at least one uppercase letter, one digit, and one special character")
	private String password;
	
	@NotFound
	private Gender gender;
	
	@NotBlank(message = "Contact Number Required")
	@Size(min = 10, max = 10)
	private String contactNumber;
	
	@NotBlank(message = "Address Required")
	@Size(min = 10 ,max = 255)
	private String address;
	
	@NotNull(message = "Role Required")
	private Role role;

}
