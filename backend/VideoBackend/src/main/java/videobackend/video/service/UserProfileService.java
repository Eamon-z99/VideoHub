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
    private final AvatarSubmissionService avatarSubmissionService;

    public UserProfileService(UserRepository userRepository,
                              AvatarSubmissionService avatarSubmissionService) {
        this.userRepository = userRepository;
        this.avatarSubmissionService = avatarSubmissionService;
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public User updateBio(Long userId, String bio) {
        userRepository.updateBio(userId, bio);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    /**
     * 头像修改改为“提交审核”：写入 avatar_submissions，审核通过后再回写 users.avatar。
     * 返回用户实体用于兼容调用方（此处不会立刻变更 avatar 字段）。
     */
    public User updateAvatar(Long userId, MultipartFile file) throws IOException {
        avatarSubmissionService.submitAvatar(userId, file);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}


