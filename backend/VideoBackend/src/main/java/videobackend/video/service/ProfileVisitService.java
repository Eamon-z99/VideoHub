package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProfileVisitService {

    private final JdbcTemplate jdbcTemplate;

    public ProfileVisitService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordVisit(Long visitorId, Long profileUserId) {
        if (visitorId == null || profileUserId == null) return;
        if (visitorId.equals(profileUserId)) return;

        jdbcTemplate.update(
                """
                INSERT INTO profile_visits (visitor_id, profile_user_id, visit_date, create_time)
                VALUES (?, ?, CURDATE(), NOW())
                ON DUPLICATE KEY UPDATE create_time = create_time
                """,
                visitorId, profileUserId
        );
    }
}
