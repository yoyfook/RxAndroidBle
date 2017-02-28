package com.polidea.rxandroidble.internal.radio;


import com.polidea.rxandroidble.internal.RadioAwaitReleaseInterface;
import com.polidea.rxandroidble.internal.RadioReleaseInterface;
import java.util.concurrent.Semaphore;

class RadioSemaphore implements RadioReleaseInterface, RadioAwaitReleaseInterface {

    private final Semaphore semaphore = new Semaphore(0);

    @Override
    public void awaitRelease() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public void release() {
        semaphore.release();
    }
}
