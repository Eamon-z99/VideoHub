package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.ChatMessage;
import videobackend.video.model.MessageContact;
import videobackend.video.service.MessageService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取关注好友会话列表
     */
    @GetMapping("/contacts")
    public ResponseEntity<?> listContacts(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        List<MessageContact> contacts = messageService.listContacts(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", contacts));
    }

    /**
     * 获取与指定好友的聊天记录
     */
    @GetMapping("/history")
    public ResponseEntity<?> history(HttpServletRequest request,
                                     @RequestParam Long peerId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "50") int pageSize) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        List<ChatMessage> list = messageService.listMessages(userId, peerId, page, pageSize);
        // 将对方发给我的消息标记为已读
        messageService.markAsRead(userId, peerId);

        return ResponseEntity.ok(Map.of("success", true, "data", list));
    }

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        Object peerIdObj = body.get("peerId");
        Object contentObj = body.get("content");
        if (peerIdObj == null || contentObj == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "参数不完整"));
        }

        Long peerId;
        try {
            if (peerIdObj instanceof Number number) {
                peerId = number.longValue();
            } else {
                peerId = Long.parseLong(peerIdObj.toString());
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "peerId 参数不合法"));
        }

        String content = contentObj.toString().trim();
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "消息内容不能为空"));
        }

        ChatMessage message = messageService.sendMessage(userId, peerId, content);
        if (message == null) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "发送失败，请稍后重试"));
        }

        return ResponseEntity.ok(Map.of("success", true, "data", message));
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number number) {
                    return number.longValue();
                }
                if (userIdObj instanceof String str) {
                    try {
                        return Long.parseLong(str);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return null;
    }
}


