package com.example.bookgarden.service;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UpdateProfileRequestDTO;
import com.example.bookgarden.dto.UserDTO;
import com.example.bookgarden.entity.Address;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.AddressRepository;
import com.example.bookgarden.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AddressRepository addressRepository;

    @MockBean
    private Cloudinary cloudinary;

    @InjectMocks
    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testGetProfile_UserFound() {
        // Create mock User entity
        User user = new User();
        user.setId("userId1");
        user.setFullName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setAddresses(Arrays.asList("addressId1"));

        // Create mock Address entity
        Address address = new Address();
        address.setId("addressId1");
        address.setAddress("123 Main St");

        // Define mock behavior
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(address));

        // Call the method to test
        ResponseEntity<GenericResponse> response = userService.getProfile("userId1");

        // Validate the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Successfully retrieved user profile", response.getBody().getMessage());

        UserDTO userDTO = (UserDTO) response.getBody().getData();
        assertEquals("John Doe", userDTO.getFullName());
        assertEquals("john.doe@example.com", userDTO.getEmail());
        assertEquals(1, userDTO.getAddresses().size());
        assertEquals("123 Main St", userDTO.getAddresses().get(0).getAddress());
    }
}
