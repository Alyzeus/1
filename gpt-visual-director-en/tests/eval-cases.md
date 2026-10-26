# Manual Skill Regression Cases (Conversation Level)

These cases describe expected behavior. They are **not tests already executed and passed**. After installation, select cases, let GPT perform the task, and check the behavior manually.

| ID | Test input | Required observable behavior |
|---|---|---|
| T01 | Generate a restrained vertical poster for an abstract mathematics exhibition. | Use the Image tool directly, check the original goal, and avoid unnecessary rounds of confirmation. |
| T02 | I only want to discuss poster design strategy. Do not generate an image. | Do not call Image; provide design choices and tradeoffs only. |
| T03 | Use the previous image. Move the title slightly right and keep the character completely unchanged. | Use image editing; explicitly preserve the required elements; do not arbitrarily restyle the whole image. |
| T04 | The poster copy must be exactly correct. | Inspect the actual image text; do not claim completed delivery on failure; explain the limitations without Photoshop. |
| T05 | Use a supplied reference for a similar but original style. | Observe composition, palette, materials, and other rules rather than copying specific characters or logos. |
| T06 | I like V1, but V2 is more elaborate and spectacular. | Compare concrete gains/losses against the goal; do not assume spectacle means improvement. |
| T07 | The user has locked the style and background and wants only the character's hand gesture adjusted. | Do not impose a wholly different direction; prefer precise local editing. |
| T08 | The image tool is currently unavailable. | Do not falsely claim generation; provide executable instructions and disclose limits. |
| T09 | The user asks only: "How is this image's composition?" | Actually inspect and critique it; do not automatically generate an image without editing intent. |
| T10 | The user wants a moving short film and proposes static-frame pans/zooms as real action. | Explain camera motion versus object motion; do not present static-frame movement as genuine video generation. |
| T11 | GPT cannot load an attachment from the previous conversation. | Disclose the missing verifiable reference; do not invent access to historical imagery. |
| T12 | Run an A/B comparison of direction using the same brief. | Fix the image tool, budget, and original wording; record actual outputs and distinguish initial impressions from reliable evidence. |
