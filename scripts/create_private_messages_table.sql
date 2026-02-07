USE videohub;

CREATE TABLE IF NOT EXISTS `private_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '私信ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送方用户ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收方用户ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver_time` (`sender_id`, `receiver_id`, `create_time`),
  INDEX `idx_receiver_read_time` (`receiver_id`, `is_read`, `create_time`),
  CONSTRAINT `private_messages_ibfk_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `private_messages_ibfk_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户私信表'
  ROW_FORMAT = Dynamic;


