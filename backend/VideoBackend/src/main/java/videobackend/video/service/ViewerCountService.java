package videobackend.video.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轻量级“在看人数”统计（无 WebSocket 版本）：
 * - 前端周期性 heartbeat(videoId, clientId)
 * - 服务端以 TTL 方式统计同一 videoId 下活跃 client 数量
 *
 * 说明：
 * - clientId 建议由前端生成并持久化（localStorage），以区分不同浏览器实例/标签页
 * - 这里只做内存计数：单机有效；多实例部署需改用 Redis 等共享存储
 */
@Service
public class ViewerCountService {

    // videoId -> (clientId -> lastSeenMillis)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> store = new ConcurrentHashMap<>();

    // 30 秒不心跳视为离开
    private static final long TTL_MILLIS = 30_000L;

    /**
     * 记录一次心跳并返回当前 videoId 的活跃人数
     */
    public int heartbeat(String videoId, String clientId) {
        long now = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> clients = store.computeIfAbsent(videoId, k -> new ConcurrentHashMap<>());
        clients.put(clientId, now);
        cleanup(videoId, clients, now);
        return clients.size();
    }

    /**
     * 仅查询当前 videoId 的活跃人数（也会触发一次清理）
     */
    public int count(String videoId) {
        long now = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> clients = store.get(videoId);
        if (clients == null) return 0;
        cleanup(videoId, clients, now);
        return clients.size();
    }

    private void cleanup(String videoId, ConcurrentHashMap<String, Long> clients, long now) {
        long threshold = now - TTL_MILLIS;
        for (Map.Entry<String, Long> e : clients.entrySet()) {
            Long last = e.getValue();
            if (last == null || last < threshold) {
                clients.remove(e.getKey(), last);
            }
        }
        if (clients.isEmpty()) {
            store.remove(videoId, clients);
        }
    }
}


