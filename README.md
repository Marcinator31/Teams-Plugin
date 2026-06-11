# TeamForge

A comprehensive team/guild plugin for **PaperMC 1.21.11** with many GUIs, extensive configuration and optional Vault & PlaceholderAPI integration.

## Features

- **Teams**: create, rename, change tag & color, set description & icon
- **Roles**: Owner, Officer, Member – with configurable officer permissions
- **Invitations** with expiry, open teams (join without invite)
- **Member management**: invite, kick, promote, demote, transfer ownership
- **Team home** with warmup/cooldown, cancel on move/damage, disabled worlds
- **Alliances** between teams including a request system and protection from ally damage
- **Team bank** (requires Vault) with deposit/withdraw and a minimum role to withdraw
- **Friendly fire** toggleable per team, protection against splash/lingering potions
- **Chat channels**: team chat, ally chat, admin spy, optional public chat decoration
- **Statistics**: kills/deaths, leaderboards (`/team top`)
- **Nametags** with a colored tag via the scoreboard
- **~13 GUIs** for almost every action (main menu, members, settings, color, icon, browser, allies, bank, invitations, …)
- All text in `messages.yml` (English, MiniMessage format) is customizable

## Commands

Main command: `/team` (aliases: `/t`, `/teams`, `/tf`)

| Command | Description |
|---------|-------------|
| `/team` | Opens the menu |
| `/team create <name> [tag]` | Create a team |
| `/team disband confirm` | Disband the team |
| `/team invite <player>` | Invite a player |
| `/team uninvite <player>` | Revoke an invitation |
| `/team join <name>` | Join a team |
| `/team deny <name>` | Decline an invitation |
| `/team leave` | Leave the team |
| `/team kick <player>` | Remove a member |
| `/team promote / demote <player>` | Change rank |
| `/team transfer <player>` | Transfer ownership |
| `/team sethome / home / delhome` | Team home |
| `/team ally <team>` / `/team unally <team>` | Alliances |
| `/team rename / tag / desc / color` | Customize the team |
| `/team pvp` / `/team open` | Toggle settings |
| `/team bank [deposit\|withdraw] <amount>` | Team bank |
| `/team chat` / `/team allychat` | Chat channels |
| `/team info [team]` / `/team list` / `/team top [members\|kills\|bank]` | Info |
| `/team invites` | Pending invitations |

Admin: `/teamadmin` (alias `/ta`) – `reload`, `disband <team>`, `forcejoin <player> <team>`, `forcekick <player>`, `spy`.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `teams.use` | everyone | Basic usage |
| `teams.create` | everyone | Create teams |
| `teams.admin` | OP | Admin commands |
| `teams.chat.spy` | OP | Read team chats |
| `teams.limit.<n>` | – | Raises the member limit (e.g. `teams.limit.24`) |

## Configuration

All values live in `config.yml` (commented in English): name/tag rules, member limits, friendly-fire behavior, home settings, alliances, bank, action costs, chat options, nametags and officer permissions. Text is customized in `messages.yml`.

> Note: If you use a chat plugin (e.g. with ranks), set `chat.decorate-public-chat` to `false` and use the PlaceholderAPI placeholders instead.

## Build via GitHub Actions

The project is **not built locally** but through GitHub Actions:

1. Create a repository on GitHub and push the contents of this folder.
2. Under **Actions** the `Build` workflow runs automatically on every push.
3. The finished `.jar` is then available as the artifact **TeamForge** for download.
4. If you push a tag like `v1.0.0`, a **release** with the `.jar` is created automatically as well.

Alternatively, locally with a JDK 21 installed:

```bash
gradle build
# Result: build/libs/TeamForge-1.0.0.jar
```

Or simply open the project in IntelliJ IDEA (Gradle is detected automatically).

## Dependencies

- **Paper 1.21.11**, Java 21
- **Vault** (optional, for the team bank and costs)
- **PlaceholderAPI** (optional, provides `%teamforge_...%` placeholders)

Both are *soft depends* – the plugin runs without them, in which case the respective features are disabled.

## PlaceholderAPI

`%teamforge_<name>%`, including: `has_team`, `name`, `tag`, `tag_colored`, `color`, `role`, `members`, `members_max`, `online`, `owner`, `bank`, `kills`, `deaths`, `friendlyfire`, `description`.

## License

MIT – see `LICENSE`.
