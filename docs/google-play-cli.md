# Google Play 命令行发布配置指南

本文档说明如何通过 Google Play Developer API + gradle-play-publisher（GPP），用命令行完成本草记（`hua.lee.herbmind`）的商店资料填报与应用发布。

## 原理

- 官方 API：**Google Play Developer API v3 (androidpublisher)**，免费，配额 3000 次/分钟。
- 本项目用 **gradle-play-publisher**（Gradle 插件 `com.github.triplet.play`）作为封装层，商店资料就是仓库里的普通文件：
- 本机也已安装 **fastlane 2.239.0**（rbenv + Ruby 3.3.12）作为备选封装层；如需用 fastlane（如更复杂的 CI 发布编排），其 `metadata` 目录格式与 GPP 不同，需另行生成。
  - `androidApp/src/main/play/listings/<lang>/` —— 商店标题、简介（`title.txt` / `short-description.txt` / `full-description.txt`）
  - `androidApp/src/main/play/listings/<lang>/graphics/` —— 图标（512×512）、置顶大图（1024×500）、手机截图
  - `androidApp/src/main/play/release-notes/<track>/<lang>/default.txt` —— 版本更新说明
  - 发布相关配置在 `androidApp/build.gradle.kts` 的 `play { }` 块

改商店资料 = 改这些文本/图片后跑一条 Gradle 命令；发布 = 一条 `publishBundle` 完成构建 + 上传 + 提交。

## 一次性配置（需要浏览器登录 Google 账号，约 20 分钟）

按顺序执行，只有这些步骤必须人工：

### 1. 确认应用已在 Play Console 创建

API 不能创建新应用。若本草记还没在 Play Console 创建：

1. <https://play.google.com/console> → 创建应用（包名 `hua.lee.herbmind`），完成目标受众、内容分级问卷、隐私政策链接等一次性声明。
2. **第一个 AAB 必须手动上传一次**（用 `./gradlew :androidApp:bundleRelease` 的产物，在 Console → 创建版本）。此后所有更新都可走 API。
   - ✅ 本草记已上传过 versionCode 1 的包，此步骤已完成。

如果应用已存在且有任意一个已上传版本，本节跳过。

### 2. 启用 API 并创建服务账号

1. Play Console → **设置 → API 权限（API access）** → 关联 Google Cloud 项目（没有就新建一个）。
2. 在同一页面启用 **Google Play Developer API**。
3. Google Cloud Console → **IAM 和管理 → 服务账号** → 创建服务账号（名字随意，如 `herbmind-publisher`）。
4. 进入该服务账号 → **密钥** → 添加密钥 → 创建并下载 **JSON 私钥**。

### 3. 把服务账号加入 Play Console 并授权

1. Play Console → **用户和权限（Users and permissions）** → 邀请新用户，填入服务账号邮箱（`xxx@xxx.iam.gserviceaccount.com`）。
2. 账号权限选择**按应用授权**：只选 `hua.lee.herbmind`，勾选：
   - 商店资料相关：**管理商店资料和定价**（"Manage store presence and pricing"）
   - 发布相关：**将应用发布到测试轨道**、**将应用发布到生产轨道**（Releases 类）
3. 保存。

### 4. 放置密钥到本仓库

```bash
cp ~/Downloads/xxxx.json play-service-account.json
chmod 600 play-service-account.json
```

该文件已在 `.gitignore` 中（`/play-service-account.json`），不会被提交。

### 5. 验证连通性

```bash
# 把 Console 上现有的商店资料反拉到本地（也是初始化校验，成功即说明 API 权限 OK）
./gradlew :androidApp:bootstrapListing
```

如果 Console 上还是空资料，跳过 bootstrap，直接看下一步用 `publishListing` 把本地资料推上去。

## 日常命令

```bash
# 只上传/更新商店资料（标题、简介、图标、截图、更新说明），不传安装包
./gradlew :androidApp:publishListing

# 构建 release AAB 并上传到封闭式测试 Alpha 轨道（自动提交，当前配置的默认轨道）
./gradlew :androidApp:publishBundle

# Alpha 验证没问题后，提升（promote）到生产环境，不重新上传包
./gradlew :androidApp:promoteArtifact --from-track alpha --to-track production
```

其他常用任务：`./gradlew :androidApp:tasks --group publishing` 查看全部发布任务。

## 发布前检查清单

- [ ] `play-service-account.json` 已就位且未被 git 跟踪（`git check-ignore play-service-account.json` 应有输出）
- [ ] **release 签名已配置**：本机通过 `~/.gradle/gradle.properties` 或环境变量注入 `HERBMIND_STORE_FILE` / `HERBMIND_STORE_PASSWORD` / `HERBMIND_KEY_ALIAS` / `HERBMIND_KEY_PASSWORD`；CI 已通过 Secrets 注入（勿入库）
- [ ] AdMob：Manifest 中的 APP ID（`ca-app-pub-8623430918768964~5684788741`）与 `AdMobAdapter.kt` 中 5 个正式广告位 ID 均已生效
- [ ] 广告声明：Play Console → 应用内容 → **广告** 已勾选"含广告"（本应用集成 AdMob）
- [ ] 数据安全表单（Data safety）：按 `docs/privacy-policy.md` 内容在 Console 填写
- [ ] 隐私政策 URL 可公开访问（Console 要求填写链接）
- [ ] 商店文案确认：`play/listings/zh-CN/` 与 `en-US/` 下的标题（≤30 字符）/简介（≤80 字符）/完整描述（≤4000 字符）
- [ ] 截图确认：`play/listings/*/graphics/phone-screenshots/`（由 `app_store_res/` 衬垫生成 1440×2880，符合 Play 2:1 比例要求；UI 改版后需重新导出）

## 常见问题

- **报 unauthorized / 权限不足**：服务账号未加入 Play Console 用户，或应用级权限未勾选 Store presence / Releases。
- **报 "Package not found"**：包名不符或应用尚未在 Console 创建（API 不能创建应用）。
- **截图上传被拒**：Play 要求截图任一边 320–3840px 且高宽比 ≤ 2:1；本仓库截图已衬垫为 1440×2880，符合要求。
- **publishBundle 报签名相关错误**：见检查清单中的 release 签名配置。
- **GPP 与 AGP 版本兼容**：本项目用 AGP 8.2.0 + GPP 3.11.0；升级 AGP 时注意查看 [Triple-T/gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher) 的兼容性说明（该插件处于维护模式）。

## 参考

- <https://developers.google.com/android-publisher>
- <https://github.com/Triple-T/gradle-play-publisher>
