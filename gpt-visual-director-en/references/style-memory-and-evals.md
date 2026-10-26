# Style Kit, Experience Cards, and Small Experiments

## Design technique card

```yaml
id: STYLE-001
name: Oversized masthead at the top
source: user_reference | generated_experiment | observed_example
observable_pattern: A title spans most of the top width while the character overlaps its front/back layers
purpose: Establish immediate visual dominance and an editorial feel
conditions: [short title, sufficiently simple character and background, clear reading order]
counterexamples: [long copy crowds out the subject, main title conflicts with the face]
execution_hint: Reserve the full letterform height near the top edge; separate title and subject through value contrast
failure_modes: [incorrect text, occlusion of a key subject, generic faux-magazine styling]
evidence: [work version identifiers and actual observations]
status: observed   # observed | tested | supported
confidence_notes: Observed in one work; not tested across subjects
```

- **observed:** A candidate rule inferred from a reference or one experiment; not yet validated.
- **tested:** Applied to at least one new task, with the before/after images actually viewed.
- **supported:** Retried across different subjects or new tasks and supported by reviewable human feedback or explicit evidence. This does not mean the model's weights have "learned" it.
- When a result conflicts with an existing card, add conditions or counterexamples rather than silently overwriting the old card.
- If saving is unavailable, return a copyable card. Do not promise automatic loading across conversations.

## Test whether direction adds value

Run an ablation using a fixed Image tool:

- **A:** User brief → direct generation, as the baseline.
- **B:** Same brief → add art-direction planning and review, with the same Image tool and the same generation/revision limit.
- **C:** Same as B, plus retrieval of only the necessary Style Cards that have actually been supplied, to measure the experience library's contribution.

Keep the task, aspect ratio, references, generation count/budget, and human-intervention rules as similar as possible. Record the user's blind preference, hard task errors, revision count, aesthetic problems, and actual elapsed time. Do not infer model superiority or statistical stability from a single experiment.

## Transfer across models or conversations

When changing GPT models or tools, provide only the `design-brief`, approved direction, assets and versions, and tested style cards. It should not require exporting the entire conversation. **Claim successful transfer only after a new-conversation recovery test.**
