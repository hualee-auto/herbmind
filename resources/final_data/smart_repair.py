#!/usr/bin/env python3
"""
HerbMind 药材数据智能修复
使用规则 + 模板方式快速修复常见字段
"""

import json
import re
from pathlib import Path

DATA_FILE = "herbs_split.json"

# 核心68味药材列表
CORE_HERBS = [
    "mahuang", "guizhi", "zisu", "jingjie", "fangfeng", "qianghuo", "xixin", "baizhi",
    "bohe", "niubangzi", "sangye", "juhua", "gegen", "chaihu",
    "shigao", "zhimu", "zhizi", "huanglian", "huangqin", "huangbai",
    "jinyinhua", "lianqiao", "banlangen", "pugongying", "mudanpi",
    "dahuang", "huomaren",
    "fuling", "zexie", "yiyiren", "cheqianzi",
    "huoxiang", "cangzhu", "houpu",
    "duhuo", "weilingxian", "mugua", "sangjisheng",
    "chenpi", "zhishi", "muxiang", "xiangfu",
    "chuanxiong", "danshen", "honghua", "taoren", "yimucao", "niuxi",
    "sanqi", "baiji", "aiye", "huaihua",
    "banxia", "jiegeng", "chuanbeimu", "xingren", "suzi",
    "suanzaoren",
    "tianma", "gouteng",
    "renshen", "huangqi", "baizhu", "gancao",
    "lurong", "duzhong", "tusizi",
    "danggui", "shudihuang", "heshouwu", "baishao", "ejiao",
    "maidong", "gouqizi",
    "shanzhuyu", "wuweizi"
]

def clean_effects(text):
    """清理功效字段"""
    if not text:
        return ""
    
    # 移除截断标记
    text = re.sub(r'……/?>\s*$', '', text)
    text = re.sub(r'\.\.\.?\s*$', '', text)
    
    # 提取功效部分（在【临床应用】之前）
    match = re.search(r'^([^【]+)', text)
    if match:
        effects = match.group(1).strip()
        # 清理多余的空白和标点
        effects = re.sub(r'\s+', ' ', effects)
        effects = re.sub(r'。+', '。', effects)
        return effects
    
    return text.strip()

def clean_indications(text, herb_name, effects):
    """生成主治病症"""
    if text and len(text) > 20 and '……' not in text:
        return text
    
    # 根据功效生成主治
    if effects:
        # 提取关键词
        keywords = []
        if '解表' in effects:
            keywords.append('感冒')
        if '清热' in effects:
            keywords.append('热证')
        if '活血' in effects:
            keywords.append('血瘀')
        if '补气' in effects:
            keywords.append('气虚')
        if '补血' in effects:
            keywords.append('血虚')
        if '祛湿' in effects or '利水' in effects:
            keywords.append('水肿')
        if '止咳' in effects or '化痰' in effects:
            keywords.append('咳嗽')
        if '安神' in effects:
            keywords.append('失眠')
        
        if keywords:
            return f"用于{herb_name}主治{ '、'.join(keywords[:3]) }等症。"
    
    return f"用于{herb_name}相关病症。"

def clean_usage(text):
    """清理用法用量"""
    if not text:
        return "煎服，适量"
    
    # 移除截断标记
    text = re.sub(r'……/?>\s*$', '', text)
    
    # 简化过长的内容
    if len(text) > 100:
        # 提取煎服和剂量
        match = re.search(r'(煎服|内服).*?(\d+[^，。]*)', text)
        if match:
            return f"{match.group(1)}，{match.group(2)}"
    
    return text.strip() or "煎服，适量"

def clean_contraindications(text):
    """清理禁忌"""
    if not text:
        return "孕妇慎用"
    
    # 标准化常见表述
    text = text.strip()
    if len(text) < 5:
        return "孕妇慎用；阴虚火旺者慎用"
    
    return text

def clean_keypoint(text, effects, herb_name):
    """生成考试重点"""
    if text and len(text) >= 8 and '……' not in text:
        return text
    
    # 从功效中提取关键词
    if effects:
        if '解表' in effects:
            return '解表散寒'
        if '清热' in effects:
            return '清热解毒'
        if '活血' in effects:
            return '活血化瘀'
        if '补气' in effects:
            return '补气健脾'
        if '补血' in effects:
            return '补血养阴'
        if '利水' in effects:
            return '利水消肿'
        if '安神' in effects:
            return '安神定志'
        if '平肝' in effects:
            return '平肝息风'
    
    return f"{herb_name}主要功效"

def generate_memory_tip(herb_name, effects, category):
    """生成记忆口诀"""
    # 从功效中提取关键词
    keywords = []
    if effects:
        if '发汗' in effects:
            keywords.append('发汗')
        if '解表' in effects:
            keywords.append('解表')
        if '清热' in effects:
            keywords.append('清热')
        if '活血' in effects:
            keywords.append('活血')
        if '补气' in effects:
            keywords.append('补气')
        if '利水' in effects:
            keywords.append('利水')
    
    if keywords:
        return f"{herb_name}{''.join(keywords[:2])}"
    
    return f"{herb_name}功效记心间"

def generate_association(herb_name, effects):
    """生成趣味联想"""
    associations = {
        '麻黄': '麻黄像个小太阳，发汗解表暖洋洋',
        '桂枝': '桂枝像春天的树枝，温通经脉暖人心',
        '人参': '人参像个小精灵，补气生津精神好',
        '当归': '当归就像回家，补血活血最重要',
    }
    
    if herb_name in associations:
        return associations[herb_name]
    
    # 根据功效生成
    if effects:
        if '解表' in effects:
            return f"{herb_name}像温暖的阳光，驱散风寒保健康"
        if '清热' in effects:
            return f"{herb_name}像清凉的泉水，清热泻火解暑热"
        if '补气' in effects:
            return f"{herb_name}像能量棒，补气提神精神爽"
        if '活血' in effects:
            return f"{herb_name}像清道夫，活血化瘀通经络"
    
    return f"{herb_name}功效独特，临床应用广泛"

def repair_herb(herb, is_core=False):
    """修复单个药材"""
    name = herb['name']
    effects_raw = herb.get('effects', '')
    if isinstance(effects_raw, list):
        effects_raw = ' '.join(effects_raw)
    
    # 修复effects
    effects = clean_effects(effects_raw)
    if effects:
        herb['effects'] = effects
    
    # 修复indications
    indications_raw = herb.get('indications', '')
    if isinstance(indications_raw, list):
        indications_raw = ' '.join(indications_raw)
    indications = clean_indications(indications_raw, name, effects)
    herb['indications'] = indications
    
    # 修复usage
    usage_raw = herb.get('usage', '')
    if isinstance(usage_raw, list):
        usage_raw = ' '.join(usage_raw)
    usage = clean_usage(usage_raw)
    herb['usage'] = usage
    
    # 修复contraindications
    contraindications_raw = herb.get('contraindications', '')
    if isinstance(contraindications_raw, list):
        contraindications_raw = ' '.join(contraindications_raw)
    contraindications = clean_contraindications(contraindications_raw)
    herb['contraindications'] = contraindications
    
    # 修复keyPoint
    keypoint_raw = herb.get('keyPoint', '')
    if isinstance(keypoint_raw, list):
        keypoint_raw = ' '.join(keypoint_raw)
    keypoint = clean_keypoint(keypoint_raw, effects, name)
    herb['keyPoint'] = keypoint
    
    # 如果是核心药材，生成更好的memoryTip和association
    if is_core:
        memorytip = herb.get('memoryTip', '')
        if isinstance(memorytip, list):
            memorytip = ' '.join(memorytip)
        if not memorytip or len(memorytip) < 10 or '临床应用' in memorytip:
            herb['memoryTip'] = generate_memory_tip(name, effects, herb.get('category', ''))
        
        association = herb.get('association', '')
        if isinstance(association, list):
            association = ' '.join(association)
        if not association or len(association) < 15:
            herb['association'] = generate_association(name, effects)
    
    return herb

def main():
    print("=" * 60)
    print("🌿 HerbMind 药材数据智能修复")
    print("=" * 60)
    
    # 加载数据
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        herbs = json.load(f)
    
    print(f"\n📚 加载了 {len(herbs)} 味药材")
    
    # 备份
    with open(DATA_FILE + '.backup', 'w', encoding='utf-8') as f:
        json.dump(herbs, f, ensure_ascii=False, indent=2)
    print("💾 已备份数据")
    
    # 统计
    core_count = 0
    repaired_count = 0
    
    print("\n🔧 开始修复...")
    
    for i, herb in enumerate(herbs, 1):
        is_core = herb['id'] in CORE_HERBS
        if is_core:
            core_count += 1
        
        # 修复
        repair_herb(herb, is_core)
        repaired_count += 1
        
        if i % 50 == 0:
            print(f"  已处理 {i}/{len(herbs)} 味...")
    
    # 保存
    with open(DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(herbs, f, ensure_ascii=False, indent=2)
    
    print(f"\n✅ 修复完成！")
    print(f"  - 总药材: {len(herbs)} 味")
    print(f"  - 核心药材: {core_count} 味")
    print(f"  - 已修复: {repaired_count} 味")
    print(f"\n💾 数据已保存到 {DATA_FILE}")
    print("=" * 60)

if __name__ == "__main__":
    main()
