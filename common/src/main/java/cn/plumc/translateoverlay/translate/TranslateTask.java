package cn.plumc.translateoverlay.translate;

import cn.plumc.translateoverlay.config.Config;

import java.util.concurrent.Callable;

public class TranslateTask implements Callable<String> {
    private String text;

    public TranslateTask(String text) {
        this.text = text;
    }

    @Override
    public String call() {
        return Config.getTranslator().translate(text);
    }
}
