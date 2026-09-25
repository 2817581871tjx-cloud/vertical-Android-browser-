# LeftTab 浏览器 (LeftTab Browser)

专为**平板横屏模式**打造的左侧栏垂直标签页 Android 浏览器，上下屏幕内容零遮挡，视野更宽广。内置完整的油猴脚本（Tampermonkey / Userscript）引擎与现代多标签页管理体系。

---

## ✨ 核心特性

- 📱 **左侧垂直标签栏**：专为平板横屏优化，解决传统浏览器上下被地址栏与底栏遮挡痛点，让网页垂直阅读区域最大化。
- 📜 **油猴脚本扩展（Userscript）**：内置脚本管理器，支持自定义 JavaScript 注入、GreasyFork 脚本运行与开关控制。
- 📑 **全功能多标签页系统**：支持一键新建、关闭、独立状态恢复与标签页快速切换。
- 🛡️ **现代隐私与安全**：支持无痕/隐身浏览模式、Cookie 自动管理及隔离机制。
- 🎨 **Material 3 响应式设计**：支持深色/浅色动态主题切换，横竖屏自动适配折叠侧栏。

---

## 🚀 安装与下载 (Releases)

在 GitHub 项目页右侧的 **[Releases](../../releases)** 页面中，直接下载最新的 `app-debug.apk` 安装到平板或安卓手机即可使用。

---

## 🛠️ 构建指南 (Build)

```bash
# 克隆仓库
git clone <repository-url>
cd LeftTab-Browser

# 使用 Gradle 构建 APK
gradle :app:assembleDebug

# 构建输出路径
app/build/outputs/apk/debug/app-debug.apk
```
