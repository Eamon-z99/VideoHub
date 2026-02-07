package videobackend.video.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import videobackend.video.model.ChatMessage;
import videobackend.video.model.MessageContact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public MessageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取当前用户关注的好友列表，并附带最后一条消息和未读数
     */
    public List<MessageContact> listContacts(Long userId) {
        String sql = """
                SELECT u.id       AS user_id,
                       u.username AS username,
                       u.avatar   AS avatar
                FROM fans f
                         JOIN users u ON f.following_id = u.id
                WHERE f.follower_id = ?
                ORDER BY u.username ASC
                """;
        List<MessageContact> contacts = new ArrayList<>();
        List<MessageContact> baseList = jdbcTemplate.query(sql, (rs, i) -> new MessageContact(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("avatar"),
                null,
                null,
                0L
        ), userId);

        for (MessageContact base : baseList) {
            Long peerId = base.userId();

            // 查询最后一条消息（需要完整字段，方便统一映射为 ChatMessage）
            String lastSql = """
                    SELECT id, sender_id, receiver_id, content, create_time
                    FROM private_messages
                    WHERE (sender_id = ? AND receiver_id = ?)
                       OR (sender_id = ? AND receiver_id = ?)
                    ORDER BY create_time DESC
                    LIMIT 1
                    """;
            List<ChatMessage> lastList = jdbcTemplate.query(lastSql, (rs, i) -> mapToChatMessage(rs),
                    userId, peerId, peerId, userId);

            String lastContent = null;
            String lastTime = null;
            if (!lastList.isEmpty()) {
                ChatMessage last = lastList.get(0);
                lastContent = last.content();
                lastTime = last.createTime();
            }

            // 未读数量：对方发给我且未读
            String unreadSql = """
                    SELECT COUNT(*)
                    FROM private_messages
                    WHERE sender_id = ?
                      AND receiver_id = ?
                      AND is_read = 0
                    """;
            Long unread = jdbcTemplate.queryForObject(unreadSql, Long.class, peerId, userId);
            if (unread == null) {
                unread = 0L;
            }

            contacts.add(new MessageContact(
                    base.userId(),
                    base.username(),
                    base.avatar(),
                    lastContent,
                    lastTime,
                    unread
            ));
        }

        return contacts;
    }

    /**
     * 分页获取与某个好友的聊天记录（按时间升序）
     */
    public List<ChatMessage> listMessages(Long userId, Long peerId, int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String sql = """
                SELECT id, sender_id, receiver_id, content, create_time
                FROM private_messages
                WHERE (sender_id = ? AND receiver_id = ?)
                   OR (sender_id = ? AND receiver_id = ?)
                ORDER BY create_time ASC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToChatMessage(rs),
                userId, peerId, peerId, userId, safeSize, offset);
    }

    /**
     * 发送一条私信
     */
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        String sql = """
                INSERT INTO private_messages (sender_id, receiver_id, content, is_read)
                VALUES (?, ?, ?, 0)
                """;
        jdbcTemplate.update(sql, senderId, receiverId, content);

        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (id == null) {
            log.warn("发送私信后获取 LAST_INSERT_ID 失败");
            return null;
        }

        String querySql = """
                SELECT id, sender_id, receiver_id, content, create_time
                FROM private_messages
                WHERE id = ?
                """;
        List<ChatMessage> list = jdbcTemplate.query(querySql, (rs, i) -> mapToChatMessage(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 将和某个好友的未读消息标记为已读
     */
    public void markAsRead(Long userId, Long peerId) {
        String sql = """
                UPDATE private_messages
                SET is_read = 1
                WHERE sender_id = ?
                  AND receiver_id = ?
                  AND is_read = 0
                """;
        jdbcTemplate.update(sql, peerId, userId);
    }

    private ChatMessage mapToChatMessage(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long senderId = rs.getLong("sender_id");
        Long receiverId = rs.getLong("receiver_id");
        String content = rs.getString("content");
        Timestamp ts = rs.getTimestamp("create_time");
        String timeStr = ts != null ? DATE_FORMATTER.format(ts.toLocalDateTime()) : null;
        return new ChatMessage(id, senderId, receiverId, content, timeStr);
    }
}


