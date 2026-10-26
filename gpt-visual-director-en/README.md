# GPT Visual Director · Usage and Installation

[Chinese original](../gpt-visual-director/README.md) · English edition

**Purpose:** A workflow combining GPT art direction, Image generation, actual visual inspection, and targeted revision. It does not require Photoshop, ComfyUI, a third-party API key, or an external server.

This is a complete English edition of v1.0.0, including the core instructions, three reference documents, both templates, and all 12 manual evaluation cases. Its identifier is `gpt-visual-director-en` so it can coexist with the Chinese `gpt-visual-director` edition. The directing, critique, revision, rollback, and evidence-based experience mechanisms are preserved.

## Files

- `SKILL.md`: Core instructions governing activation and execution.
- `references/`: Art-direction methods, critique/verdict rules, and experience-card guidance to load only as needed.
- `assets/`: Optional templates and the existing skill icon.
- `agents/openai.yaml`: English display metadata and a starter prompt.
- `tests/`: Twelve manual regression cases for checking actual viewing and editing behavior.

## ChatGPT installation (when Skills is available)

Use the Skills creation/import entry offered by your account or workspace. The original package describes **Plugins → Skills → Create → Upload from your computer**; labels and availability may vary by client.

1. Select the English edition's folder as the skill source, or package that folder as a ZIP if your importer requires a ZIP.
2. Import it through the supported Skills upload flow.
3. Inspect the trigger description, permissions, and scan result, then confirm installation.
4. Check that **GPT Visual Director (English)** appears in the installed list.
5. In a new conversation, select the installed skill through the client's skill selector and test a task such as: "Use GPT Visual Director (English) to design a poster."

The original October 2026 installation notes describe availability differences across account/workspace types. Treat those notes as package documentation rather than a guarantee for every current client. Verify the actual account and client interface. Downloading this repository or a ZIP **does not install the skill**; appearance in the installed list also does not by itself prove that a particular conversation loaded it.

## If there is no Skills entry

Upload `SKILL.md` to the conversation where the work will happen and send:

> Read the attached SKILL.md and follow the GPT Visual Director workflow for the upcoming poster or illustration task. Use only the GPT and Image capabilities genuinely available in this conversation. Do not pretend to have Photoshop or an external rendering backend.

For complex tasks, also supply the relevant `references` files. This fallback depends on the current conversation's available context; it is not permanent account-level installation.

## Codex use

Place this edition in the skills directory supported by your Codex client, for example:

```text
~/.codex/skills/gpt-visual-director-en/SKILL.md
```

Use `$gpt-visual-director-en` where the client supports that invocation syntax. Installing a skill does not add image generation/editing tools to a host that lacks them. Verify the actual tool capabilities and follow the client's current installation guidance.

## Suggested trial tasks

- A clearly specified poster: verify direct generation without excessive planning.
- A "change only the title/color" edit: verify a real image-editing call and preservation of the subject.
- Two creative directions: verify differences in composition/expression rather than color alone.
- A poster with incorrect text: verify refusal to falsely pass the output and honest reporting of tool limits.

## Important boundaries

This skill does not grant Photoshop access, precise vector typography, editable PSD output, a permanent database, unlimited generation quota, background tasks, or control over a particular GPT/Image model version. All execution capabilities depend on the host.

Experience cards are evidence records, not automatic model training or guaranteed cross-session memory. The manual cases in `tests/` describe expected behavior; they are not a claim that those tests have already passed.

**Version:** 1.0.0 · English edition · 2026-10-26
