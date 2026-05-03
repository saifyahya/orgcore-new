package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.category.CategoryDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.response.ResponseDto;
import com.engineering.orgcore.dto.user.AddUserRequest;
import com.engineering.orgcore.dto.user.UserDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;
    private final Utils utils;

    @PostMapping("/addUser")
    private ResponseEntity<ResponseDto> addUser(@RequestBody AddUserRequest newUser) throws Exception {
        usersService.addUser(utils.getCurrentTenant(), newUser);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.CREATED.toString(), "User added successfully"));
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<Page<UserDto>> getAllUsers(@ModelAttribute PageFilter pageable) throws Exception {
        Long tenantId = utils.getCurrentTenant();
        Page<UserDto> page = usersService.getAllUsers(tenantId, pageable);
        return ResponseEntity.ok(page);
    }

    // Update
    @PutMapping
    public UserDto update(@Valid @RequestBody UserDto request
    ) throws NotFoundException {
        return usersService.update(request);
    }
}
