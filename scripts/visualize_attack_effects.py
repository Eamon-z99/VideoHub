"""
投毒攻击效果可视化脚本
对比投毒前后的推荐系统效果
"""

import pymysql
import matplotlib.pyplot as plt
import matplotlib
import numpy as np
import pandas as pd
from datetime import datetime
import json
import os

# 设置中文字体
matplotlib.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei']
matplotlib.rcParams['axes.unicode_minus'] = False

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'videohub',
    'charset': 'utf8mb4'
}


class AttackVisualizer:
    """攻击效果可视化类"""
    
    def __init__(self, db_config: dict):
        self.conn = pymysql.connect(**db_config)
        self.cursor = self.conn.cursor()
        self.output_dir = "attack_analysis"
        os.makedirs(self.output_dir, exist_ok=True)
    
    def __del__(self):
        if hasattr(self, 'conn'):
            self.conn.close()
    
    def get_user_statistics(self) -> pd.DataFrame:
        """获取用户行为统计"""
        query = """
            SELECT 
                u.id as user_id,
                u.username,
                u.account,
                COUNT(DISTINCT vl.video_id) as like_count,
                COUNT(DISTINCT f.video_id) as favorite_count,
                COUNT(DISTINCT ph.video_id) as watch_count,
                COUNT(DISTINCT vl.video_id) + COUNT(DISTINCT f.video_id) as total_actions,
                CASE WHEN u.account LIKE 'attack_user_%' THEN 1 ELSE 0 END as is_attack_user
            FROM users u
            LEFT JOIN video_likes vl ON u.id = vl.user_id
            LEFT JOIN favorites f ON u.id = f.user_id
            LEFT JOIN play_history ph ON u.id = ph.user_id AND ph.play_time > 0
            GROUP BY u.id, u.username, u.account
        """
        return pd.read_sql(query, self.conn)
    
    def get_video_statistics(self) -> pd.DataFrame:
        """获取视频统计"""
        query = """
            SELECT 
                v.video_id,
                v.title,
                v.view_count,
                v.like_count,
                v.favorite_count,
                COUNT(DISTINCT vl.user_id) as like_users,
                COUNT(DISTINCT f.user_id) as favorite_users,
                COUNT(DISTINCT CASE WHEN u.account LIKE 'attack_user_%' THEN vl.user_id END) as attack_likes,
                COUNT(DISTINCT CASE WHEN u.account LIKE 'attack_user_%' THEN f.user_id END) as attack_favorites
            FROM videos v
            LEFT JOIN video_likes vl ON v.video_id = vl.video_id
            LEFT JOIN favorites f ON v.video_id = f.video_id
            LEFT JOIN users u ON (vl.user_id = u.id OR f.user_id = u.id)
            GROUP BY v.video_id, v.title, v.view_count, v.like_count, v.favorite_count
        """
        return pd.read_sql(query, self.conn)
    
    def plot_user_behavior_distribution(self):
        """绘制用户行为分布图"""
        df = self.get_user_statistics()
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        
        # 1. 正常用户 vs 攻击用户的行为数量对比
        normal_users = df[df['is_attack_user'] == 0]
        attack_users = df[df['is_attack_user'] == 1]
        
        axes[0, 0].hist([normal_users['total_actions'], attack_users['total_actions']],
                       bins=20, alpha=0.7, label=['正常用户', '攻击用户'], color=['blue', 'red'])
        axes[0, 0].set_xlabel('行为数量')
        axes[0, 0].set_ylabel('用户数')
        axes[0, 0].set_title('用户行为数量分布')
        axes[0, 0].legend()
        axes[0, 0].grid(True, alpha=0.3)
        
        # 2. 点赞比例对比
        normal_like_ratio = normal_users['like_count'] / (normal_users['total_actions'] + 1)
        attack_like_ratio = attack_users['like_count'] / (attack_users['total_actions'] + 1)
        
        axes[0, 1].boxplot([normal_like_ratio.dropna(), attack_like_ratio.dropna()],
                          labels=['正常用户', '攻击用户'])
        axes[0, 1].set_ylabel('点赞比例')
        axes[0, 1].set_title('点赞比例对比')
        axes[0, 1].grid(True, alpha=0.3)
        
        # 3. 行为多样性（熵值）
        def calculate_entropy(row):
            actions = [row['like_count'], row['favorite_count'], row['watch_count']]
            total = sum(actions)
            if total == 0:
                return 0
            probs = [a / total for a in actions if a > 0]
            return -sum(p * np.log2(p) for p in probs)
        
        normal_entropy = normal_users.apply(calculate_entropy, axis=1)
        attack_entropy = attack_users.apply(calculate_entropy, axis=1)
        
        axes[1, 0].hist([normal_entropy.dropna(), attack_entropy.dropna()],
                       bins=20, alpha=0.7, label=['正常用户', '攻击用户'], color=['blue', 'red'])
        axes[1, 0].set_xlabel('行为熵值')
        axes[1, 0].set_ylabel('用户数')
        axes[1, 0].set_title('用户行为多样性（熵值）')
        axes[1, 0].legend()
        axes[1, 0].grid(True, alpha=0.3)
        
        # 4. 用户类型统计
        user_counts = df['is_attack_user'].value_counts()
        axes[1, 1].pie([user_counts.get(0, 0), user_counts.get(1, 0)],
                       labels=['正常用户', '攻击用户'],
                       autopct='%1.1f%%',
                       colors=['blue', 'red'],
                       startangle=90)
        axes[1, 1].set_title('用户类型分布')
        
        plt.tight_layout()
        plt.savefig(f'{self.output_dir}/user_behavior_distribution.png', dpi=300, bbox_inches='tight')
        print(f"用户行为分布图已保存: {self.output_dir}/user_behavior_distribution.png")
        plt.close()
    
    def plot_video_impact(self):
        """绘制视频受攻击影响图"""
        df = self.get_video_statistics()
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        
        # 1. 视频点赞数 vs 攻击点赞数
        axes[0, 0].scatter(df['like_count'], df['attack_likes'], alpha=0.6)
        axes[0, 0].set_xlabel('总点赞数')
        axes[0, 0].set_ylabel('攻击用户点赞数')
        axes[0, 0].set_title('视频点赞数 vs 攻击点赞数')
        axes[0, 0].grid(True, alpha=0.3)
        
        # 2. 攻击比例最高的视频Top 10
        df['attack_ratio'] = df['attack_likes'] / (df['like_count'] + 1)
        top_attacked = df.nlargest(10, 'attack_ratio')
        
        axes[0, 1].barh(range(len(top_attacked)), top_attacked['attack_ratio'])
        axes[0, 1].set_yticks(range(len(top_attacked)))
        axes[0, 1].set_yticklabels([title[:20] + '...' if len(title) > 20 else title 
                                    for title in top_attacked['title']], fontsize=8)
        axes[0, 1].set_xlabel('攻击比例')
        axes[0, 1].set_title('受攻击最严重的视频 Top 10')
        axes[0, 1].grid(True, alpha=0.3, axis='x')
        
        # 3. 视频点赞数分布（投毒前后对比）
        # 这里简化处理，假设攻击前点赞数 = 当前点赞数 - 攻击点赞数
        df['before_attack_likes'] = df['like_count'] - df['attack_likes']
        
        axes[1, 0].hist([df['before_attack_likes'], df['like_count']],
                       bins=30, alpha=0.7, label=['投毒前', '投毒后'], color=['green', 'orange'])
        axes[1, 0].set_xlabel('点赞数')
        axes[1, 0].set_ylabel('视频数')
        axes[1, 0].set_title('投毒前后视频点赞数分布')
        axes[1, 0].legend()
        axes[1, 0].grid(True, alpha=0.3)
        
        # 4. 攻击影响程度分布
        attack_impact = df[df['attack_likes'] > 0]['attack_ratio']
        axes[1, 1].hist(attack_impact, bins=20, color='red', alpha=0.7)
        axes[1, 1].set_xlabel('攻击比例')
        axes[1, 1].set_ylabel('视频数')
        axes[1, 1].set_title('视频受攻击影响程度分布')
        axes[1, 1].grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(f'{self.output_dir}/video_impact.png', dpi=300, bbox_inches='tight')
        print(f"视频影响图已保存: {self.output_dir}/video_impact.png")
        plt.close()
    
    def plot_time_analysis(self):
        """绘制时间分析图"""
        query = """
            SELECT 
                DATE(create_time) as date,
                COUNT(*) as action_count,
                COUNT(DISTINCT CASE WHEN u.account LIKE 'attack_user_%' THEN vl.user_id END) as attack_users
            FROM video_likes vl
            JOIN users u ON vl.user_id = u.id
            GROUP BY DATE(create_time)
            ORDER BY date
        """
        df = pd.read_sql(query, self.conn)
        df['date'] = pd.to_datetime(df['date'])
        
        fig, axes = plt.subplots(2, 1, figsize=(15, 10))
        
        # 1. 每日行为数量
        axes[0].plot(df['date'], df['action_count'], marker='o', label='总行为数')
        axes[0].set_xlabel('日期')
        axes[0].set_ylabel('行为数量')
        axes[0].set_title('每日用户行为数量趋势')
        axes[0].legend()
        axes[0].grid(True, alpha=0.3)
        axes[0].tick_params(axis='x', rotation=45)
        
        # 2. 攻击用户数量趋势
        axes[1].plot(df['date'], df['attack_users'], marker='s', color='red', label='攻击用户数')
        axes[1].set_xlabel('日期')
        axes[1].set_ylabel('攻击用户数')
        axes[1].set_title('每日攻击用户数量趋势')
        axes[1].legend()
        axes[1].grid(True, alpha=0.3)
        axes[1].tick_params(axis='x', rotation=45)
        
        plt.tight_layout()
        plt.savefig(f'{self.output_dir}/time_analysis.png', dpi=300, bbox_inches='tight')
        print(f"时间分析图已保存: {self.output_dir}/time_analysis.png")
        plt.close()
    
    def generate_report(self):
        """生成分析报告"""
        user_df = self.get_user_statistics()
        video_df = self.get_video_statistics()
        
        report = {
            'timestamp': datetime.now().isoformat(),
            'user_statistics': {
                'total_users': len(user_df),
                'normal_users': len(user_df[user_df['is_attack_user'] == 0]),
                'attack_users': len(user_df[user_df['is_attack_user'] == 1]),
                'avg_actions_normal': user_df[user_df['is_attack_user'] == 0]['total_actions'].mean(),
                'avg_actions_attack': user_df[user_df['is_attack_user'] == 1]['total_actions'].mean(),
            },
            'video_statistics': {
                'total_videos': len(video_df),
                'videos_attacked': len(video_df[video_df['attack_likes'] > 0]),
                'max_attack_ratio': video_df['attack_ratio'].max(),
                'avg_attack_ratio': video_df[video_df['attack_ratio'] > 0]['attack_ratio'].mean(),
            }
        }
        
        with open(f'{self.output_dir}/report.json', 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        print("\n" + "="*50)
        print("分析报告:")
        print(json.dumps(report, indent=2, ensure_ascii=False))
        print("="*50)
        print(f"\n报告已保存: {self.output_dir}/report.json")
    
    def run_all_analysis(self):
        """运行所有分析"""
        print("开始生成可视化图表...")
        self.plot_user_behavior_distribution()
        self.plot_video_impact()
        self.plot_time_analysis()
        self.generate_report()
        print("\n所有分析完成！")


def main():
    visualizer = AttackVisualizer(DB_CONFIG)
    visualizer.run_all_analysis()


if __name__ == "__main__":
    main()



