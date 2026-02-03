package videobackend.video.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import videobackend.video.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByAccount(String account) {
        String sql = """
                SELECT id, username, account, password, email, avatar, bio, status, create_time, update_time
                FROM users
                WHERE account = ?
                """;
        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), account);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(Long id) {
        String sql = """
                SELECT id, username, account, password, email, avatar, bio, status, create_time, update_time
                FROM users
                WHERE id = ?
                """;
        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        String sql = "UPDATE users SET avatar = ?, update_time = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, avatarUrl, userId);
    }

    public void updateBio(Long userId, String bio) {
        String sql = "UPDATE users SET bio = ?, update_time = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, bio, userId);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setAccount(rs.getString("account"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            user.setAvatar(rs.getString("avatar"));
            user.setBio(rs.getString("bio"));
            user.setStatus(rs.getObject("status") != null ? rs.getInt("status") : null);
            
            // 处理时间字段
            java.sql.Timestamp createTime = rs.getTimestamp("create_time");
            if (createTime != null) {
                user.setCreateTime(createTime.toLocalDateTime());
            }
            java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
            if (updateTime != null) {
                user.setUpdateTime(updateTime.toLocalDateTime());
            }
            
            return user;
        }
    }
}

