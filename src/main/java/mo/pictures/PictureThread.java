package mo.pictures;

import mo.Dot;

import java.util.concurrent.CountDownLatch;

class PictureThread extends Thread {
    Dot begin;
    Dot end;
    ThreadAction someAction;
    CountDownLatch countDownLatch;

    PictureThread(Dot begin, Dot end, CountDownLatch countDownLatch, ThreadAction some) {
        this.begin = begin;
        this.end = end;
        someAction = some;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            morphOperation(someAction);
            countDownLatch.countDown();
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }

    private void morphOperation(ThreadAction someAction) { //поменял название входного елемента. мало ли сломалось
        for (int x = begin.getX(); x < end.getX(); x++) {
            for (int y = begin.getY(); y < end.getY(); y++) {
                someAction.operation(x, y);
            }
        }
    }
}
