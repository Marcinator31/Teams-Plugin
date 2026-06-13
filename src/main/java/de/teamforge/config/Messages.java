package de.teamforge.config;

import de.teamforge.TeamForgePlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Access to messages.yml (MiniMessage format) with <prefix> support.
 */
public class Messages {

    private final TeamForgePlugin plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private YamlConfiguration config;
    private TagResolver prefixResolver;

    public Messages(TeamForgePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        // Load defaults from the jar in case keys are missing in the file
        InputStream stream = plugin.getResource("messages.yml");
        if (stream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }

        String prefixRaw = config.getString("prefix", "<gradient:#3ED98C:#27A8E0><b>TeamForge</b></gradient> <dark_gray>»</dark_gray> ");
        Component prefix = mini.deserialize(prefixRaw);
        prefixResolver = TagResolver.resolver(Placeholder.component("prefix", prefix));
    }

    /** Roher MiniMessage-String eines Keys (ohne Parsing). */
    public String raw(String key) {
        String value = config.getString(key);
        if (value == null) {
            plugin.getLogger().warning("Fehlender Message-Key: " + key);
            return "<red>" + key + "</red>";
        }
        return value;
    }

    /** Parsed message with prefix resolver and optional placeholders. */
    public Component get(String key, TagResolver... resolvers) {
        return mini.deserialize(raw(key), combine(resolvers));
    }

    /** List of components (e.g. for lore or help pages). */
    public List<Component> getList(String key, TagResolver... resolvers) {
        List<String> lines = config.getStringList(key);
        List<Component> result = new ArrayList<>();
        TagResolver combined = combine(resolvers);
        for (String line : lines) {
            result.add(mini.deserialize(line, combined));
        }
        return result;
    }

    /** Send a message directly to a recipient. Empty strings are skipped. */
    public void send(Audience audience, String key, TagResolver... resolvers) {
        String value = raw(key);
        if (value.isEmpty()) {
            return;
        }
        audience.sendMessage(mini.deserialize(value, combine(resolvers)));
    }

    /** Raw MiniMessage string for on/off (unparsed). */
    public String onOff(boolean state) {
        return raw(state ? "word.on" : "word.off");
    }

    /** Parsed component for on/off (colors rendered). */
    public Component onOffComponent(boolean state) {
        return get(state ? "word.on" : "word.off");
    }

    /** Parsed component for any word.* key (colors rendered). */
    public Component word(String key) {
        return get("word." + key);
    }

    private TagResolver combine(TagResolver... resolvers) {
        TagResolver[] all = new TagResolver[resolvers.length + 1];
        all[0] = prefixResolver;
        System.arraycopy(resolvers, 0, all, 1, resolvers.length);
        return TagResolver.resolver(all);
    }
}
