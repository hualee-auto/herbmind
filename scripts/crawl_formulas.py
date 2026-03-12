#!/usr/bin/env python3
"""
爬取 HKBU 中药方剂数据库
- 遍历所有方剂列表页
- 获取每个方剂的详情
- 提取组成药材、功效、主治、方歌等信息
- 生成 formulas.json 并与 herbs_hkbu.json 建立关联
"""

import json
import re
import time
from urllib.parse import urljoin, parse_qs, urlparse
from playwright.sync_api import sync_playwright

BASE_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/"
LIST_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/index.php"
DETAIL_URL = "https://sys01.lib.hkbu.edu.hk/cmed/cmfid/detail.php"
LANG = "chs"  # 简体中文

# 加载现有药材数据
with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/herbs_hkbu.json', 'r', encoding='utf-8') as f:
    herbs_data = json.load(f)

# 创建药材名称到ID的映射
herb_name_to_id = {}
for herb in herbs_data:
    herb_name_to_id[herb['name']] = herb['id']
    # 也添加拼音小写映射（因为方剂中可能用拼音或别名）
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

    # 先获取总页数
    print("Getting total pages...")
    page.goto(f"{BASE_URL}?lang={LANG}")
    page.wait_for_load_state('networkidle')
    time.sleep(2)

    # 查找页码信息
    page_text = page.content()
    # 尝试提取总页数
    total_match = re.search(r'共\s*(\d+)\s*筆', page_text) or re.search(r'共\s*(\d+)\s*笔', page_text)
    if total_match:
        total_formulas = int(total_match.group(1))
        print(f"Total formulas: {total_formulas}")
    else:
        # 从列表中统计
        total_formulas = 182  # 已知数量
        print(f"Using known total: {total_formulas}")

    # 收集所有方剂ID
    formula_ids = []

    # 遍历所有列表页
    current_page = 1
    while True:
        print(f"\nProcessing page {current_page}...")

        list_url = f"{LIST_URL}?lang={LANG}&page={current_page}"
        try:
            page.goto(list_url)
            page.wait_for_load_state('domcontentloaded')
            time.sleep(1)

            # 查找方剂链接
            content = page.content()

            # 提取 detail.php?lang=xxx&id=XXX 链接
            id_matches = re.findall(r'detail\.php\?[^"\']*id=([A-Z0-9]+)', content)

            if not id_matches:
                # 尝试其他格式
                id_matches = re.findall(r'["\']detail\.php\?[^"\']*id=([A-Z0-9]+)', content)

            if not id_matches and current_page > 1:
                print("No more formulas found, stopping.")
                break

            for fid in id_matches:
                if fid not in formula_ids:
                    formula_ids.append(fid)

            print(f"Found {len(id_matches)} formulas on this page, total: {len(formula_ids)}")

            # 检查是否还有下一页
            if '下一頁' not in content and '下一页' not in content and 'Next' not in content:
                if current_page >= 10:  # 安全限制
                    break

            current_page += 1

            if current_page > 20:  # 安全限制
                break

        except Exception as e:
            print(f"Error on page {current_page}: {e}")
            break

    print(f"\nTotal formula IDs collected: {len(formula_ids)}")

    # 爬取每个方剂的详情
    for idx, fid in enumerate(formula_ids):
        print(f"\n[{idx+1}/{len(formula_ids)}] Fetching formula {fid}...")

        try:
            detail_url = f"{DETAIL_URL}?lang={LANG}&id={fid}"
            print(f"  URL: {detail_url}")
            page.goto(detail_url)
            page.wait_for_load_state('networkidle')
            time.sleep(2)

            content = page.content()

            # 尝试从页面文本获取内容
            page_text = page.inner_text('body') if page.locator('body').count() > 0 else ""

            # 解析方剂详情
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
                'ingredients': [],  # 组成药材
                'herbs': [],  # 关联的药材ID列表
                'related_formulas': []
            }

            # 提取名称 - 尝试多种模式
            name_match = re.search(r'<title>([^<]+)</title>', content)
            if name_match:
                title = name_match.group(1)
                # 清理标题
                title = re.sub(r'\s*-\s*HKBU', '', title, flags=re.IGNORECASE)
                formula['name'] = title.strip()

            # 从内容中提取方名
            if not formula['name']:
                name_match = re.search(r'方名[:\s]*([^<\n]+)', content)
                if name_match:
                    formula['name'] = name_match.group(1).strip()

            # 提取拼音
            pinyin_match = re.search(r'漢語拼音[:\s]*([^<\n]+)', content) or \
                          re.search(r'汉语拼音[:\s]*([^<\n]+)', content) or \
                          re.search(r'Pinyin[:\s]*([^<\n]+)', content, re.IGNORECASE)
            if pinyin_match:
                formula['pinyin'] = pinyin_match.group(1).strip()

            # 提取英文名
            eng_match = re.search(r'英文名[:\s]*([^<\n]+)', content, re.IGNORECASE)
            if eng_match:
                formula['english_name'] = eng_match.group(1).strip()

            # 提取类别
            cat_match = re.search(r'方劑類別[:\s]*([^<\n]+)', content) or \
                       re.search(r'方剂类别[:\s]*([^<\n]+)', content) or \
                       re.search(r'分類[:\s]*([^<\n]+)', content)
            if cat_match:
                formula['category'] = cat_match.group(1).strip()

            # 提取出处
            source_match = re.search(r'出處[:\s]*([^<\n]+)', content) or \
                          re.search(r'出处[:\s]*([^<\n]+)', content)
            if source_match:
                formula['source'] = source_match.group(1).strip()

            # 提取功用
            func_match = re.search(r'功用[:\s]*([^<\n]+)', content) or \
                        re.search(r'功能[:\s]*([^<\n]+)', content)
            if func_match:
                formula['function'] = func_match.group(1).strip()

            # 提取主治
            ind_match = re.search(r'主治[:\s]*([^<\n]+)', content)
            if ind_match:
                formula['indication'] = ind_match.group(1).strip()

            # 提取组成 - 这是关键字段
            comp_match = re.search(r'組成[:\s]*([^<\n]+)', content) or \
                        re.search(r'组成[:\s]*([^<\n]+)', content)
            if comp_match:
                composition = comp_match.group(1).strip()
                # 解析组成药材
                # 格式通常是：药材名 剂量; 药材名 剂量
                herb_parts = re.split(r'[;；]', composition)

                for part in herb_parts:
                    part = part.strip()
                    if not part:
                        continue

                    # 提取药材名称（去掉剂量数字和单位）
                    herb_name_match = re.match(r'^([^0-9]+)', part)
                    if herb_name_match:
                        herb_name = herb_name_match.group(1).strip()
                        # 去掉炮制说明如"炒"、"炙"等
                        herb_name = re.sub(r'[炒炙制煅淬]$', '', herb_name)

                        # 查找对应的药材ID
                        herb_id = herb_name_to_id.get(herb_name)

                        ingredient = {
                            'herb_name': herb_name,
                            'herb_id': herb_id,
                            'original_text': part
                        }
                        formula['ingredients'].append(ingredient)

                        if herb_id:
                            formula['herbs'].append(herb_id)

            # 提取方歌
            song_match = re.search(r'方歌[:\s]*([^<]+?)(?:</|\n)', content) or \
                        re.search(r'方歌[:\s]*<[^>]*>([^<]+)', content)
            if song_match:
                formula['song'] = song_match.group(1).strip()

            # 提取用法
            usage_match = re.search(r'用法[:\s]*([^<\n]+)', content)
            if usage_match:
                formula['usage'] = usage_match.group(1).strip()

            # 提取注意事项
            prec_match = re.search(r'注意事項[:\s]*([^<\n]+)', content) or \
                        re.search(r'注意事项[:\s]*([^<\n]+)', content)
            if prec_match:
                formula['precautions'] = prec_match.group(1).strip()

            print(f"  Name: {formula['name']}")
            print(f"  Herbs: {len(formula['herbs'])}/{len(formula['ingredients'])}")

            formulas.append(formula)

            # 每10个保存一次
            if (idx + 1) % 10 == 0:
                with open('/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas_temp.json', 'w', encoding='utf-8') as f:
                    json.dump(formulas, f, ensure_ascii=False, indent=2)
                print(f"  Saved progress: {len(formulas)} formulas")

        except Exception as e:
            print(f"Error fetching formula {fid}: {e}")
            import traceback
            traceback.print_exc()
            continue

    browser.close()

# 保存最终结果
print(f"\n\nTotal formulas collected: {len(formulas)}")

# 统计有多少方剂与药材关联
herbs_with_formulas = set()
for f in formulas:
    herbs_with_formulas.update(f['herbs'])

print(f"Herbs with formulas: {len(herbs_with_formulas)}/{len(herbs_data)}")

# 保存方剂数据
output_path = '/Users/lijie/workspace/herbmind/hkbu_data/final_data/formulas.json'
with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(formulas, f, ensure_ascii=False, indent=2)

print(f"Formulas saved to: {output_path}")

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

print(f"Updated herbs saved to: {herbs_output_path}")

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
