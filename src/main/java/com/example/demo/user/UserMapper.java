package com.example.demo.user;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

    public UserDto convertToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername()
        );
    }

    public UserShortDto convertToUserShortDto(User user) {
        return new UserShortDto(user.getId());
    }
}
