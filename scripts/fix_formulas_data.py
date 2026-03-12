#!/usr/bin/env python3
"""
修复方剂数据 - 基于正确的字段标记重新抓取
"""

import json
import re
import time
from playwright.sync_api import sync_playwright

BASE_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/"
LANG = "chs"

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
    # 添加别名映射
    if herb.get('aliases'):
        for alias in herb['aliases']:
            herb_name_to_id[alias] = herb['id']
    if herb['pinyin']:
        herb_name_to_id[herb['pinyin']] = herb['id']
        herb_name_to_id[herb['pinyin'].lower()] = herb['id']

print(f"Loaded {len(formulas)} formulas")
print(f"Loaded {len(herbs_data)} herbs")

# 辅助函数：解析组成药材
def parse_ingredients(composition_text):
    """
    解析组成字段，提取单个药材
    格式示例："大腹皮 白芷 紫苏 茯苓去皮，各一两 (30g) 半夏曲..."
    """
    ingredients = []
    herbs_list = []

    # 清理文本
    composition_text = composition_text.strip()

    # 常见剂量模式
    dose_patterns = [
        r'各[一二三四五六七八九十百千]+两?\s*\(\d+g\)',
        r'[一二三四五六七八九十百千]+两?\s*\(\d+g\)',
        r'\d+g',
        r'[一二三四五六七八九十百千]+钱',
        r'[一二三四五六七八九十百千]+分',
        r'半两',
        r'少许',
        r'适量',
        r'等分',
    ]

    # 按空格分割，但保留剂量信息
    parts = composition_text.split()

    i = 0
    while i < len(parts):
        part = parts[i].strip()
        if not part:
            i += 1
            continue

        # 检查是否是纯剂量信息（不以汉字开头）
        if re.match(r'^[(（各\d]', part):
            i += 1
            continue

        # 提取药材名称（去掉炮制说明如去皮、去土等）
        herb_name = part

        # 去掉炮制后缀
        herb_name = re.sub(r'(去皮|去心|去芦|去毛|去节|去根|去梗|去蒂|去子|去核|去壳|去油|去足|去翅|去头|去尾|去骨|去筋|去心焙|焙|炒|炙|制|煅|淬|研|末|碎|切|片|段|块)$', '', herb_name)

        # 查找药材ID
        herb_id = herb_name_to_id.get(herb_name)

        # 尝试模糊匹配
        if not herb_id and len(herb_name) >= 2:
            for name, hid in herb_name_to_id.items():
                if herb_name in name or name in herb_name:
                    herb_id = hid
                    break

        if herb_name and len(herb_name) >= 2:
            ingredients.append({
                'herb_name': herb_name,
                'herb_id': herb_id,
                'original_text': part
            })
            if herb_id and herb_id not in herbs_list:
                herbs_list.append(herb_id)

        i += 1

    return ingredients, herbs_list


# 重新爬取每个方剂
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        user_agent='Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
    )
    page = context.new_page()

    for idx, formula in enumerate(formulas):
        fid = formula['id']
        print(f"\n[{idx+1}/{len(formulas)}] Fixing formula {fid}...")

        try:
            detail_url = f"{BASE_URL}detail.php?lang={LANG}&id={fid}"
            page.goto(detail_url)
            page.wait_for_load_state('networkidle')
            time.sleep(0.5)

            # 获取页面文本内容
            content_text = page.inner_text('body')

            # 提取方名 - 【中文】
            name_match = re.search(r'【中文】\s*([^【\n]+)', content_text)
            if name_match:
                formula['name'] = name_match.group(1).strip()
                print(f"  Name: {formula['name']}")

            # 提取拼音 - 【汉语】
            pinyin_match = re.search(r'【汉语】\s*([^【\n]+)', content_text)
            if pinyin_match:
                formula['pinyin'] = pinyin_match.group(1).strip()

            # 提取英文名 - 【英文】
            eng_match = re.search(r'【英文】\s*([^【\n]+)', content_text)
            if eng_match:
                formula['english_name'] = eng_match.group(1).strip()

            # 提取类别 - 【分类 】
            cat_match = re.search(r'【分类\s*】\s*([^【\n]+)', content_text)
            if cat_match:
                formula['category'] = cat_match.group(1).strip()

            # 提取出处 - 【出处】
            source_match = re.search(r'【出处】\s*([^【\n]+)', content_text)
            if source_match:
                formula['source'] = source_match.group(1).strip()

            # 提取功用 - 【功用】
            func_match = re.search(r'【功用】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if func_match:
                formula['function'] = func_match.group(1).strip().replace('\n', ' ')

            # 提取主治 - 【主治】
            ind_match = re.search(r'【主治】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if ind_match:
                formula['indication'] = ind_match.group(1).strip().replace('\n', ' ')

            # 提取组成 - 【组成】
            comp_match = re.search(r'【组成】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if comp_match:
                composition = comp_match.group(1).strip()
                ingredients, herbs_list = parse_ingredients(composition)
                formula['ingredients'] = ingredients
                formula['herbs'] = herbs_list
                print(f"  Ingredients: {len(ingredients)} herbs, matched: {len(herbs_list)}")

            # 提取方歌 - 【方歌】
            song_match = re.search(r'【方歌】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if song_match:
                formula['song'] = song_match.group(1).strip().replace('\n', ' ')

            # 提取用法 - 【用法】
            usage_match = re.search(r'【用法】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if usage_match:
                formula['usage'] = usage_match.group(1).strip().replace('\n', ' ')

            # 提取注意事项 - 【注意事项】
            prec_match = re.search(r'【注意事项】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if prec_match:
                formula['precautions'] = prec_match.group(1).strip().replace('\n', ' ')

            # 每10个保存一次
            if (idx + 1) % 10 == 0:
                with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas_fixed.json', 'w', encoding='utf-8') as f:
                    json.dump(formulas, f, ensure_ascii=False, indent=2)
                print(f"  Saved progress: {idx+1}/{len(formulas)}")

        except Exception as e:
            print(f"Error fixing formula {fid}: {e}")
            continue

    browser.close()

# 保存最终结果
output_path = '/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas.json'
with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(formulas, f, ensure_ascii=False, indent=2)

print(f"\n\n{'='*50}")
print(f"Fixed {len(formulas)} formulas")

# 统计
stats = {
    'with_name': sum(1 for f in formulas if f.get('name') and '中药方剂图像数据库' not in f['name']),
    'with_pinyin': sum(1 for f in formulas if f.get('pinyin')),
    'with_english': sum(1 for f in formulas if f.get('english_name')),
    'with_category': sum(1 for f in formulas if f.get('category')),
    'with_herbs': sum(1 for f in formulas if f.get('herbs')),
}

print("\nStats:")
for key, value in stats.items():
    print(f"  {key}: {value}/{len(formulas)}")
