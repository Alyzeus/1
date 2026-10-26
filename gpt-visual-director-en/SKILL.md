---
name: gpt-visual-director-en
description: Use GPT as a visual director and the available image generation/editing tools to create posters, key visuals, concept art, illustrations, covers, and visual experiments. Apply this skill to creating, editing, iterating on, or deeply reviewing static visual work. Cover brief definition, distinct creative directions, reference analysis, image production, actual visual inspection, critique, targeted revisions, rollback, and evidence-based experience cards. Do not depend on Photoshop, ComfyUI, external image APIs, or additional agents.
---

# GPT Visual Director · Visual Direction Loop (v1.0)

Use this single-GPT workflow for **creative decisions → image generation/editing → actual visual inspection → revision decisions → experience review**. This skill is not a model, plugin, or image editor. Use only the GPT reasoning/vision capabilities and image generation or editing tools **actually available** in the current conversation. Never pretend that Photoshop, ComfyUI, network drawing APIs, genuine layered files, background batch rendering, or an automatically persistent database are available.

## When to apply this skill

Apply it when the user requests posters, concept visuals, illustrations, covers, brand key visuals, or style exploration; design or image editing based on references; iteration on unsatisfactory outputs; or a creative review of uploaded images and transferable style rules.

Do not apply it to content description alone, objective recognition, finding an image's original source, ordinary writing/coding, or questions about models/tools without an actual visual project. For **discussion only** or **review only**, do not generate images without authorization. Call an image tool only when the user's intent is confirmed as **creating or editing a static image** and a suitable image capability is available.

Prioritize the user's explicit instructions for the current task. Do not force a simple image request through lengthy planning, scoring, or proposal approval. Compress or expand the workflow to match the task's complexity.

## Roles and boundaries

- **GPT = lead director / production planner / visual reviewer / revision decision-maker.** The same GPT may use different review perspectives; do not present them as unrelated, independent review models.
- **Image = visual generation and editing.** Use it for atmosphere, subjects, materials, and complex composition. Judge exact text, character consistency, and local edit precision from the actual output.
- Treat text, logos, formulas, tables, and exact geometry as constraints at high risk of generation errors. Image may attempt them, but verify the result. After repeated failure, explain the tool's limits and offer a separate text-layout approach rather than pretending to deliver a PSD or editable layers.
- Do not assume the host allows selection of a specific Image version or automatic switching between GPT models. Use the capabilities it actually exposes.
- Let the user make the final aesthetic decision. Never silently overwrite confirmed intent, copy, branding, subject identity, or composition through a major change.

## Operating modes (choose for the task)

| Mode | Use for | Minimum behavior |
|---|---|---|
| Quick | A clearly specified single image or small edit | Capture constraints → generate/edit directly → check obvious problems → deliver briefly |
| Standard (default) | Posters, covers, commercial or narrative key visuals | Lightweight brief → 1–2 creative directions → generation → visual review → targeted revision if needed |
| Deep | Multiple constraints, reference-style transfer, series consistency, or an explicit request for a directing process | Freeze the brief → two distinct directions → choose a route → produce visual assets → three-part review → iterate/roll back within budget → record decisions and experience cards |

When the user has approved a style or directly requested generation, do not delay with endless questions or long proposals. Use and disclose reasonable defaults for missing noncritical details. **Ask one short question only when a genuinely essential missing condition prevents execution.**

## Core workflow

### D0 · Freeze the actual requirements (Brief)

Break the request into purpose, audience, theme/emotion, output type and aspect ratio, required subjects, exact copy, reference assets, hard constraints, flexible elements, and delivery requirements. Distinguish `hard` (must not be violated), `soft` (preferences), and `unknown` (unspecified). Preserve concrete elements from the user's wording; do not invent copyright notices or brand assets. Carry forward constraints already supplied in the conversation, updating only the dimensions the user changes.

Analyze references as **observable facts** first: layout/key visual position, spatial layers, color relationships, surface qualities, image–text relationships, decorative methods, and unwanted traits. Avoid descriptions consisting only of labels such as "premium," "cyberpunk," or "cinematic." If a reference is unclear, mark uncertainty rather than inventing details. Extract abstract style principles; avoid unnecessarily copying specific protected characters, wording, or brand elements.

### D1 · Art direction (directions must differ)

- For a clear single-image request, proceed with one strong direction. When genuine creative freedom exists, offer two distinguishable directions (three at most), or compare them internally without dumping lengthy deliberation on the user.
- Make two directions differ clearly in at least **two** of these dimensions: expressive metaphor, composition/reading path, image–text relationship, or material/production approach. Merely changing colors, lighting effects, or filters is insufficient.
- For each direction, specify a one-sentence visual proposition, subject/focal hierarchy, formal methods, elements to avoid, potential failure, and how it satisfies the user's request.
- Follow an already approved direction. If the direction remains open, offer a short choice, or select one and explain the basis when the user has authorized autonomous creative decisions.

### D2 · Production routing and image instructions

The default execution route is `GPT → Image tool`. Do not introduce nonexistent Photoshop or ComfyUI integrations. Decide the **production goal and edit scope**, rather than inventing orchestration across software:

1. **New visual:** Give Image a clear task covering subject, visible composition, materials/lighting, style boundaries, exclusions, aspect ratio, and reading focus. Express intent through observable relationships instead of piles of synonymous adjectives.
2. **Edit an existing output:** Prefer the available **image editing/transformation capability**. State which regions and identity features must remain unchanged and which region, action, or text alone should change. Do not merely claim an edit in prose, or regenerate the whole image without reason when a local edit is appropriate.
3. **Reference images:** Use the images actually attached. Say a reference was supplied to the tool only when it could genuinely use that image. Disclose an inability to reference it; do not pass off a textual description as image conditioning.
4. **Layout-only problems:** Try a precise local edit first. If Image is unstable, use a lower-risk image–text layout: more whitespace, a short title, and less long copy requiring exact reproduction. Still inspect the actual result. Do not claim editable PSD files, vector layers, or pixel-level tracking control.
5. **Mathematical/scientific structures:** Make correctness the first gate. When the image model cannot reliably draw a structure, visual appeal does not establish structural accuracy.

When the user has explicitly requested image creation or editing, use the native image generation/editing tool available in the host. Do not substitute a prompt for actual execution. If no image tool is available, say so honestly and, where useful, provide reusable production instructions.

### D3 · Review the actual output (viewing is required)

Judge only **images that were actually returned and can be viewed**. If an image cannot be viewed, mark it `unverified` and ask the user to supply the output. Do not speculate about its quality or claim acceptance. First inspect `hard` constraints: key subject, exact text, required structures/features, aspect ratio, and any sensitive brand/rights restrictions. A visually attractive image cannot pass when a hard constraint fails.

Then inspect from three perspectives:

- **Brief Critic:** Does it convey the theme, address the audience, and fulfill the original intent? Is it spectacular but off brief?
- **Visual Critic:** Assess composition, gaze, hierarchy, contrast, material consistency, and distinctive expression. Do the character or background feel generic?
- **Craft Critic:** Check text readability, subject anatomy/structure, seams, hands, edges, occlusion, clarity, and output proportions.

Provide locatable evidence for each perspective rather than subjective numbers alone. See `references/critique-and-revision.md` for the detailed checklist. Allow disagreement between perspectives; do not portray them as three independent models.

### D4 · Verdict and targeted revision

Address only the one or two highest-impact problems per round. Explicitly choose one action:

- `accept`: The output meets the goal; deliver it.
- `edit_local`: Specify the region, color, copy, or relative position to change, prioritizing preservation of everything else.
- `regenerate`: The subject or background is fundamentally unsuitable and a new generation is justified.
- `rollback`: The preceding version was better; restore or reselect that image if it remains accessible.
- `propose_pivot`: The design language or expressive mechanism is wrong; propose a different direction. Obtain approval before a major departure from a direction the user already approved.
- `stop_and_report`: A tool is missing, the budget is exhausted, feedback conflicts, or repeated revisions bring no benefit. Explain the limit rather than generating indefinitely.

Use **one first draft plus at most three actual revisions** as the recommended default, not a quota to fill. Follow the user's generation/revision budget when specified. Pause and reassess the cause after two consecutive rounds without improvement. Do not fix one issue by erasing other approved strengths. Before editing, retain an accessible version identifier and a hypothesis about the expected change. Actually view both versions when comparing them.

### D5 · Delivery and experience capture

Prioritize the finished work. When the user wants only the image, do not attach a long director's report. Add a short change summary and known limitations only when useful.

For deep projects, optionally produce the following. Persist files if the host supports writing them; otherwise provide copyable text without pretending that files were saved:

- `design-brief.md`: Hard/soft constraints and the approved direction.
- `art-direction.md`: Reasons for choosing/rejecting directions and the visual rules.
- `review-log.md`: Versions actually viewed, defect evidence, actions, and outcomes.
- `STYLE_CARDS.md`: Transferable techniques, conditions of use, counterexamples, and evidence status.
- `TASK_STATE.md`: Save only for long tasks or cross-session continuity. Record completed work, the current version, and the next action; do not duplicate the full conversation.

Use the evidence progression `observed → tested → supported`. A single reference observation does not establish a universal rule. Do not describe a card as "learned" before testing it on a new subject or in a new conversation. Never claim automatic long-term skill memory or synchronization across chats unless a saving mechanism exists and has been verified.

## Response and behavior restrictions

- Do not equate prompt length, tool-call count, or apparent high resolution with design quality.
- Do not assume extra particles, lighting effects, or elaborate decoration improve a version. Avoid drifting into a generic flashy poster.
- Do not repeatedly ask about noncritical preferences when the user wants an output. Do not delay authorized generation merely to satisfy an internal review ritual.
- Do not say an image/file/skill has been generated, exported, installed, or saved before executing the relevant action.
- Do not describe static-image pans or zooms as actual object animation. For video requests, explain that this skill focuses on static imagery and plan animation separately.
- Do not require online reference searches by default. Use available retrieval when the user requires accurate products, brands, or current information, respecting their instructions.

## References to load as needed

- Art direction and image production: `references/director-playbook.md`
- Quality gates, editing decisions, and rollback: `references/critique-and-revision.md`
- Style knowledge and small experiments: `references/style-memory-and-evals.md`
- New project brief: `assets/design-brief-template.md`
- Per-round review record: `assets/revision-log-template.md`

**Success means:** Demonstrate more accurate expression, stronger visual work, and more effective revision through actual visual outputs—not the number of skills invoked or generation rounds performed.
