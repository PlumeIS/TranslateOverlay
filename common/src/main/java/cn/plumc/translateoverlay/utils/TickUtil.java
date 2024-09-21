package cn.plumc.translateoverlay.utils;

import cn.plumc.translateoverlay.TranslateOverlay;
import net.minecraft.client.MinecraftClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class TickUtil {
    private static boolean isWaitingTask = false;
    private static Predicate<Object> waitingPredicate;
    private static Runnable waitingTask;
    private static Long waitingStartedTime;
    private static Integer waitingTimeout;
    private static final Queue<Runnable> tasks = new LinkedList<>();
    private static final List<Runnable> recurringTasks = new LinkedList<>(); ;
    private static final Map<Runnable, CacheUtil.IntCounter> delayTask = new ConcurrentHashMap<>();

    public static void onClientTick() {
        if (!tasks.isEmpty()) {
            MinecraftClient.getInstance().execute(tasks.poll());
        }

        if (!delayTask.isEmpty()) {
            for (Map.Entry<Runnable, CacheUtil.IntCounter> entry : delayTask.entrySet()) {
                entry.getValue().set(entry.getValue().get()-1);
                if (entry.getValue().get() <= 0){
                    MinecraftClient.getInstance().execute(entry.getKey());
                    delayTask.remove(entry.getKey());
                }
            }
        }

        for (Runnable task:recurringTasks){
            MinecraftClient.getInstance().execute(task);
        }

        if (isWaitingTask&&waitingPredicate.test(null)){
            MinecraftClient.getInstance().execute(waitingTask);
            clearWaitingTask();
        }

        if (isWaitingTask&&(System.currentTimeMillis()-waitingStartedTime)>waitingTimeout){
            clearWaitingTask();
        }

        if (TranslateOverlay.translateKey.isPressed()||TranslateOverlay.OCRKey.isPressed()){
            KeyUtil.onKeyPress();
        }
    }

    public static void tickRun(Runnable task){
        TickUtil.tasks.add(task);
    }

    public static void runAfterTick(Runnable task, double delay){
        int tick = (int) (delay * 20);
        delayTask.put(task, new CacheUtil.IntCounter(tick));
    }

    public static int addTask(Runnable task){
        recurringTasks.add(task);
        return recurringTasks.size() - 1;
    }

    public static void removeTask(int index){
        recurringTasks.remove(index);
    }

    public static void tickWait(Predicate<Object> predicate, Runnable runnable, int timeout){
        waitingStartedTime = System.currentTimeMillis();
        waitingPredicate = predicate;
        waitingTask = runnable;
        waitingTimeout = timeout;
        isWaitingTask = true;
    }

    private static void clearWaitingTask(){
        waitingStartedTime = null;
        waitingPredicate = null;
        waitingTask = null;
        waitingTimeout = null;
        isWaitingTask = false;
    }
}
