package videobackend.video.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import videobackend.video.dto.DanmakuDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 使用 Redis 存储弹幕数据：
 * key: danmaku:{videoId}
 * type: ZSET
 *   score = time(秒)
 *   member = 弹幕 JSON
 */
@Service
public class DanmakuService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DanmakuService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String videoId) {
        return "danmaku:" + videoId;
    }

    /**
     * 写入一条弹幕
     */
    public void addDanmaku(String videoId, DanmakuDTO dto) throws JsonProcessingException {
        if (videoId == null || dto == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return;
        }
        String key = buildKey(videoId);
        if (dto.getTime() == null) {
            dto.setTime(0.0);
        }
        if (dto.getColor() == null || dto.getColor().isEmpty()) {
            dto.setColor("#ffffff");
        }
        if (dto.getMode() == null || dto.getMode().isEmpty()) {
            dto.setMode("scroll");
        }

        String json = objectMapper.writeValueAsString(dto);
        try {
            // score = 视频内时间
            redisTemplate.opsForZSet().add(key, json, dto.getTime());

            // 简单维护一个计数（统计不是核心逻辑，异常时不影响主流程）
            try {
                redisTemplate.opsForHash().increment("danmaku:stat:" + videoId, "count", 1);
            } catch (Exception ignore) {
                // ignore
            }
        } catch (DataAccessException e) {
            // Redis 连接失败 / WRONGTYPE 等都会进来
            throw e;
        }
    }

    /**
     * 按时间范围读取弹幕
     */
    public List<DanmakuDTO> listDanmaku(String videoId, double from, double to) {
        String key = buildKey(videoId);
        Set<String> members = redisTemplate.opsForZSet()
                .rangeByScore(key, from, to);

        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }

        return members.stream().map(json -> {
            try {
                return objectMapper.readValue(json, DanmakuDTO.class);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).sorted(Comparator.comparing(DanmakuDTO::getTime))
                .collect(Collectors.toList());
    }

    /**
     * 获取视频的弹幕总数
     */
    public long getDanmakuCount(String videoId) {
        String key = buildKey(videoId);
        try {
            Long count = redisTemplate.opsForZSet().count(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            return count != null ? count : 0;
        } catch (Exception e) {
            // 如果 ZSet 不存在或出错，尝试从 Hash 统计读取
            try {
                Object countObj = redisTemplate.opsForHash().get("danmaku:stat:" + videoId, "count");
                if (countObj != null) {
                    return Long.parseLong(countObj.toString());
                }
            } catch (Exception ignore) {
                // ignore
            }
            return 0;
        }
    }
}


