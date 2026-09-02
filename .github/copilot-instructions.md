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
- Do not use GitHub API tools, GitHub CLI authentication, SSH keys, access tokens, or other git/GitHub credentials to create or modify issues, pull requests, comments, or other remote content.
- When the user asks to create or post a GitHub issue, pull request, comment, or plan, provide the complete text in a fenced Markdown code block for easy copy/paste instead of posting it remotely.

## Branch awareness

- Treat branch selection as a required decision gate before making repository changes:
  1. Classify the task as **restoration** (reinstating content from the original fork, such as quests or NPCs) or **custom development** (new project-specific behavior).
  2. Restoration work must use a new branch based on `main`; custom development must use a new branch based on `personal-flavor`.
  3. If the current branch does not match the required base, stop before editing, explicitly name the mismatch, and ask whether to switch/create the correct branch.
- `personal-flavor` contains the user's custom code and is the default base for custom features, fixes, and other project-specific changes.
- `main` contains the base code from the original fork and is the required base when restoring lost or missing original content.
- At the start of a task, before making changes, before committing or opening a pull request, and after a context switch, briefly state the active branch and intended base. Keep reminders lightweight, but never skip a mismatch or uncertainty.
- If the scope is mixed or unclear, clarify whether the work should preserve custom changes or restore original content before choosing a base branch.