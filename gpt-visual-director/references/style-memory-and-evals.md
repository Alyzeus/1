# Style Kit、经验卡与微型实验

## 设计招式卡

```yaml
id: STYLE-001
name: 超大刊头压顶
source: user_reference | generated_experiment | observed_example
observable_pattern: 标题占据顶端主要宽度，人物主体在字体前后形成层次
purpose: 快速建立视觉主导和编辑感
conditions: [短标题, 人物与背景足够简洁, 阅读顺序明确]
counterexamples: [长文案挤占主体, 主标题与人脸冲突]
execution_hint: 在上边缘预留完整字形高度；适当对比标题与主体明度
failure_modes: [错别字, 遮挡关键主体, 假杂志模板化]
evidence: [作品版本号和实际观察]
status: observed   # observed | tested | supported
confidence_notes: 单次作品观察，未做跨主题测试
```

- **observed**：从参考或一次实验归纳的候选规则，尚未验证。
- **tested**：至少在一个新任务上实施并查看前后图。
- **supported**：经过不同题材或新的任务重试，并有可复查的人类/明确证据支持；不是模型权重“学会”。
- 与已有经验矛盾时增加适用条件或负面样本，不静默覆盖旧卡。
- 如没有保存功能，只返回可复制的经验卡。不要承诺跨会话自动加载。

## 验证导演是否真的有价值

用固定 Image 工具做消融：

- A：用户 Brief → 直接生图，作为基线。
- B：同 Brief → 增加导演方案构思与审查，使用相同 Image 工具、相同生成张数/修改上限。
- C：同 B + 只检索必要的 Style Cards（真正已提供），对照经验库增益。

保持任务、画幅、参考、生成次数/预算、人工干预规则尽量相同。记录：用户盲选偏好、任务硬错误、修改次数、审美问题、实际耗时。单次实验不能用于推出模型优劣或统计稳定结论。

## 跨模型/跨聊天迁移

如未来更换 GPT 或其他工具，只提供 `design-brief`、被批准的方向、资产与版本、经过测试的风格卡，不应需要转存全部聊天。**迁移必须做一次新会话恢复测试才可声称有效**。
