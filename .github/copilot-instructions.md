# Repository instructions

## Shared planning

- The user is generally open to proactive suggestions for plans, improvements, risks, and follow-up work at any stage; offer relevant suggestions without unnecessarily blocking the current task.
- Use a GitHub Issue as the canonical source for an active, multi-step implementation plan so the plan remains available across branches and chats.
- When beginning work, search the repository's open issues for a relevant plan if the user does not provide an issue number or link. If there are multiple plausible matches, ask the user to identify the intended issue.
- Reference the issue number in branch names, pull requests, and progress updates whenever practical.
- Keep the issue synchronized with implementation progress, decisions, validation, and completion status.
- Use `docs/plans/` only for committed design or historical documents that belong in the repository. Do not treat uncommitted or branch-only planning files as the shared source of truth.
- Use one Markdown file per document with a descriptive kebab-case filename.
- Do not create planning files for trivial one-step changes.
- Treat session-local plans as temporary working state.
- When a task appears multi-step, cross-cutting, or likely to benefit from coordination, offer to create or update a GitHub Issue before making repository changes.
- Do not create or modify a GitHub Issue or planning document without the user's approval, unless the user has already explicitly requested planning or implementation work for that task.
- Mark completed planning documents with a `Status: Complete` heading and retain them for historical reference unless the user asks for cleanup.

## Branch awareness

- Occasionally check the current Git branch and briefly remind the user which branch is active, especially at the start of a task, before making changes, before committing or opening a pull request, and after a context switch.
- Keep reminders lightweight and avoid repeating them on every turn; call out a mismatch or uncertainty clearly before proceeding.
- `personal-flavor` contains the user's custom code and is the default base for custom features, fixes, and other project-specific changes.
- `main` contains the base code from the original fork and is the appropriate base when restoring lost or missing original content, such as quests or NPCs.
- Before creating a branch, classify the work as restoration or custom development and branch from `main` or `personal-flavor` accordingly; if the scope is mixed or unclear, clarify the intended base first.