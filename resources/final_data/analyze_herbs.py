#!/usr/bin/env python3
"""
HerbMind 药材数据修复脚本
使用大模型修复缺失/截断的药材字段
"""

import json
import re
from pathlib import Path

# 配置
DATA_FILE = "herbs_split.json"
BACKUP_FILE = "herbs_split_backup.json"
PROGRESS_FILE = "herb_repair_progress.json"
BATCH_SIZE = 5  # 每批处理5个药材

def load_data():
    """加载数据"""
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_data(herbs):
    """保存数据"""
    with open(DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(herbs, f, ensure_ascii=False, indent=2)

def load_progress():
    """加载进度"""
    try:
        with open(PROGRESS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except:
        return {'completed': [], 'failed': []}

def save_progress(progress):
    """保存进度"""
    with open(PROGRESS_FILE, 'w', encoding='utf-8') as f:
        json.dump(progress, f, ensure_ascii=False, indent=2)

def needs_repair(herb):
    """检查药材是否需要修复"""
    issues = []
    
    # effects - 检查是否截断
    effects = herb.get('effects', '')
    if isinstance(effects, list):
        effects = ' '.join(effects)
    if '……' in effects or '...' in effects or len(effects) < 50:
        issues.append('effects')
    
    # indications - 检查是否为空或过短
    indications = herb.get('indications', '')
    if isinstance(indications, list):
        indications = ' '.join(indications)
    if not indications or len(indications) < 30:
        issues.append('indications')
    
    # usage - 检查是否过短
    usage = herb.get('usage', '')
    if isinstance(usage, list):
        usage = ' '.join(usage)
    if not usage or len(usage) < 15:
        issues.append('usage')
    
    # contraindications - 检查是否过短
    contraindications = herb.get('contraindications', '')
    if isinstance(contraindications, list):
        contraindications = ' '.join(contraindications)
    if not contraindications or len(contraindications) < 10:
        issues.append('contraindications')
    
    # keyPoint - 检查是否过短
    keypoint = herb.get('keyPoint', '')
    if isinstance(keypoint, list):
        keypoint = ' '.join(keypoint)
    if not keypoint or len(keypoint) < 8:
        issues.append('keyPoint')
    
    # memoryTip - 检查是否需要优化
    memorytip = herb.get('memoryTip', '')
    if isinstance(memorytip, list):
        memorytip = ' '.join(memorytip)
    if not memorytip or '临床应用' in memorytip or len(memorytip) > 200:
        issues.append('memoryTip')
    
    # association - 检查是否需要优化
    association = herb.get('association', '')
    if isinstance(association, list):
        association = ' '.join(association)
    if not association or '具有' in association and len(association) < 20:
        issues.append('association')
    
    return issues

def generate_repair_prompt(herb, fields_to_repair):
    """生成修复提示词"""
    name = herb['name']
    category = herb.get('category', '')
    subcategory = herb.get('subCategory', '')
    nature = herb.get('nature', '')
    flavor = herb.get('flavor', [])
    meridians = herb.get('meridians', [])
    
    # 获取现有内容作为参考
    existing = {}
    for field in fields_to_repair:
        value = herb.get(field, '')
        if isinstance(value, list):
            value = ' '.join(value)
        existing[field] = value[:200] if value else ''  # 限制长度
    
    prompt = f"""请为中药材【{name}】生成以下字段内容：

药材信息：
- 名称：{name}
- 分类：{category} - {subcategory}
- 性味：{nature}
- 味道：{', '.join(flavor) if isinstance(flavor, list) else flavor}
- 归经：{', '.join(meridians) if isinstance(meridians, list) else meridians}

需要修复的字段：
"""
    
    for field in fields_to_repair:
        if field == 'effects':
            prompt += f"\n1. effects (功效)：\n"
            prompt += f"   现有内容（参考）：{existing.get('effects', '无')}\n"
            prompt += f"   要求：列出主要功效，3-5条，简洁明了\n"
        elif field == 'indications':
            prompt += f"\n2. indications (主治)：\n"
            prompt += f"   现有内容（参考）：{existing.get('indications', '无')}\n"
            prompt += f"   要求：描述主治病症，2-3句话\n"
        elif field == 'usage':
            prompt += f"\n3. usage (用法用量)：\n"
            prompt += f"   现有内容（参考）：{existing.get('usage', '无')}\n"
            prompt += f"   要求：简洁的用法用量说明\n"
        elif field == 'contraindications':
            prompt += f"\n4. contraindications (禁忌)：\n"
            prompt += f"   现有内容（参考）：{existing.get('contraindications', '无')}\n"
            prompt += f"   要求：列出禁忌人群或情况\n"
        elif field == 'keyPoint':
            prompt += f"\n5. keyPoint (考试重点)：\n"
            prompt += f"   现有内容（参考）：{existing.get('keyPoint', '无')}\n"
            prompt += f"   要求：一句话概括核心考点\n"
        elif field == 'memoryTip':
            prompt += f"\n6. memoryTip (记忆口诀)：\n"
            prompt += f"   现有内容（参考）：{existing.get('memoryTip', '无')}\n"
            prompt += f"   要求：朗朗上口的记忆口诀，帮助记忆功效和特点\n"
        elif field == 'association':
            prompt += f"\n7. association (趣味联想)：\n"
            prompt += f"   现有内容（参考）：{existing.get('association', '无')}\n"
            prompt += f"   要求：有趣的联想或故事，帮助记忆\n"
    
    prompt += """\n请按以下JSON格式返回：
{
"fields": {
"effects": "...",
"indications": "...",
...
}
}
"""
    return prompt

def repair_herb(herb, fields_to_repair):
    """修复单个药材（这里需要调用大模型，暂时返回空）"""
    # 实际实现时需要调用大模型API
    # 这里返回需要修复的信息
    return {
        'name': herb['name'],
        'id': herb['id'],
        'fields': fields_to_repair,
        'prompt': generate_repair_prompt(herb, fields_to_repair)
    }

def main():
    print("=" * 60)
    print("🌿 HerbMind 药材数据修复工具")
    print("=" * 60)
    
    # 加载数据
    herbs = load_data()
    print(f"\n📚 加载了 {len(herbs)} 味药材数据")
    
    # 备份数据
    save_data(herbs)
    print(f"💾 已备份到 {BACKUP_FILE}")
    
    # 加载进度
    progress = load_progress()
    completed = set(progress.get('completed', []))
    print(f"⏭️  已修复: {len(completed)} 味")
    
    # 统计需要修复的药材
    need_repair = []
    for herb in herbs:
        if herb['id'] in completed:
            continue
        fields = needs_repair(herb)
        if fields:
            need_repair.append({
                'herb': herb,
                'fields': fields
            })
    
    print(f"🔧 需要修复: {len(need_repair)} 味")
    
    if not need_repair:
        print("\n✅ 所有药材数据都已修复！")
        return
    
    # 显示前10个需要修复的药材
    print("\n前10个需要修复的药材:")
    for i, item in enumerate(need_repair[:10], 1):
        print(f"  {i}. {item['herb']['name']} ({item['herb']['id']}) - 字段: {', '.join(item['fields'])}")
    
    print(f"\n请使用 repair_herbs_batch.py 进行批量修复")
    print("=" * 60)

if __name__ == "__main__":
    main()
