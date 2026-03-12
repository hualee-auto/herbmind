#!/usr/bin/env python3
"""
修复所有方剂数据
"""

import json
import re
import time
import urllib.request
import ssl

BASE_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/"
LANG = "chs"

ssl._create_default_https_context = ssl._create_unverified_context

# 加载数据
with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas.json', 'r', encoding='utf-8') as f:
    formulas = json.load(f)

with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/herbs_hkbu.json', 'r', encoding='utf-8') as f:
    herbs_data = json.load(f)

# 创建映射
herb_name_to_id = {}
for herb in herbs_data:
    herb_name_to_id[herb['name']] = herb['id']
    if herb.get('aliases'):
        for alias in herb['aliases']:
            herb_name_to_id[alias] = herb['id']
    if herb['pinyin']:
        herb_name_to_id[herb['pinyin']] = herb['id']
        herb_name_to_id[herb['pinyin'].lower()] = herb['id']

# 特殊映射
herb_name_to_id['藿香'] = 'huoxiang'
herb_name_to_id['苏叶'] = 'zisugeng'
herb_name_to_id['苏梗'] = 'zisugeng'

print(f"Loaded {len(formulas)} formulas, {len(herbs_data)} herbs")

def extract_field(html, field_pattern):
    pattern = rf'【{field_pattern}】\s*</td>\s*<td[^>]*>\s*([^<]+)'
    match = re.search(pattern, html, re.DOTALL)
    if match:
        value = match.group(1).strip()
        value = value.replace('&nbsp;', ' ')
        value = ' '.join(value.split())
        return value
    return ''

def parse_ingredients(text):
    ingredients = []
    herbs_list = []

    for part in text.split():
        part = part.strip()
        if not part or len(part) < 2:
            continue
        if re.match(r'^[(（各\d半少适一-十两钱分g末]', part):
            continue

        herb_name = re.sub(r'(去皮|去心|去芦|去毛|去节|去根|去梗|去蒂|去子|去核|去壳|去油|去足|去翅|去头|去尾|去骨|去筋|去心焙|焙|炒|炙|制|煅|淬|研|末|碎|切|片|段|块|姜汁炙|米泔水浸|蜜炒黄)$', '', part)

        if len(herb_name) < 2:
            continue

        herb_id = herb_name_to_id.get(herb_name)
        if not herb_id:
            for name, hid in herb_name_to_id.items():
                if len(name) >= 2 and (herb_name in name or name in herb_name):
                    herb_id = hid
                    break

        ingredients.append({'herb_name': herb_name, 'herb_id': herb_id, 'original_text': part})
        if herb_id and herb_id not in herbs_list:
            herbs_list.append(herb_id)

    return ingredients, herbs_list

headers = {'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'}

# 修复每个方剂
for idx, formula in enumerate(formulas):
    fid = formula['id']
    print(f"[{idx+1}/{len(formulas)}] Fixing {fid}...")

    try:
        url = f"{BASE_URL}detail.php?lang={LANG}&id={fid}"
        req = urllib.request.Request(url, headers=headers)

        with urllib.request.urlopen(req, timeout=30) as response:
            html = response.read().decode('utf-8')

            formula['name'] = extract_field(html, '中文')
            formula['pinyin'] = extract_field(html, '汉语')
            formula['english_name'] = extract_field(html, '英文')
            formula['category'] = extract_field(html, '分类\s*')
            formula['source'] = extract_field(html, '出处')
            formula['function'] = extract_field(html, '功用')
            formula['indication'] = extract_field(html, '主治')
            formula['usage'] = extract_field(html, '用法')
            formula['precautions'] = extract_field(html, '注意事项')
            formula['song'] = extract_field(html, '方歌')

            composition = extract_field(html, '组成')
            if composition:
                ingredients, herbs_list = parse_ingredients(composition)
                formula['ingredients'] = ingredients
                formula['herbs'] = herbs_list

            print(f"  -> {formula['name'][:20] if formula['name'] else 'N/A'} ({len(formula.get('herbs', []))} herbs)")

        if (idx + 1) % 10 == 0:
            with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas_temp.json', 'w', encoding='utf-8') as f:
                json.dump(formulas, f, ensure_ascii=False, indent=2)
            print(f"  Saved progress: {idx+1}")

        time.sleep(0.3)

    except Exception as e:
        print(f"Error: {e}")
        continue

# 保存结果
output_path = '/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas.json'
with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(formulas, f, ensure_ascii=False, indent=2)

print(f"\n{'='*50}")
print(f"Fixed {len(formulas)} formulas")

stats = {
    'with_name': sum(1 for f in formulas if f.get('name') and '中药' not in f['name']),
    'with_pinyin': sum(1 for f in formulas if f.get('pinyin')),
    'with_english': sum(1 for f in formulas if f.get('english_name')),
    'with_category': sum(1 for f in formulas if f.get('category')),
    'with_herbs': sum(1 for f in formulas if f.get('herbs')),
}
print("\nStats:")
for key, value in stats.items():
    print(f"  {key}: {value}/{len(formulas)}")
