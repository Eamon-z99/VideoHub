"""
推荐系统投毒攻击脚本
模拟不同类型的投毒攻击，修改数据库中的用户行为数据
"""

import pymysql
import random
import time
from datetime import datetime, timedelta
from typing import List, Tuple
import argparse
import json

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'videohub',
    'charset': 'utf8mb4'
}


class PoisonAttack:
    """投毒攻击类"""
    
    def __init__(self, db_config: dict):
        self.conn = pymysql.connect(**db_config)
        self.cursor = self.conn.cursor()
    
    def __del__(self):
        if hasattr(self, 'conn'):
            self.conn.close()
    
    def get_all_video_ids(self) -> List[str]:
        """获取所有视频ID"""
        self.cursor.execute("SELECT video_id FROM videos")
        return [row[0] for row in self.cursor.fetchall()]
    
    def get_target_videos(self, count: int = 5) -> List[str]:
        """选择目标视频（攻击目标）"""
        all_videos = self.get_all_video_ids()
        # 选择播放量较低的视频作为攻击目标（更容易被提升）
        self.cursor.execute("""
            SELECT video_id FROM videos 
            ORDER BY view_count ASC 
            LIMIT %s
        """, (count,))
        return [row[0] for row in self.cursor.fetchall()]
    
    def create_fake_users(self, count: int, prefix: str = "attack_user_") -> List[int]:
        """创建虚假用户（僵尸账户）"""
        user_ids = []
        password_hash = "$2a$10$/UbxnmyC1Gh7X0K.S90DgOZerW8.gUP2jHXCEyMRwu4Qe0dWHvMzO"  # 默认密码hash
        
        for i in range(count):
            username = f"{prefix}{int(time.time())}_{i}"
            account = f"{prefix}{int(time.time())}_{i}"
            email = f"{prefix}{i}@attack.com"
            
            self.cursor.execute("""
                INSERT INTO users (username, account, password, email, status, create_time)
                VALUES (%s, %s, %s, %s, 1, NOW())
            """, (username, account, password_hash, email))
            
            user_id = self.cursor.lastrowid
            user_ids.append(user_id)
        
        self.conn.commit()
        print(f"创建了 {len(user_ids)} 个虚假用户")
        return user_ids
    
    def random_attack(self, user_ids: List[int], target_videos: List[str], 
                     actions_per_user: int = 50):
        """
        随机攻击：僵尸用户随机对目标视频进行点赞和收藏
        """
        print(f"\n开始随机攻击...")
        print(f"用户数: {len(user_ids)}, 目标视频数: {len(target_videos)}")
        
        for user_id in user_ids:
            # 随机选择一些目标视频
            selected_videos = random.sample(target_videos, 
                                           min(len(target_videos), actions_per_user))
            
            for video_id in selected_videos:
                # 随机决定行为类型
                action_type = random.choice(['like', 'favorite', 'both'])
                
                if action_type in ['like', 'both']:
                    try:
                        self.cursor.execute("""
                            INSERT INTO video_likes (user_id, video_id, create_time)
                            VALUES (%s, %s, NOW())
                            ON DUPLICATE KEY UPDATE create_time = NOW()
                        """, (user_id, video_id))
                    except Exception as e:
                        pass  # 忽略重复键错误
                
                if action_type in ['favorite', 'both']:
                    try:
                        self.cursor.execute("""
                            INSERT INTO favorites (user_id, video_id, create_time)
                            VALUES (%s, %s, NOW())
                            ON DUPLICATE KEY UPDATE create_time = NOW()
                        """, (user_id, video_id))
                    except Exception as e:
                        pass
                
                # 更新视频统计
                self.cursor.execute("""
                    UPDATE videos 
                    SET like_count = (SELECT COUNT(*) FROM video_likes WHERE video_id = %s),
                        favorite_count = (SELECT COUNT(*) FROM favorites WHERE video_id = %s)
                    WHERE video_id = %s
                """, (video_id, video_id, video_id))
        
        self.conn.commit()
        print(f"随机攻击完成，共生成 {len(user_ids) * actions_per_user} 个行为")
    
    def average_attack(self, user_ids: List[int], target_videos: List[str],
                      filler_ratio: float = 0.3):
        """
        平均攻击：对目标视频给予高分，对其他视频给予平均分
        """
        print(f"\n开始平均攻击...")
        
        all_videos = self.get_all_video_ids()
        filler_videos = [v for v in all_videos if v not in target_videos]
        
        for user_id in user_ids:
            # 对目标视频进行点赞和收藏
            for video_id in target_videos:
                try:
                    self.cursor.execute("""
                        INSERT INTO video_likes (user_id, video_id, create_time)
                        VALUES (%s, %s, NOW())
                        ON DUPLICATE KEY UPDATE create_time = NOW()
                    """, (user_id, video_id))
                    
                    self.cursor.execute("""
                        INSERT INTO favorites (user_id, video_id, create_time)
                        VALUES (%s, %s, NOW())
                        ON DUPLICATE KEY UPDATE create_time = NOW()
                    """, (user_id, video_id))
                except Exception as e:
                    pass
            
            # 对填充视频进行少量行为（模拟正常用户）
            filler_count = int(len(target_videos) * filler_ratio)
            selected_fillers = random.sample(filler_videos, 
                                            min(len(filler_videos), filler_count))
            
            for video_id in selected_fillers:
                # 只进行点赞，不收藏（降低权重）
                try:
                    self.cursor.execute("""
                        INSERT INTO video_likes (user_id, video_id, create_time)
                        VALUES (%s, %s, NOW())
                        ON DUPLICATE KEY UPDATE create_time = NOW()
                    """, (user_id, video_id))
                except Exception as e:
                    pass
        
        # 更新视频统计
        for video_id in target_videos + selected_fillers:
            self.cursor.execute("""
                UPDATE videos 
                SET like_count = (SELECT COUNT(*) FROM video_likes WHERE video_id = %s),
                    favorite_count = (SELECT COUNT(*) FROM favorites WHERE video_id = %s)
                WHERE video_id = %s
            """, (video_id, video_id, video_id))
        
        self.conn.commit()
        print(f"平均攻击完成")
    
    def group_attack(self, user_ids: List[int], target_videos: List[str],
                    group_size: int = 5):
        """
        组攻击：将用户分成组，每组协同攻击特定视频
        """
        print(f"\n开始组攻击...")
        
        # 将用户分组
        groups = [user_ids[i:i+group_size] 
                 for i in range(0, len(user_ids), group_size)]
        
        # 将目标视频分配给各组
        video_groups = [target_videos[i:i+len(target_videos)//len(groups)+1]
                       for i in range(0, len(target_videos), len(target_videos)//len(groups)+1)]
        
        for group_idx, group_users in enumerate(groups):
            group_videos = video_groups[group_idx % len(video_groups)] if video_groups else target_videos
            
            for user_id in group_users:
                for video_id in group_videos:
                    # 组内用户对相同视频进行相同操作
                    try:
                        self.cursor.execute("""
                            INSERT INTO video_likes (user_id, video_id, create_time)
                            VALUES (%s, %s, NOW())
                            ON DUPLICATE KEY UPDATE create_time = NOW()
                        """, (user_id, video_id))
                        
                        self.cursor.execute("""
                            INSERT INTO favorites (user_id, video_id, create_time)
                            VALUES (%s, %s, NOW())
                            ON DUPLICATE KEY UPDATE create_time = NOW()
                        """, (user_id, video_id))
                    except Exception as e:
                        pass
        
        # 更新视频统计
        for video_id in target_videos:
            self.cursor.execute("""
                UPDATE videos 
                SET like_count = (SELECT COUNT(*) FROM video_likes WHERE video_id = %s),
                    favorite_count = (SELECT COUNT(*) FROM favorites WHERE video_id = %s)
                WHERE video_id = %s
            """, (video_id, video_id, video_id))
        
        self.conn.commit()
        print(f"组攻击完成，共 {len(groups)} 个攻击组")
    
    def time_burst_attack(self, user_ids: List[int], target_videos: List[str],
                         burst_duration_hours: int = 1):
        """
        时间爆发攻击：在短时间内集中进行大量操作
        """
        print(f"\n开始时间爆发攻击...")
        
        # 设置爆发时间窗口
        start_time = datetime.now() - timedelta(hours=burst_duration_hours)
        
        for user_id in user_ids:
            for video_id in target_videos:
                # 在时间窗口内进行操作
                action_time = start_time + timedelta(
                    seconds=random.randint(0, burst_duration_hours * 3600))
                
                try:
                    self.cursor.execute("""
                        INSERT INTO video_likes (user_id, video_id, create_time)
                        VALUES (%s, %s, %s)
                        ON DUPLICATE KEY UPDATE create_time = %s
                    """, (user_id, video_id, action_time, action_time))
                    
                    self.cursor.execute("""
                        INSERT INTO favorites (user_id, video_id, create_time)
                        VALUES (%s, %s, %s)
                        ON DUPLICATE KEY UPDATE create_time = %s
                    """, (user_id, video_id, action_time, action_time))
                except Exception as e:
                    pass
        
        self.conn.commit()
        print(f"时间爆发攻击完成")
    
    def get_attack_statistics(self) -> dict:
        """获取攻击统计信息"""
        stats = {}
        
        # 统计虚假用户数量
        self.cursor.execute("""
            SELECT COUNT(*) FROM users 
            WHERE account LIKE 'attack_user_%'
        """)
        stats['fake_users'] = self.cursor.fetchone()[0]
        
        # 统计攻击行为数量
        self.cursor.execute("""
            SELECT COUNT(*) FROM video_likes vl
            JOIN users u ON vl.user_id = u.id
            WHERE u.account LIKE 'attack_user_%'
        """)
        stats['fake_likes'] = self.cursor.fetchone()[0]
        
        self.cursor.execute("""
            SELECT COUNT(*) FROM favorites f
            JOIN users u ON f.user_id = u.id
            WHERE u.account LIKE 'attack_user_%'
        """)
        stats['fake_favorites'] = self.cursor.fetchone()[0]
        
        return stats


def main():
    parser = argparse.ArgumentParser(description='推荐系统投毒攻击脚本')
    parser.add_argument('--attack-type', type=str, 
                       choices=['random', 'average', 'group', 'time-burst', 'all'],
                       default='all', help='攻击类型')
    parser.add_argument('--user-count', type=int, default=20, 
                       help='虚假用户数量')
    parser.add_argument('--target-count', type=int, default=5, 
                       help='目标视频数量')
    parser.add_argument('--actions-per-user', type=int, default=50,
                       help='每个用户的行为数量（随机攻击）')
    
    args = parser.parse_args()
    
    attack = PoisonAttack(DB_CONFIG)
    
    # 创建虚假用户
    fake_users = attack.create_fake_users(args.user_count)
    
    # 选择目标视频
    target_videos = attack.get_target_videos(args.target_count)
    print(f"\n目标视频: {target_videos}")
    
    # 执行攻击
    if args.attack_type in ['random', 'all']:
        attack.random_attack(fake_users, target_videos, args.actions_per_user)
    
    if args.attack_type in ['average', 'all']:
        attack.average_attack(fake_users, target_videos)
    
    if args.attack_type in ['group', 'all']:
        attack.group_attack(fake_users, target_videos)
    
    if args.attack_type in ['time-burst', 'all']:
        attack.time_burst_attack(fake_users, target_videos)
    
    # 输出统计信息
    stats = attack.get_attack_statistics()
    print("\n" + "="*50)
    print("攻击统计:")
    print(json.dumps(stats, indent=2, ensure_ascii=False))
    print("="*50)


if __name__ == "__main__":
    main()



