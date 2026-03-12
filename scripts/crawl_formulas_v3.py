#!/usr/bin/env python3
"""
爬取 HKBU 中药方剂数据库 - 版本3
基于实际页面结构优化提取逻辑
"""

import json
import re
import time
from playwright.sync_api import sync_playwright

BASE_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/"
LANG = "chs"

# 加载现有药材数据
with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/herbs_hkbu.json', 'r', encoding='utf-8') as f:
    herbs_data = json.load(f)

# 创建药材名称到ID的映射
herb_name_to_id = {}
for herb in herbs_data:
    herb_name_to_id[herb['name']] = herb['id']
    # 添加更多别名映射
    if herb['pinyin']:
        herb_name_to_id[herb['pinyin']] = herb['id']
        herb_name_to_id[herb['pinyin'].lower()] = herb['id']

print(f"Loaded {len(herbs_data)} herbs")
print(f"Name mapping: {len(herb_name_to_id)} entries")

formulas = []

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        user_agent='Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
    )
    page = context.new_page()

    # 收集所有方剂ID - 遍历所有页面
    formula_ids = []
    for page_num in range(1, 50):
        print(f"\nProcessing page {page_num}...")
        try:
            list_url = f"{BASE_URL}index.php?lang={LANG}&page={page_num}"
            page.goto(list_url)
            page.wait_for_load_state('networkidle')
            time.sleep(1)

            # 获取页面所有链接
            links = page.locator('a[href*="detail.php"]').all()
            page_ids = []
            for link in links:
                href = link.get_attribute('href')
                if href:
                    match = re.search(r'id=([A-Z0-9]+)', href)
                    if match:
                        fid = match.group(1)
                        if fid not in formula_ids and fid not in page_ids:
                            page_ids.append(fid)

            if not page_ids:
                print("No more formulas found.")
                break

            formula_ids.extend(page_ids)
            print(f"Found {len(page_ids)} new formulas, total: {len(formula_ids)}")

        except Exception as e:
            print(f"Error on page {page_num}: {e}")
            break

    print(f"\n\nTotal formula IDs: {len(formula_ids)}")

    # 爬取每个方剂的详情
    for idx, fid in enumerate(formula_ids):
        print(f"\n[{idx+1}/{len(formula_ids)}] Fetching formula {fid}...")

        try:
            detail_url = f"{BASE_URL}detail.php?lang={LANG}&id={fid}"
            page.goto(detail_url)
            page.wait_for_load_state('networkidle')
            time.sleep(1.5)

            formula = {
                'id': fid,
                'name': '',
                'pinyin': '',
                'english_name': '',
                'category': '',
                'source': '',
                'function': '',
                'indication': '',
                'pathogenesis': '',
                'usage': '',
                'key_points': '',
                'modern_usage': '',
                'precautions': '',
                'song': '',
                'ingredients': [],
                'herbs': [],
                'related_formulas': [],
                'image_url': f"{BASE_URL}images/{LANG}/{fid}s.jpg",  # 方解表图片
                'source_url': detail_url
            }

            # 获取页面文本内容
            try:
                content_text = page.inner_text('body')
            except:
                content_text = ""

            # 提取方名 - 从页面内容中的【方名】字段
            name_match = re.search(r'【方名】\s*([^【\n]+)', content_text)
            if name_match:
                formula['name'] = name_match.group(1).strip()
            else:
                # 尝试从标题提取
                title = page.title()
                title = re.sub(r'\s*[-–]\s*中药方剂图像数据库.*', '', title)
                formula['name'] = title.strip()

            # 提取拼音
            pinyin_match = re.search(r'【汉语拼音】\s*([^【\n]+)', content_text)
            if pinyin_match:
                formula['pinyin'] = pinyin_match.group(1).strip()

            # 提取英文名
            eng_match = re.search(r'【英文名】\s*([^【\n]+)', content_text)
            if eng_match:
                formula['english_name'] = eng_match.group(1).strip()

            # 提取类别
            cat_match = re.search(r'【方剂类别】\s*([^【\n]+)', content_text)
            if cat_match:
                formula['category'] = cat_match.group(1).strip()

            # 提取出处
            source_match = re.search(r'【出处】\s*([^【\n]+)', content_text)
            if source_match:
                formula['source'] = source_match.group(1).strip()

            # 提取功用
            func_match = re.search(r'【功用】\s*([^【\n]+)', content_text)
            if func_match:
                formula['function'] = func_match.group(1).strip()

            # 提取主治
            ind_match = re.search(r'【主治】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if ind_match:
                formula['indication'] = ind_match.group(1).strip().replace('\n', ' ')

            # 提取组成 - 关键字段
            comp_match = re.search(r'【组成】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if comp_match:
                composition = comp_match.group(1).strip()
                # 解析组成药材 - 处理多行格式
                lines = composition.split('\n')
                for line in lines:
                    line = line.strip()
                    if not line:
                        continue

                    # 提取药材名称（去掉剂量数字和单位）
                    # 格式如：人参 9g 或 人参(9g) 或 人参三钱
                    herb_name_match = re.match(r'^([^0-9（(]+)', line)
                    if herb_name_match:
                        herb_name = herb_name_match.group(1).strip()
                        # 去掉炮制说明
                        herb_name_clean = re.sub(r'[炒炙制煅淬麸炒蜜炙醋炙盐炙研]$', '', herb_name)

                        # 查找对应的药材ID
                        herb_id = herb_name_to_id.get(herb_name) or herb_name_to_id.get(herb_name_clean)

                        ingredient = {
                            'herb_name': herb_name,
                            'herb_id': herb_id,
                            'original_text': line
                        }
                        formula['ingredients'].append(ingredient)

                        if herb_id and herb_id not in formula['herbs']:
                            formula['herbs'].append(herb_id)

            # 提取方歌
            song_match = re.search(r'【方歌】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if song_match:
                formula['song'] = song_match.group(1).strip().replace('\n', ' ')

            # 提取用法
            usage_match = re.search(r'【用法】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if usage_match:
                formula['usage'] = usage_match.group(1).strip().replace('\n', ' ')

            # 提取注意事项
            prec_match = re.search(r'【注意事项】\s*([^【]+?)(?=【|$)', content_text, re.DOTALL)
            if prec_match:
                formula['precautions'] = prec_match.group(1).strip().replace('\n', ' ')

            print(f"  Name: {formula['name']}")
            print(f"  Herbs: {len(formula['herbs'])}/{len(formula['ingredients'])}")
            if formula['herbs']:
                print(f"  Matched: {formula['herbs']}")

            formulas.append(formula)

            # 每10个保存一次
            if (idx + 1) % 10 == 0:
                with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas_temp.json', 'w', encoding='utf-8') as f:
                    json.dump(formulas, f, ensure_ascii=False, indent=2)
                print(f"  Saved: {len(formulas)} formulas")

        except Exception as e:
            print(f"Error fetching formula {fid}: {e}")
            import traceback
            traceback.print_exc()
            continue

    browser.close()

# 保存最终结果
print(f"\n\n{'='*50}")
print(f"Total formulas collected: {len(formulas)}")

# 统计有多少方剂与药材关联
herbs_with_formulas = set()
for f in formulas:
    herbs_with_formulas.update(f['herbs'])

print(f"Herbs with formulas: {len(herbs_with_formulas)}/{len(herbs_data)}")

# 保存方剂数据
output_path = '/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas.json'
with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(formulas, f, ensure_ascii=False, indent=2)

print(f"Saved: {output_path}")

# 同时更新药材数据，添加 related_formulas 字段
for herb in herbs_data:
    herb['related_formulas'] = []

for formula in formulas:
    for herb_id in formula['herbs']:
        for herb in herbs_data:
            if herb['id'] == herb_id:
                herb['related_formulas'].append(formula['id'])
                break

# 统计
herbs_with_formulas_count = sum(1 for h in herbs_data if h['related_formulas'])
print(f"Herbs with related formulas: {herbs_with_formulas_count}/{len(herbs_data)}")

# 保存更新后的药材数据
herbs_output_path = '/Users/lijie/workspace/herbmind/hkbu_data/final_data/herbs_hkbu_with_formulas.json'
with open(herbs_output_path, 'w', encoding='utf-8') as f:
    json.dump(herbs_data, f, ensure_ascii=False, indent=2)

print(f"Saved: {herbs_output_path}")

# 生成统计报告
report = {
    'total_formulas': len(formulas),
    'total_herbs': len(herbs_data),
    'herbs_with_formulas': herbs_with_formulas_count,
    'herbs_without_formulas': len(herbs_data) - herbs_with_formulas_count,
    'coverage_rate': f"{herbs_with_formulas_count/len(herbs_data)*100:.1f}%"
}

print("\n=== Report ===")
print(json.dumps(report, ensure_ascii=False, indent=2))
