package videobackend.video.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import videobackend.video.model.User;
import videobackend.video.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final AvatarStorageService avatarStorageService;

    public UserProfileService(UserRepository userRepository,
                              AvatarStorageService avatarStorageService) {
        this.userRepository = userRepository;
        this.avatarStorageService = avatarStorageService;
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public User updateBio(Long userId, String bio) {
        userRepository.updateBio(userId, bio);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    public User updateAvatar(Long userId, MultipartFile file) throws IOException {
        String avatarUrl = avatarStorageService.storeAvatar(userId, file);
        userRepository.updateAvatar(userId, avatarUrl);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}


