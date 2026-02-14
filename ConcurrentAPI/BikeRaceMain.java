import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Rider implements Callable<Rider> {

    private String riderName;
    private int raceDistance;
    private int covered = 0;
    private int speed;
    private LocalTime start;
    private LocalTime finish;
    private CountDownLatch latch;

    public Rider(String riderName, CountDownLatch latch, int raceDistance) {
        this.riderName = riderName;
        this.latch = latch;
        this.raceDistance = raceDistance;
        this.speed = new Random().nextInt(150) + 120;
    }

    @Override
    public Rider call() throws Exception {
        latch.await();

        System.out.println(riderName + " started racing!");
        start = LocalTime.now();

        while (covered < raceDistance) {
            covered += 100;
            System.out.println(riderName + " covered " + covered + " meters");
            Thread.sleep(250);
        }

        finish = LocalTime.now();
        System.out.println(riderName + " COMPLETED");

        return this;
    }

    public String getRiderName() {
        return riderName;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getFinish() {
        return finish;
    }

    public long getDuration() {
        return Duration.between(start, finish).toMillis();
    }
}

class RaceGame {

    private int totalDistance;
    private int totalRiders;

    public RaceGame() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Distance (KM): ");
        totalDistance = sc.nextInt() * 1000;

        System.out.print("Enter Number of Riders: ");
        totalRiders = sc.nextInt();
    }

    public void beginRace() throws Exception {

        ExecutorService service = Executors.newFixedThreadPool(totalRiders);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Rider>> futureList = new ArrayList<>();
        List<Rider> completedRiders = new ArrayList<>();

        Scanner sc = new Scanner(System.in);
        List<Rider> riderTasks = new ArrayList<>();

        for (int i = 0; i < totalRiders; i++) {
            System.out.print("Enter rider name: ");
            String name = sc.next();
            riderTasks.add(new Rider(name, latch, totalDistance));
        }

        System.out.println("\nAll riders ready!");
        System.out.println("Race will start in 3 seconds...\n");

        for (int i = 3; i >= 1; i--) {
            System.out.println(i);
            Thread.sleep(1000);
        }

        latch.countDown();

        for (Rider rider : riderTasks) {
            futureList.add(service.submit(rider));
        }

        System.out.println("\nRace Started...\n");

        for (Future<Rider> future : futureList) {
            completedRiders.add(future.get());
        }

        service.shutdown();

        displayResults(completedRiders);
    }

    private void displayResults(List<Rider> riders) {

        riders.sort(Comparator.comparing(Rider::getFinish));

        System.out.println("\n========== FINAL RESULT ==========");
        System.out.println("Rank\tName\tStart Time\t\tFinish Time\t\tDuration(ms)");

        int position = 1;
        for (Rider r : riders) {
            System.out.println(position++ + "\t"
                    + r.getRiderName() + "\t"
                    + r.getStart() + "\t"
                    + r.getFinish() + "\t"
                    + r.getDuration());
        }
    }
}

public class BikeRaceMain {

    public static void main(String[] args) throws Exception {
        System.out.println("Bike Racing Game Started\n");
        RaceGame race = new RaceGame();
        race.beginRace();
    }
}
