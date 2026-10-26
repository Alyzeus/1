# Director Playbook · Creative Direction and Image Production

## 1. Translate abstract wishes into visible relationships

Avoid instructions consisting only of "premium," "science fiction," "epic," "cyberpunk," or "cinematic." Specify instead:

- **Focus:** Which object leads, and why do the other elements support it?
- **Composition:** Visual center of gravity, negative space, hierarchy, foreground/background occlusion, and where the viewer begins looking.
- **Lighting:** Key-light direction, material reflections, retained shadow areas, and separation between subject and background.
- **Color:** Proportions and contrast among subject, environment, and accent colors.
- **Form:** Geometric/organic, ordered/disordered, hard/soft, dense/open.
- **Image and text:** Is the title the visual subject or an explanatory layer? Is it actually readable?
- **Avoiding generic results:** Name concrete cliches to avoid, such as purposeless particles, arbitrary accumulated glow, or a vaguely futuristic city.

When the user provides a reference, describe relationships that can actually be seen. Do not copy its specific wording, logos, or character identity. Use assets the user has rights to use, and mark an unknown license as requiring confirmation.

## 2. Two genuinely different directions (only when needed)

Use a concise concept card:

```yaml
idea_id: A
one_line: Express mathematical beauty as order emerging from noise
first_glance: A floating symmetrical structure
composition: Central subject with dark negative space on both sides
visual_grammar: Restrained colors, readable structure, high-contrast contours
text_strategy: A separate lower title area with minimal wording
image_role: Produce the materials, environment, and visualized subject
must_preserve: [symmetry, exact title]
risks: [overly didactic presentation, inaccurate generated geometry]
```

The other direction must do more than change colors. For example, replace a central single focus with an oversized masthead, an offset subject, and deliberate image–text tension, changing both the narrative and the viewing path.

## 3. Structure instructions for the Image tool

Use compact, testable natural language. Do not make the user read a long meta-prompt:

- **Output:** Aspect ratio and purpose, such as a vertical 2:3 art poster.
- **Subject:** Specific appearance, action, materials, and visual features that must not change.
- **Space:** Position, scale, depth layers, and proportion of negative space.
- **Art direction:** Compositional contrast, light, palette, and texture rather than a stack of style labels.
- **Text strategy:** Exact copy, preferably no text, or a reserved text area.
- **Avoid:** One to three clear problems responsible for the previous version's failure.

Pass reference images only when the image tool supports using them. Do not substitute a text description and claim an image was supplied. For edits, explicitly name preserved elements and the target region.

## 4. Aesthetics and cost

Choose Quick/Standard/Deep according to complexity. (The original playbook calls its fast mode "Fast"; it corresponds to Quick in SKILL.md.) Do not assume that producing many candidates is necessary for quality. Unless the user specifies an image count, begin with one baseline and use targeted edits as needed. Do not create extra calls solely to manufacture a comparison.

## 5. Static versus moving visuals

The original package draws this distinction from four "Beauty of Group Theory" explorations: detailed static artwork is not mature animation, and camera pans/zooms are not continuous changes in objects or a world. This skill targets static imagery. For a video request, separately design motion over time, subject action, storyboards, and the available model capabilities. Do not present failed static-image camera motion as successful video production.
