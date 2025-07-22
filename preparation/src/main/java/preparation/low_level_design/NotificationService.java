package preparation.low_level_design;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationService {
    static enum Status {
        SUBMITTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED;
    }
    static class NotificationJob {
        private final UUID id;
        private final String recipient;
        private final String payload;
        private int retryCount;
        private Status status;

        public NotificationJob(UUID id, String recipient, String payload) {
            this.id = id;
            this.recipient = recipient;
            this.payload = payload;
            this.retryCount = 0;
            this.status = Status.SUBMITTED;
        }
    }

    static class NotificationDispatcherService {

        private final Queue<NotificationJob> jobQueue;
        private final Set<UUID> jobSet;
        private final List<NotificationJob> deadLetterQueue;
        private final ExecutorService workerPool;
        private final ScheduledExecutorService retryScheduler;
        private final AtomicInteger successCounter;
        private final AtomicInteger retryCounter;
        private final AtomicInteger failedCounter;

        static final int RETRY_COUNT = 3;
        static final int RETRY_DELAY = 500;

        private final int workerThreadCount;
        private final int retryThreadCount;

        public NotificationDispatcherService(final int workerThreadCount, final int retryThreadCount) {
            this.jobQueue = new ConcurrentLinkedDeque<>();
            this.workerPool = Executors.newFixedThreadPool(workerThreadCount);
            this.retryScheduler = Executors.newScheduledThreadPool(retryThreadCount);
            this.deadLetterQueue = new LinkedList<>();
            this.jobSet = new HashSet<>();
            this.successCounter = new AtomicInteger(0);
            this.retryCounter = new AtomicInteger(0);
            this.failedCounter = new AtomicInteger(0);
            this.workerThreadCount = workerThreadCount;
            this.retryThreadCount = retryThreadCount;
        }

        public void start() {
            for(int i=0; i<workerThreadCount; i++) {
                workerPool.submit(this::workerJob);
            }
        }

        public void sendMessage(final NotificationJob job) {
            if(jobSet.contains(job.id)) {
                System.out.println("🔁 Duplicate detected, skipping notification: " + job.id);
                return;
            }

            jobSet.add(job.id);
            jobQueue.offer(job);
        }

        public void workerJob() {
            try {
                while (true) {
                    final NotificationJob job = jobQueue.poll();
                    if (job == null) {
                        System.out.println("Current queue is empty");
                        Thread.sleep(100);
                        continue;
                    }
                    job.status = Status.IN_PROGRESS;
                    final boolean result = send(job);
                    if(result) {
                        successCounter.incrementAndGet();
                        System.out.println("Job sent successfully id: " + job.id + " Thread : "+Thread.currentThread().getName());
                        job.status = Status.COMPLETED;
                    } else {
                        failedCounter.incrementAndGet();
                        if(job.retryCount < RETRY_COUNT) {
                            System.out.println("Job sent failed, retry count is " + job.retryCount + " retrying...");
                            job.retryCount = job.retryCount + 1;
                            scheduleJobForRetry(job, RETRY_DELAY);
                        } else {
                            System.out.println("Job sent failed, retry count is " + job.retryCount + " " +
                                    "Moving the message to dead letter queue...");
                            job.status = Status.FAILED;
                            deadLetterQueue.add(job);
                        }

                    }
                }
            } catch (InterruptedException exception) {
                System.out.println(exception.getMessage());

            }
        }

        private void scheduleJobForRetry(final NotificationJob job, final int delay) {
            retryCounter.incrementAndGet();
            retryScheduler.schedule(() -> {
                jobQueue.offer(job);
            }, delay, TimeUnit.MILLISECONDS);
        }

        private boolean send(NotificationJob job) {
            return new Random().nextBoolean();
        }

        public void printMetrics() {
            System.out.println(":: Notification Dispatcher Service Summary ::");
            System.out.println(":: Success Counter ::: " + successCounter.get());
            System.out.println(":: Retry Counter ::: " + retryCounter.get());
            System.out.println(":: Failed Counter ::: " + failedCounter.get());
        }

        public void shutdown() {
            workerPool.shutdown();
            retryScheduler.shutdown();
        }
    }

    public static void main(String[] args)  {
        NotificationDispatcherService notificationDispatcherService = new NotificationDispatcherService(3, 3);
        notificationDispatcherService.start();
        notificationDispatcherService.printMetrics();

        for (int i=0; i<10; i++) {
            final NotificationJob job = new NotificationJob(UUID.randomUUID(), UUID.randomUUID().toString(), new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes())));
            notificationDispatcherService.sendMessage(job);
        }

        try {
            Thread.sleep(2000);
        }  catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            notificationDispatcherService.shutdown();
            notificationDispatcherService.printMetrics();
        }
    }
}
