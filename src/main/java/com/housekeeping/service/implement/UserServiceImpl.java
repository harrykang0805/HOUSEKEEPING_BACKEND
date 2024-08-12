package com.housekeeping.service.implement;

import com.housekeeping.DTO.UserDTO;
import com.housekeeping.entity.User;
import com.housekeeping.entity.enums.UserPlatform;
import com.housekeeping.repository.UserRepository;
import com.housekeeping.repository.RefreshRepository;
import com.housekeeping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getUserDTOById(Long id) {
        User user = getUserById(id);
        return convertToDTO(user);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO completeRegistration(UserDTO userDTO) {
        // 구현 로직
        return userDTO;
    }

    @Override
    public boolean isNewUser(String email, UserPlatform platform) {
        // 구현 로직
        return false;
    }

    @Override
    public UserDTO updateUserInfo(UserDTO userDTO) {
        User user = getUserById(userDTO.getUserId());
        user.setName(userDTO.getName());
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        User updatedUser = saveUser(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        refreshRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public void updateUserStatus(Long userId, boolean isOnline) {
        User user = getUserById(userId);
        user.setUserIsOnline(isOnline);
        saveUser(user);
    }

    @Override
    public void updateUserStatusByNickname(String nickname, boolean isOnline) {
        User user = getUserByNickname(nickname);
        user.setUserIsOnline(isOnline);
        saveUser(user);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .userPlatform(user.getUserPlatform())
                .build();
    }
}