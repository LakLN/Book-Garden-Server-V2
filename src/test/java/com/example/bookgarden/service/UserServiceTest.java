package com.example.bookgarden.service;

import com.example.bookgarden.dto.*;
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

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    @Test
    void testUpdateAddresses_Success() {
        User user = new User();
        user.setId("userId1");
        user.setAddresses(new ArrayList<>());

        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        AddressesRequestDTO addressesRequestDTO = new AddressesRequestDTO();
        addressesRequestDTO.setAddresses(List.of("123 Street", "456 Avenue"));

        when(addressRepository.findByAddress(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        Address address1 = new Address("1", "123 Street");
        Address address2 = new Address("2", "456 Avenue");

        when(addressRepository.save(any(Address.class))).thenReturn(address1).thenReturn(address2);
        when(addressRepository.findById("1")).thenReturn(Optional.of(address1));
        when(addressRepository.findById("2")).thenReturn(Optional.of(address2));

        ResponseEntity<GenericResponse> response = userService.updateAddresses("userId1", addressesRequestDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cập nhật địa chỉ người dùng thành công", response.getBody().getMessage());
    }
    @Test
    void testChangePassword_Success() {
        User user = new User();
        user.setId("userId1");
        user.setPassWord("oldPassword");

        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ChangePasswordRequestDTO requestDTO = new ChangePasswordRequestDTO();
        requestDTO.setPassWord("newPassword");
        requestDTO.setConfirmPassWord("newPassword");

        ResponseEntity<GenericResponse> response = userService.changePassword("userId1", requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Đổi mật khẩu thành công", response.getBody().getMessage());
    }

    @Test
    void testChangePassword_UserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        ChangePasswordRequestDTO requestDTO = new ChangePasswordRequestDTO();
        requestDTO.setPassWord("newPassword");
        requestDTO.setConfirmPassWord("newPassword");

        ResponseEntity<GenericResponse> response = userService.changePassword("userId1", requestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Không tìm thấy người dùng", response.getBody().getMessage());
    }

    @Test
    void testChangePassword_SaveError() {
        User user = new User();
        user.setId("userId1");
        user.setPassWord("oldPassword");

        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        ChangePasswordRequestDTO requestDTO = new ChangePasswordRequestDTO();
        requestDTO.setPassWord("newPassword");
        requestDTO.setConfirmPassWord("newPassword");

        ResponseEntity<GenericResponse> response = userService.changePassword("userId1", requestDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Lỗi khi đổi mật khẩu", response.getBody().getMessage());
    }
}
