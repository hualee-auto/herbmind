#!/usr/bin/env python3
"""
HerbMind 药材数据批量修复
分批处理，保存进度
"""

import json
import time
from pathlib import Path
from datetime import datetime

DATA_FILE = "herbs_split.json"
PROGRESS_FILE = "herb_repair_progress.json"
REPAIR_LOG = "herb_repair_log.json"
BATCH_SIZE = 3  # 每批处理3个，方便验证质量

def load_data():
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_data(herbs):
    with open(DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(herbs, f, ensure_ascii=False, indent=2)

def load_progress():
    try:
        with open(PROGRESS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except:
        return {'completed': [], 'current_batch': 0, 'total': 0}

def save_progress(progress):
    with open(PROGRESS_FILE, 'w', encoding='utf-8') as f:
        json.dump(progress, f, ensure_ascii=False, indent=2)

def append_log(entry):
    try:
        with open(REPAIR_LOG, 'r', encoding='utf-8') as f:
            log = json.load(f)
    except:
        log = []
    log.append(entry)
    with open(REPAIR_LOG, 'w', encoding='utf-8') as f:
        json.dump(log, f, ensure_ascii=False, indent=2)

def get_herbs_to_repair(herbs, progress):
    """获取需要修复的药材列表"""
    completed = set(progress.get('completed', []))
    to_repair = []
    
    for herb in herbs:
        if herb['id'] not in completed:
            to_repair.append(herb)
    
    return to_repair

def generate_batch_prompt(herbs_batch):
    """为一批药材生成修复提示词"""
    prompt = """你是一位资深中医药专家，请为以下中药材修复数据字段。

要求：
1. effects (功效)：简洁列出主要功效，3-5条
2. indications (主治)：描述主治病症，2-3句话
3. usage (用法用量)：简洁说明，如"煎服，3-10g"
4. contraindications (禁忌)：列出禁忌人群
5. keyPoint (考试重点)：一句话核心考点
6. memoryTip (记忆口诀)：朗朗上口的口诀
7. association (趣味联想)：有趣的记忆联想

请按以下JSON数组格式返回，每个药材一个对象：
[
  {
    "id": "药材拼音ID",
    "name": "药材名称",
    "effects": "功效描述",
    "indications": "主治病症",
    "usage": "用法用量",
    "contraindications": "禁忌",
    "keyPoint": "考试重点",
    "memoryTip": "记忆口诀",
    "association": "趣味联想"
  },
  ...
]

注意：只需返回JSON数据，不要其他说明文字。

需要修复的药材：

"""
    
    for i, herb in enumerate(herbs_batch, 1):
        prompt += f"\n【{i}. {herb['name']}】"
        prompt += f"\nID: {herb['id']}"
        prompt += f"\n分类: {herb.get('category', '')} - {herb.get('subCategory', '')}"
        prompt += f"\n性味: {herb.get('nature', '')}"
        prompt += f"\n归经: {', '.join(herb.get('meridians', [])) if isinstance(herb.get('meridians'), list) else herb.get('meridians', '')}"
        
        # 添加现有内容作为参考
        effects = herb.get('effects', '')
        if isinstance(effects, list):
            effects = ' '.join(effects)
        if effects and len(effects) < 300:
            prompt += f"\n现有功效参考: {effects[:200]}"
        
        prompt += "\n"
    
    return prompt

def parse_repair_result(result_text):
    """解析修复结果"""
    try:
        # 尝试提取JSON
        json_match = result_text.strip()
        if json_match.startswith('```json'):
            json_match = json_match[7:]
        if json_match.startswith('```'):
            json_match = json_match[3:]
        if json_match.endswith('```'):
            json_match = json_match[:-3]
        
        repaired_herbs = json.loads(json_match.strip())
        return repaired_herbs
    except Exception as e:
        print(f"  ❌ 解析失败: {e}")
        return None

def repair_batch(herbs, batch_num, total_batches):
    """修复一批药材"""
    print(f"\n{'='*60}")
    print(f"🔄 批次 {batch_num}/{total_batches} ({len(herbs)}个药材)")
    print(f"{'='*60}")
    
    for herb in herbs:
        print(f"  • {herb['name']}")
    
    prompt = generate_batch_prompt(herbs)
    
    # 保存提示词到文件，方便手动调用
    prompt_file = f"repair_batch_{batch_num:03d}_prompt.txt"
    with open(prompt_file, 'w', encoding='utf-8') as f:
        f.write(prompt)
    
    print(f"\n💾 提示词已保存到: {prompt_file}")
    print("\n请将此提示词发送给大模型，获取修复后的JSON数据")
    print("然后将结果保存为: repair_batch_{:03d}_result.txt".format(batch_num))
    
    return prompt_file

def apply_repair_result(herbs, repaired_data):
    """应用修复结果到数据"""
    repaired_count = 0
    
    for repaired in repaired_data:
        herb_id = repaired.get('id')
        
        # 查找对应的药材
        for herb in herbs:
            if herb['id'] == herb_id:
                # 更新字段
                for field in ['effects', 'indications', 'usage', 'contraindications', 
                             'keyPoint', 'memoryTip', 'association']:
                    if field in repaired and repaired[field]:
                        herb[field] = repaired[field]
                
                repaired_count += 1
                break
    
    return repaired_count

def main():
    print("=" * 70)
    print("🌿 HerbMind 药材数据批量修复")
    print("=" * 70)
    
    # 加载数据
    herbs = load_data()
    print(f"\n📚 加载了 {len(herbs)} 味药材")
    
    # 加载进度
    progress = load_progress()
    completed = set(progress.get('completed', []))
    print(f"✅ 已完成: {len(completed)} 味")
    
    # 获取需要修复的药材
    to_repair = get_herbs_to_repair(herbs, progress)
    print(f"🔧 待修复: {len(to_repair)} 味")
    
    if not to_repair:
        print("\n🎉 所有药材已修复完成！")
        return
    
    # 计算批次
    total_batches = (len(to_repair) + BATCH_SIZE - 1) // BATCH_SIZE
    current_batch = progress.get('current_batch', 0)
    
    print(f"\n📦 将分 {total_batches} 批处理，每批 {BATCH_SIZE} 个")
    print(f"⏩ 当前批次: {current_batch + 1}")
    
    # 准备当前批次
    start_idx = current_batch * BATCH_SIZE
    end_idx = min(start_idx + BATCH_SIZE, len(to_repair))
    batch_herbs = to_repair[start_idx:end_idx]
    
    # 生成修复提示词
    prompt_file = repair_batch(batch_herbs, current_batch + 1, total_batches)
    
    print(f"\n{'='*70}")
    print("📋 使用说明:")
    print("1. 复制提示词文件内容发送给大模型（Kimi/Claude/ChatGPT）")
    print("2. 获取返回的JSON结果")
    print("3. 将结果保存到对应的 repair_batch_XXX_result.txt 文件")
    print("4. 运行 apply_repair.py 应用修复结果")
    print("="*70)

if __name__ == "__main__":
    main()
