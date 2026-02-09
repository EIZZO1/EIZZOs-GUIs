# EIZZOs-GUIs

**Dynamic GUI System** for creating custom inventory menus with requirements, actions, and in-game editing.

---

## 🎯 Features

### 📋 Interactive Menu List
- `/eguis list` or `/eguis` - Opens a visual GUI browser
- **Left-click** any menu to open it
- **Right-click** to edit (admin permission required)
- Shows menu title, size, and rows for each menu
- Sorted alphabetically for easy navigation

### 🎨 In-Game GUI Editor
- Create, edit, and delete menus without reloading
- Visual slot editor with click-to-edit interface
- Chat-based input for names, actions, and requirements
- Live preview while editing

### ⚙️ Advanced Features
- **Requirements System:** Permission, money (Vault), tokens (EIZZOs-Tokens)
- **Actions:** Commands, messages, menu navigation, close actions
- **Item Binding:** Bind GUIs to physical items (right-click to open)
- **Placeholder Support:** PlaceholderAPI integration
- **Context Arguments:** Pass arguments when opening menus programmatically

---

## 📝 Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/eguis` | - | Open interactive menu list |
| `/eguis list` | - | Open interactive menu list |
| `/eguis help` | - | Show help message |
| `/eguis open <menu> [player] [args]` | `eguis.open.others` | Open a menu for yourself or another player |
| `/eguis edit <menu>` | `eguis.edit` | Open menu editor |
| `/eguis create <name> <rows>` | `eguis.create` | Create a new menu (1-6 rows) |
| `/eguis delete <menu>` | `eguis.delete` | Delete a menu permanently |
| `/eguis setbinding <menu>` | `eguis.admin` | Bind the item in hand to a menu |
| `/eguis removebinding <menu>` | `eguis.admin` | Remove item binding from a menu |
| `/eguis giveitem <menu> [player]` | `eguis.giveitem` | Give a bound menu item |

---

## 🔐 Permissions

| Permission | Description |
|-----------|-------------|
| `eguis.admin` | Access to all admin commands |
| `eguis.create` | Create new menus |
| `eguis.edit` | Edit existing menus |
| `eguis.delete` | Delete menus |
| `eguis.open.others` | Open menus for other players |
| `eguis.giveitem` | Give bound menu items |

---

## 🛠️ Configuration

### Main Config (`config.yml`)
```yaml
open-sound: UI_BUTTON_CLICK    # Sound when opening a menu
close-sound: BLOCK_CHEST_CLOSE # Sound when closing a menu
click-sound: UI_BUTTON_CLICK   # Sound when clicking items
fail-sound: BLOCK_ANVIL_LAND   # Sound when requirements fail
```

### Menu Files (`menus/<name>.yml`)
Menus are stored as individual YAML files in the `menus/` directory.

Example menu structure:
```yaml
title: "<gradient:#FFD700:#FFA500><bold>Example Menu"
size: 27  # Must be multiple of 9 (9, 18, 27, 36, 45, 54)

items:
  example_item:
    slot: 13
    material: DIAMOND
    name: "<aqua><bold>Click Me!"
    lore:
      - "<gray>This is an example item"
      - "<yellow>Click to execute actions"

    # View Requirements (must meet to see item)
    requirements:
      permission: "example.view"

    # Click Requirements (must meet to click)
    click-requirements:
      permission: "example.use"
      vault-cost: 100.0
      token-cost: 50
      token-id: "gems"

    # Actions (executed on click)
    actions:
      - "[message] <green>You clicked the item!"
      - "[player] warp spawn"
      - "[console] give %player_name% diamond 1"
      - "[close]"

  back_button:
    slots: [18, 19, 20, 21, 22, 23, 24, 25, 26]  # Fill multiple slots
    material: GRAY_STAINED_GLASS_PANE
    name: " "
```

---

## 📦 Item Binding

Bind GUIs to physical items that open menus on right-click:

1. Hold the item you want to bind
2. Run `/eguis setbinding <menu>`
3. The item is now tagged and will open the menu
4. Give tagged items with `/eguis giveitem <menu> [player]`

The binding configuration is stored in the menu file:
```yaml
item-binding:
  material: COMPASS
  name: "<gold>Menu Opener"
  lore:
    - "<gray>Right-click to open menu"
```

---

## 🎬 Actions

Available action types in menus:

| Action | Description | Example |
|--------|-------------|---------|
| `[close]` | Close the inventory | `[close]` |
| `[message] <text>` | Send message to player | `[message] <green>Welcome!` |
| `[player] <command>` | Execute as player | `[player] spawn` |
| `[console] <command>` | Execute as console | `[console] give %player_name% diamond` |
| `[op] <command>` | Execute as OP temporarily | `[op] gamemode creative` |
| `[opengui] <menu>` | Open another menu | `[opengui] shop` |

**Note:** All actions support PlaceholderAPI placeholders (e.g., `%player_name%`, `%player_balance%`)

---

## 🔒 Requirements

### View Requirements
Items are hidden if requirements aren't met:
```yaml
requirements:
  permission: "vip.access"
```

### Click Requirements
Player must meet these to click (with cost deduction):
```yaml
click-requirements:
  permission: "shop.use"
  vault-cost: 100.0      # Requires Vault
  token-cost: 50         # Requires EIZZOs-Tokens
  token-id: "gems"       # Token type to deduct
```

---

## 🔖 Placeholders

### Built-in Player Placeholders
EIZZOs-GUIs includes built-in support for common player placeholders (no external plugins required):

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%player_name%` | Player's username | `Steve` |
| `%player_displayname%` | Player's display name | `[VIP] Steve` |
| `%player_uuid%` | Player's unique ID | `069a79f4-...` |
| `%player_ping%` | Player's latency in ms | `45` |
| `%player_world%` | Current world name | `world` |
| `%player_x%` | X coordinate | `123` |
| `%player_y%` | Y coordinate | `64` |
| `%player_z%` | Z coordinate | `-456` |
| `%player_health%` | Current health | `20` |
| `%player_max_health%` | Maximum health | `20` |
| `%player_level%` | Experience level | `30` |
| `%player_exp%` | Experience percentage | `75` |
| `%player_food%` | Food level | `20` |
| `%player_gamemode%` | Game mode | `SURVIVAL` |
| `%player_ip%` | Player's IP address | `127.0.0.1` |

### PlaceholderAPI Integration
For extended placeholder support (economy, statistics, other plugins), install **PlaceholderAPI** expansions:

**In-game installation (Recommended):**
```
/papi ecloud download Player
/papi ecloud download Vault
/papi reload
```

**Common PlaceholderAPI placeholders:**
- `%vault_eco_balance%` - Player's money (requires Vault expansion)
- `%statistic_deaths%` - Death count (requires Player expansion)
- `%eizzotokens_balance_<id>%` - Token balance (EIZZOs-Tokens, built-in)

**Note:** Built-in placeholders work immediately. PlaceholderAPI placeholders require the corresponding expansions to be installed.

---

## 🎨 Supported Materials

### Player Heads
```yaml
material: PLAYER_HEAD
# Or use special formats:
material: "[PLAYER] Notch"           # Player skin
material: "[BASE64] <base64_texture>" # Custom texture
```

### Custom Model Data & Enchantments
```yaml
material: DIAMOND_SWORD
model-data: 1234      # CustomModelData for resource packs
enchanted: true       # Adds enchantment glow effect
```

---

## 🧰 Development

### Dependencies
- Paper API 1.21.1
- PlaceholderAPI (optional, soft-depend)
- Vault (optional, for economy requirements)
- EIZZOs-Tokens (optional, for token requirements)

### Building
```bash
cd plugins/source/EIZZOs-GUIs
mvn clean package
```

### Deployment
```bash
# Copy to central plugins
cp target/EIZZOs-GUIs-*.jar ../../

# Copy to lobby server
cp target/EIZZOs-GUIs-*.jar ../../../servers/lobby/plugins/

# Restart lobby
cd ../../..
./scripts/stop-lobby.sh
./scripts/start-lobby.sh
```

---

## 📚 Examples

### Simple Shop Menu
```yaml
title: "<gold><bold>Shop"
size: 27
items:
  apple:
    slot: 11
    material: APPLE
    name: "<red>Apple"
    lore:
      - "<gray>Cost: <yellow>$10"
    click-requirements:
      vault-cost: 10.0
    actions:
      - "[console] give %player_name% apple 1"
      - "[message] <green>Purchased 1 apple!"
```

### Admin Menu
```yaml
title: "<red><bold>Admin Panel"
size: 9
items:
  gamemode:
    slot: 0
    material: COMMAND_BLOCK
    name: "<aqua>Gamemode"
    requirements:
      permission: "admin.use"
    actions:
      - "[op] gamemode creative"
      - "[close]"
```

### Multi-Page Menu
```yaml
# page1.yml
items:
  next_page:
    slot: 26
    material: ARROW
    name: "<green>Next Page"
    actions:
      - "[opengui] page2"

# page2.yml
items:
  previous_page:
    slot: 18
    material: ARROW
    name: "<red>Previous Page"
    actions:
      - "[opengui] page1"
```

---

## 🔧 Troubleshooting

### Menu not showing?
- Check the menu file exists in `plugins/EIZZOs-GUIs/menus/`
- Verify YAML syntax (use a YAML validator)
- Check console for errors on plugin load

### Items not appearing?
- Verify `requirements` permissions
- Check if material name is valid
- Ensure slot numbers are within the size range

### Actions not working?
- Test PlaceholderAPI placeholders with `/papi parse me <placeholder>`
- Check console for command execution errors
- Verify permissions for `[player]` commands

### Item binding not working?
- Ensure the item has the persistent data tag (set with `/eguis setbinding`)
- Check `ItemInteractionListener` is registered
- Verify the menu exists

---

## 📄 License

Part of the EIZZOs plugin suite for Minecraft multiplayer networks.

---

**Version:** 1.0-SNAPSHOT
**Minecraft:** 1.21.1 (Paper)
**Author:** EIZZO Development Team
