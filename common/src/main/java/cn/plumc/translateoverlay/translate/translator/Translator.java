package cn.plumc.translateoverlay.translate.translator;

import net.minecraft.client.resource.language.I18n;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.HandlerMethod;
import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.translates.ChatTranslator;
import cn.plumc.translateoverlay.translates.ItemTranslator;
import cn.plumc.translateoverlay.translates.ScoreboardTranslator;
import cn.plumc.translateoverlay.translates.TitleTranslator;
import cn.plumc.translateoverlay.utils.CacheUtil;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Translator {
    public static final int MAX_TRY = 3;
    public static final int MAX_CACHE = 100;
    public static final Map<Language, String> LANGUAGE_MAPPING = new HashMap<>();


    private final CacheUtil.IntCounter invokeCounter = new CacheUtil.IntCounter();
    private final ConcurrentHashMap<String, Future<String>> cache = new ConcurrentHashMap<>(){};
    private final ConcurrentHashMap<String, CacheUtil.IntCounter> cacheCounter = new ConcurrentHashMap<>(){};
    public void clearCache(){
        cache.clear();
        ItemTranslator.clear();
        ScoreboardTranslator.clear();
        TitleTranslator.clear();
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
            String fromLang = (getLanguageMapping().containsKey(langFrom) ? getLanguageMapping().get(langFrom) : langFrom.code);
            String toLang = (getLanguageMapping().containsKey(langTo) ? getLanguageMapping().get(langTo) : langTo.code);
            Callable<String> callable = () -> {
                try {
                    String translated;
                    int counter = 0;
                    while (counter <= MAX_TRY) {
                        counter++;
                        translated = translate(message, fromLang, toLang);
                        if (translated!=null) return translated;
                    }
                    return I18n.translate("message.translateoverlay.translator.error");
                } catch (IOException e) {
                    ChatTranslator.sendBypassMessage(new TranslatableText("message.translateoverlay.translator.error"));
                    ChatTranslator.sendBypassMessage(new TranslatableText(e.getMessage()));
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
    abstract public String translate(String message, String langFrom, String langTo) throws IOException;
    abstract public String getTranslatorName();
    public HandlerMethod getHandlerMethod(){
        return HandlerMethod.REJOIN;
    };

    public Map<Language, String> getLanguageMapping() {
        return LANGUAGE_MAPPING;
    }

    private long startTime = 0;
    private final Lock lock = new ReentrantLock();
    protected <T> T delay(long time, ThrowingSupplier<T> runnable) throws IOException {
        lock.lock();
        if (System.currentTimeMillis() - startTime < time) {
            try {
                TimeUnit.MILLISECONDS.sleep(time - (System.currentTimeMillis() - startTime));
            } catch (InterruptedException ignored) {}
        }
        try {
            return runnable.get();
        } finally {
            startTime = System.currentTimeMillis();
            lock.unlock();
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws IOException;
    }

}

