# GPT Visual Director · 使用与安装

**定位**：可装载的“GPT 导演 + Image 生图 + GPT 审图与定向返工”工作流。无需 Photoshop、ComfyUI、第三方 API Key 或外部服务器。

## 文件说明

- `SKILL.md`：核心指令，直接决定技能触发条件与执行流程。
- `references/`：只在对应任务需要时读取的导演方法、评审裁决与经验卡规则。
- `assets/`：模板，视任务选择使用，不强制每次输出文件。
- `tests/`：12 个手动回归测试，帮助检查是否执行了真看图/真编辑。

## ChatGPT 安装（如果账号/工作区有 Skills）

1. 侧栏 **Plugins → Skills**。
2. 选择 **Create → Upload from your computer**。
3. 上传 `gpt-visual-director.zip`，或将解压后的单个技能目录按编辑器支持的方式添加。
4. 检查触发描述、权限和扫描结果，确认安装。
5. 以“使用 gpt-visual-director 为我设计一张海报…”进行测试。

OpenAI 官方文档截至 2026-10：ChatGPT 的直接 Skills 创建/安装主要面向符合条件的 Business、Enterprise、Healthcare、Edu 工作区，个人 Plus 不一定有 Skills 入口。下载 ZIP **不等于自动安装**。

## 无 Skills 入口时

将 `SKILL.md` 上传到需要执行的 ChatGPT 对话，发消息：

> 请阅读附件 SKILL.md，并按照 GPT Visual Director 工作流执行接下来的海报/插画任务。仅使用此对话确实可用的 GPT 与 Image 能力，不假装接入 Photoshop 或外部后端。

较复杂的任务也可补传相应 `references` 文件。此方法仅对当前会话的可用上下文有效，不是账号级永久安装。

## Codex 使用

如果你希望在 Codex 使用相同流程，可放入你的 Codex Skills 目录，例如：

```
~/.codex/skills/gpt-visual-director/SKILL.md
```

**注意**：Codex 宿主若没有 Image 生成/编辑工具，技能不会凭空增加此能力。即使已安装，也须确保对应图片工具真实可用。安装路径和管理方式以当前 Codex 客户端文档为准。

## 推荐试运行

- 一张明确要求的海报，检查能否直接生成，不被繁琐流程拖慢。
- 一次“只改标题/颜色”的编辑，检查是否真的调用图片编辑并保留主体。
- 一次两方案风格探索，检查差异是否是构图/表达方式层面的，而非只换配色。
- 一次错字海报，检查是否能拒绝假通过并诚实说明工具限制。

## 重要边界

本 Skill 不能让 ChatGPT 自动获得 Photoshop、精确矢量文字、可编辑 PSD、永久数据库、无限生成配额、后台自动任务，也不能强制切换 GPT 或 Image 具体型号。所有能力取决于当前宿主真正提供的工具。

**版本**：1.0.0｜2026-10-26
