package cn.plumc.translateoverlay.translate.translator;

import net.minecraft.client.resource.language.I18n;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.HandlerMethod;
import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.utils.CacheUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public abstract class Translator {
    public static final int MAX_TRY = 3;
    public static final int MAX_CACHE = 100;

    private final CacheUtil.IntCounter invokeCounter = new CacheUtil.IntCounter();
    private final ConcurrentHashMap<String, Future<String>> cache = new ConcurrentHashMap<>(){};
    private final ConcurrentHashMap<String, CacheUtil.IntCounter> cacheCounter = new ConcurrentHashMap<>(){};
    public void clearCache(){
        cache.clear();
    };
    public String translate(String message){
        return translate(message, Config.getLangFrom(), Config.getLangTo());
    };
    public String translate(String message, Language langFrom, Language langTo){
        invokeCounter.add();
        Future<String> future= cache.get(message);

        if (!cacheCounter.containsKey(message)) cacheCounter.put(message, new CacheUtil.IntCounter(1));
        else cacheCounter.get(message).add();
        if (invokeCounter.get() >= 10){
            invokeCounter.set(0);
            if (cache.size() > MAX_CACHE){
                Set<Map.Entry<String, CacheUtil.IntCounter>> entries = cacheCounter.entrySet();
                List<String> sortedCache = entries.stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList();
                for (int i=0; i<=cache.size()-MAX_CACHE; i++){
                    cacheCounter.remove(sortedCache.get(i));
                }
            }
        }

        String result;
        if( future ==null ){
            String fromLang = (LANGUAGE_MAPPING.containsKey(langFrom) ? LANGUAGE_MAPPING.get(langFrom) : langFrom.code);
            String toLang = (LANGUAGE_MAPPING.containsKey(langTo) ? LANGUAGE_MAPPING.get(langTo) : langTo.code);
            Callable<String> callable = () -> {
                String translated;
                int counter = 0;
                while (counter <= MAX_TRY) {
                    counter++;
                    translated = translate(message, fromLang, toLang);
                    if (translated!=null) return translated;
                }
                return I18n.translate("message.translateoverlay.translator.error");
            };
            FutureTask<String> futureTask= new FutureTask<>(callable);
            future = futureTask;
            cache.put(message, futureTask);
            futureTask.run();
        }
        try {
            result= future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    };
    abstract public String translate(String message, String langFrom, String langTo);
    abstract public String getTranslatorName();
    public HandlerMethod getHandlerMethod(){
        return HandlerMethod.REJOIN;
    };
    public static Map<Language, String> LANGUAGE_MAPPING = new HashMap<>();

}

