# Repository instructions

## Shared planning

- The user is generally open to proactive suggestions for plans, improvements, risks, and follow-up work at any stage; offer relevant suggestions without unnecessarily blocking the current task.
- Store approved, active implementation plans in `docs/plans/`.
- Use one Markdown file per plan with a descriptive kebab-case filename.
- At the start of a task, check `docs/plans/` for relevant active plans and continue them when applicable.
- Keep the plan synchronized with implementation progress, decisions, validation, and completion status.
- Do not create planning files for trivial one-step changes.
- Treat session-local plan files as temporary working state; the committed files in `docs/plans/` are the shared source of truth.
- When a task appears multi-step, cross-cutting, or likely to benefit from coordination, offer to create or update a shared plan before making repository changes.
- Do not create or modify a shared plan without the user's approval, unless the user has already explicitly requested planning or implementation work for that task.
- Mark completed plans with a `Status: Complete` heading and retain them for historical reference unless the user asks for cleanup.