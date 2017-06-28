/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientScheduler
 * Created by HellFirePvP
 * Date: 27.06.2017 / 11:32
 */
public class ClientScheduler {

    private static long clientTick = 0;
    private static final Object lock = new Object();

    private boolean inTick = false;
    private Map<Runnable, Counter> queuedRunnables = new HashMap<>();
    private Map<Runnable, Integer> waitingRunnables = new HashMap<>();

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        clientTick++;

        inTick = true;
        synchronized (lock) {
            inTick = true;
            Iterator<Runnable> iterator = queuedRunnables.keySet().iterator();
            while (iterator.hasNext()) {
                Runnable r = iterator.next();
                Counter delay = queuedRunnables.get(r);
                delay.decrement();
                if(delay.value <= 0) {
                    r.run();
                    iterator.remove();
                }
            }
            inTick = false;
            for (Map.Entry<Runnable, Integer> waiting : waitingRunnables.entrySet()) {
                queuedRunnables.put(waiting.getKey(), new Counter(waiting.getValue()));
            }
        }
        waitingRunnables.clear();
    }

    public static long getClientTick() {
        return clientTick;
    }

    public void addRunnable(Runnable r, int tickDelay) {
        synchronized (lock) {
            if(inTick) {
                waitingRunnables.put(r, tickDelay);
            } else {
                queuedRunnables.put(r, new Counter(tickDelay));
            }
        }
    }

    public static class Counter {

        public int value;

        public Counter(int value) {
            this.value = value;
        }

        public void decrement() {
            value--;
        }

        public void increment() {
            value++;
        }

    }


}
