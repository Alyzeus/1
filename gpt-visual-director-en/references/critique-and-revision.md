# Critique, Verdict, and Revision

## Review entry condition

Require an **actually viewable output**. If the image was not returned, did not load, or cannot be read, mark it `not_verified`. Do not infer its quality from the prompt.

## Three quality gates

| Gate | Check | Hard requirement? |
|---|---|---|
| Brief Gate | Explicit user requirements, specified subject, exact title, aspect ratio, exclusions, retained character/product features | Yes, as applicable to the task |
| Design Gate | Composition, gaze, negative space, focus, thematic coherence, reference relationships, avoidance of generic conventions | Aesthetic judgment supported by evidence |
| Craft Gate | Hands/anatomy, occlusion, text errors, edges, materials, lighting, detail, and clarity | Exact text and object structure may be hard requirements |

Prefer the labels `pass / fail / uncertain`. Do not let an overall "9/10" hide incorrect text.

Optionally use a 0–4 scale as a descriptive aid, without claiming objective precision:

- 0: Not satisfied; 1: Clear problems; 2: Partly satisfied; 3: Meets expectations; 4: Clearly better than the baseline.
- Give at least one specific, locatable observation before assigning a score.
- Never mark the overall result `pass` when a hard requirement fails.

## Revision decision protocol

Use this structure per round:

```yaml
version: v02
observed_issue: Some characters in the upper-left main title are incorrect
impact: hard_constraint
hypothesis: The image model treated type decoration as texture, corrupting letters
preserve: [character appearance, lighting, background depth, basic layout proportions]
change: [correct only the title letterforms]
action: edit_local
verify: [check the exact text, confirm other regions remain essentially unchanged]
result: not_verified
```

- When the fundamental **key-visual strategy fails**, do not keep adding lighting effects or polishing textures locally. Propose a changed direction and explain the tradeoffs.
- Reject critique when its basis is insufficient, it damages hard constraints, or it conflicts with the user's approved aesthetic goal. Record why it was rejected.
- Roll back when the next image is worse, using an earlier output or recorded direction that is still accessible/usable. **Do not claim a completed rollback when you cannot actually restore or reselect the version.**
- After two consecutive rounds without confirmable improvement, prefer `stop_and_report` or `propose_pivot`. Do not keep sampling against the same failed hypothesis by default.
- Focus on no more than two highest-impact issues per round.

## Guard against self-review bias

Three perspectives from the same GPT do not guarantee independence. Counter self-confirmation with the original brief and image evidence. If the user is willing, use a human A/B choice. Do not equate a model's blind preference with human validation or invent third-party review.

## Conditions for genuine output claims

Where the host supports version storage, preserve real image IDs/paths. In a ChatGPT conversation, retain only historical images and version descriptions that are actually accessible. Save separate local image files only when authorized and supported by the environment. Prohibit claims of editable layers, exact tracking, generated PSDs, or similar deliverables without tool evidence.
