package cn.plumc.translateoverlay.config;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;
import cn.plumc.translateoverlay.TranslateOverlay;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Objects;

public class ConfigManager {
    public static final File CONFIG_DIR = new File("config");
    public static final File CONFIG_FILE = new File("config/translateoverlay.json");
    public static ConfigManager instance;

    public ConfigManager(){
        instance = this;
    }

    public JsonObject readDefaultConfig() throws IOException {
        URL url = getClass().getClassLoader().getResource("translateoverlay.json");
        JsonElement element = new JsonParser().parse(new InputStreamReader(url.openStream()));
        return element.getAsJsonObject();
    }

    public void load(){
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!CONFIG_FILE.exists()){
            try {
                CONFIG_FILE.createNewFile();
                CONFIG_FILE.setWritable(true);
                URL url = getClass().getClassLoader().getResource("translateoverlay.json");
                OutputStream out = new FileOutputStream(CONFIG_FILE);
                try (InputStream in = url.openStream()) {
                    byte[] buffer = new byte[in.available()];
                    in.read(buffer);
                    out.write(buffer);
                }
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        JsonObject defaultConfig = null;
        boolean rewriting = false;

        try {
            JsonObject jsonObject = new JsonParser().parse(new FileReader(CONFIG_FILE)).getAsJsonObject();
            Gson gson = new Gson();
            for (Field field : ConfigFile.class.getDeclaredFields()){
                Object value = gson.fromJson(jsonObject.get(field.getName()), field.getType());
                if (Objects.isNull(value)){
                    if (Objects.isNull(defaultConfig)){
                        defaultConfig = readDefaultConfig();
                    }
                    value = gson.fromJson(defaultConfig.get(field.getName()), field.getType());
                    jsonObject.add(field.getName(), gson.toJsonTree(value, field.getType()));
                    TranslateOverlay.logger.info("Add a new config entry %s to config file.".formatted(field.getName()));
                    rewriting = true;
                }
                field.set(ConfigFile.class, value);
            }
            Config.load();
            if (rewriting){
                save(jsonObject);
            }
        } catch (IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(@Nullable JsonObject json){
        try {
            CONFIG_FILE.setWritable(true);
            JsonWriter writer = new JsonWriter(new FileWriter(CONFIG_FILE));
            writer.setIndent("  ");
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (Objects.isNull(json)){
                for (Field field : ConfigFile.class.getDeclaredFields()){
                    jsonObject.add(field.getName(), gson.toJsonTree(field.get(ConfigFile.class)));
                }
            } else jsonObject = json;
            gson.toJson(jsonObject, JsonObject.class, writer);
            writer.close();
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
