#!/usr/bin/env python3
"""
修复方剂数据 - 正确处理 HTML 表格结构
"""

import json
import re
import time
import urllib.request
import ssl

BASE_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/"
LANG = "chs"

# 禁用 SSL 验证
ssl._create_default_https_context = ssl._create_unverified_context

# 加载现有数据
with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas.json', 'r', encoding='utf-8') as f:
    formulas = json.load(f)

# 加载药材数据
with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/herbs_hkbu.json', 'r', encoding='utf-8') as f:
    herbs_data = json.load(f)

# 创建药材名称到ID的映射
herb_name_to_id = {}
for herb in herbs_data:
    herb_name_to_id[herb['name']] = herb['id']
    if herb.get('aliases'):
        for alias in herb['aliases']:
            herb_name_to_id[alias] = herb['id']
    if herb['pinyin']:
        herb_name_to_id[herb['pinyin']] = herb['id']
        herb_name_to_id[herb['pinyin'].lower()] = herb['id']

print(f"Loaded {len(formulas)} formulas")
print(f"Loaded {len(herbs_data)} herbs")

# 从 HTML 中提取字段值的函数
def extract_field_value(html, field_name):
    """
    从表格结构中提取字段值
    格式: 【字段名】</td>...<td>值</td>
    """
    # 处理带空格的字段名（如【分类 】）
    pattern = rf'【{field_name}】\s*</td>\s*<td[^>]*>\s*([^<]+)'
    match = re.search(pattern, html, re.DOTALL)
    if match:
        value = match.group(1).strip()
        # 移除 HTML 实体
        value = value.replace('&nbsp;', ' ')
        value = ' '.join(value.split())  # 规范化空白
        return value
    return ''

# 解析组成药材
def parse_ingredients(composition_text):
    ingredients = []
    herbs_list = []

    # 清理文本
    composition_text = composition_text.strip()

    # 按空格分割
    parts = composition_text.split()

    for part in parts:
        part = part.strip()
        if not part or len(part) < 2:
            continue

        # 跳过剂量信息
        if re.match(r'^[(（各\d半少适一-十两钱分g末]', part):
            continue

        # 去掉炮制后缀
        herb_name = part
        herb_name = re.sub(r'(去皮|去心|去芦|去毛|去节|去根|去梗|去蒂|去子|去核|去壳|去油|去足|去翅|去头|去尾|去骨|去筋|去心焙|焙|炒|炙|制|煅|淬|研|末|碎|切|片|段|块|姜汁炙|米泔水浸|蜜炒黄)$', '', herb_name)

        if len(herb_name) < 2:
            continue

        # 查找药材ID
        herb_id = herb_name_to_id.get(herb_name)

        # 尝试模糊匹配
        if not herb_id:
            for name, hid in herb_name_to_id.items():
                if len(name) >= 2 and (herb_name in name or name in herb_name):
                    herb_id = hid
                    break

        ingredients.append({
            'herb_name': herb_name,
            'herb_id': herb_id,
            'original_text': part
        })
        if herb_id and herb_id not in herbs_list:
            herbs_list.append(herb_id)

    return ingredients, herbs_list


headers = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
}

# 先测试一个方剂
test_fid = "F00147"  # 藿香正气散
print(f"\nTest: {test_fid}")

url = f"{BASE_URL}detail.php?lang={LANG}&id={test_fid}"
req = urllib.request.Request(url, headers=headers)

with urllib.request.urlopen(req, timeout=30) as response:
    html = response.read().decode('utf-8')

    # 提取各字段
    name = extract_field_value(html, '中文')
    pinyin = extract_field_value(html, '汉语')
    english = extract_field_value(html, '英文')
    category = extract_field_value(html, '分类\s*')
    source = extract_field_value(html, '出处')

    print(f"  Name: {name}")
    print(f"  Pinyin: {pinyin}")
    print(f"  English: {english}")
    print(f"  Category: {category}")
    print(f"  Source: {source}")

    # 提取组成
    composition = extract_field_value(html, '组成')
    print(f"  Composition: {composition[:100]}...")

    if composition:
        ingredients, herbs_list = parse_ingredients(composition)
        print(f"  Parsed ingredients: {len(ingredients)}, matched: {len(herbs_list)}")
        for ing in ingredients[:10]:
            print(f"    - {ing['herb_name']} ({ing['herb_id'] or 'not matched'})")
