// Af Nikolaj Jakobsen

package pigtracker.util;

import org.junit.jupiter.api.Test;
import pigtracker.model.Animal;
import pigtracker.model.DisplayType;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.TopThreePigs;
import pigtracker.model.Visit;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsUtilTest {

    private static final double DELTA = 1e-9;

    // Builds an animal carrying only the derived fields the metric helpers read; the rest are irrelevant here.
    private static Animal animal(int number, Double fcr, Double latestWeightKg, Double totalFeedKg, Double weightGainKg,
            Integer completedDays) {
        return new Animal(0, number, "984", 1, 1, Animal.Status.ACTIVE, null, null, fcr, null, totalFeedKg,
                weightGainKg, latestWeightKg, completedDays, null, null);
    }

    private static Visit visit(int animalNumber, LocalDateTime visitTime, int durationSec, int feedIntakeG) {
        return new Visit(0, animalNumber, "984", 1, 1, visitTime, durationSec, null, feedIntakeG);
    }

    @Test
    void fcrListKeepsOnlyPositiveNonNullValues() {
        // Null FCRs and values <= 0 (incomplete animals) are dropped.
        List<Animal> animals = List.of(animal(1, 2.0, null, null, null, null), animal(2, null, null, null, null, null),
                animal(3, -1.0, null, null, null, null), animal(4, 0.0, null, null, null, null),
                animal(5, 1.5, null, null, null, null));

        assertEquals(List.of(2.0, 1.5), MetricsUtil.fcrList(animals));
    }

    @Test
    void weightListKeepsOnlyNonNullWeights() {
        List<Animal> animals = List.of(animal(1, null, 100.0, null, null, null),
                animal(2, null, null, null, null, null), animal(3, null, 80.0, null, null, null));

        assertEquals(List.of(100.0, 80.0), MetricsUtil.weightList(animals));
    }

    @Test
    void dailyGainListDividesGainByCompletedDaysAndSkipsIncompleteAnimals() {
        List<Animal> animals = List.of(animal(1, null, null, null, 20.0, 10), // 2.0/day
                animal(2, null, null, null, 30.0, 0), // dropped: zero completed days
                animal(3, null, null, null, null, 5), // dropped: no gain
                animal(4, null, null, null, 15.0, 3)); // 5.0/day

        assertEquals(List.of(2.0, 5.0), MetricsUtil.dailyGainList(animals));
    }

    @Test
    void feedPerDayListDividesFeedByCompletedDaysAndSkipsIncompleteAnimals() {
        List<Animal> animals = List.of(animal(1, null, null, 50.0, null, 10), // 5.0/day
                animal(2, null, null, 40.0, null, null), // dropped: no completed days
                animal(3, null, null, 30.0, null, 5)); // 6.0/day

        assertEquals(List.of(5.0, 6.0), MetricsUtil.feedPerDayList(animals));
    }

    @Test
    void animalCountReturnsListSize() {
        List<Animal> animals = List.of(animal(1, null, null, null, null, null),
                animal(2, null, null, null, null, null), animal(3, null, null, null, null, null));

        assertEquals(3, MetricsUtil.animalCount(animals));
    }

    @Test
    void meanHelpersAverageTheUnderlyingLists() {
        List<Animal> animals = List.of(animal(1, 2.0, null, 50.0, 20.0, 10), animal(2, 1.0, null, 30.0, 15.0, 5));

        assertEquals(1.5, MetricsUtil.fcrListMean(animals), DELTA); // (2.0 + 1.0) / 2
        assertEquals(2.5, MetricsUtil.dailyGainListMean(animals), DELTA); // (2.0 + 3.0) / 2
        assertEquals(5.5, MetricsUtil.feedPerDayListMean(animals), DELTA); // (5.0 + 6.0) / 2
    }

    @Test
    void calculateActivityByHourCountsVisitsPerHourOfDay() {
        List<Visit> visits = List.of(visit(1, LocalDateTime.of(2026, 4, 8, 0, 30), 60, 1000),
                visit(1, LocalDateTime.of(2026, 4, 8, 9, 10), 60, 1000),
                visit(2, LocalDateTime.of(2026, 4, 8, 9, 50), 60, 1000),
                visit(2, LocalDateTime.of(2026, 4, 8, 10, 5), 60, 1000),
                visit(3, LocalDateTime.of(2026, 4, 8, 23, 0), 60, 1000));

        List<Integer> byHour = MetricsUtil.calculateActivityByHour(visits);

        assertEquals(24, byHour.size());
        assertEquals(1, byHour.get(0));
        assertEquals(2, byHour.get(9));
        assertEquals(1, byHour.get(10));
        assertEquals(1, byHour.get(23));
        assertEquals(0, byHour.get(12)); // an hour with no visits stays at zero
    }

    @Test
    void visitsPerPigPerDayDividesVisitCountByActiveDaySpan() {
        Map<Integer, List<Visit>> visitsByAnimal = new LinkedHashMap<>();
        // Two visits on the same day -> span of 1 day -> 2 visits/day.
        visitsByAnimal.put(1, List.of(visit(1, LocalDateTime.of(2026, 4, 8, 9, 0), 60, 1000),
                visit(1, LocalDateTime.of(2026, 4, 8, 18, 0), 60, 1000)));
        // Three visits across 08 -> 10 April -> span of 3 days -> 1 visit/day.
        visitsByAnimal.put(2, List.of(visit(2, LocalDateTime.of(2026, 4, 8, 9, 0), 60, 1000),
                visit(2, LocalDateTime.of(2026, 4, 10, 9, 0), 60, 1000),
                visit(2, LocalDateTime.of(2026, 4, 9, 9, 0), 60, 1000)));
        // Animals with no visits are skipped entirely.
        visitsByAnimal.put(3, List.of());

        assertEquals(List.of(2.0, 1.0), MetricsUtil.visitsPerPigPerDayList(visitsByAnimal));
    }

    @Test
    void calculateMeanMedianMetricsReturnsTheSixMetricsInOrder() {
        List<Animal> animals = List.of(animal(1, 2.0, 100.0, 10.0, null, 5), animal(2, 1.0, 80.0, 12.0, null, 4));
        List<Visit> visits = List.of(visit(1, LocalDateTime.of(2026, 4, 8, 9, 0), 60, 1000),
                visit(2, LocalDateTime.of(2026, 4, 9, 10, 0), 120, 3000));
        Map<Integer, List<Visit>> visitsByAnimal = new LinkedHashMap<>();
        visitsByAnimal.put(1, List.of(visits.get(0)));
        visitsByAnimal.put(2, List.of(visits.get(1)));

        List<MeanMedianMetric> metrics = MetricsUtil.calculateMeanMedianMetrics(animals, visits, visitsByAnimal);

        assertEquals(6, metrics.size());

        // FCR: mean of [2.0, 1.0].
        assertEquals("FCR", metrics.get(0).metric());
        assertEquals(1.5, metrics.get(0).mean(), DELTA);
        assertEquals(DisplayType.DECIMAL, metrics.get(0).displayType());

        // Weight: mean of [100, 80].
        assertEquals("Weight", metrics.get(1).metric());
        assertEquals(90.0, metrics.get(1).mean(), DELTA);

        // Feed/Visit (kg): feed grams converted to kg -> mean of [1.0, 3.0].
        assertEquals("Feed/Visit (kg)", metrics.get(2).metric());
        assertEquals(2.0, metrics.get(2).mean(), DELTA);

        // Feed/Day (kg): per-animal feed / completed days -> mean of [2.0, 3.0].
        assertEquals("Feed/Day (kg)", metrics.get(3).metric());
        assertEquals(2.5, metrics.get(3).mean(), DELTA);

        // Visits/Pig/Day: one visit per pig on a single day -> mean of [1.0, 1.0].
        assertEquals("Visits/Pig/Day", metrics.get(4).metric());
        assertEquals(1.0, metrics.get(4).mean(), DELTA);

        // Visit Duration (s): mean of [60, 120], displayed as time.
        assertEquals("Visit Duration (s)", metrics.get(5).metric());
        assertEquals(90.0, metrics.get(5).mean(), DELTA);
        assertEquals(DisplayType.TIME, metrics.get(5).displayType());
    }

    @Test
    void calculateTopThreePigsRanksEachMetricAndCapsAtThree() {
        List<Animal> animals = List.of(animal(1, 3.0, 90.0, 30.0, null, null), animal(2, 1.0, 110.0, 50.0, null, null),
                animal(3, 2.0, 100.0, 40.0, null, null), animal(4, null, 120.0, 60.0, null, null));
        List<Visit> visits = List.of(visit(1, LocalDateTime.of(2026, 4, 8, 9, 0), 100, 1000),
                visit(2, LocalDateTime.of(2026, 4, 8, 9, 0), 300, 1000),
                visit(2, LocalDateTime.of(2026, 4, 8, 10, 0), 150, 1000),
                visit(2, LocalDateTime.of(2026, 4, 8, 11, 0), 50, 1000),
                visit(3, LocalDateTime.of(2026, 4, 8, 9, 0), 200, 1000),
                visit(3, LocalDateTime.of(2026, 4, 8, 10, 0), 75, 1000));
        Map<Integer, List<Visit>> visitsByAnimal = new LinkedHashMap<>();
        visitsByAnimal.put(1, List.of(visits.get(0)));
        visitsByAnimal.put(2, List.of(visits.get(1), visits.get(2), visits.get(3)));
        visitsByAnimal.put(3, List.of(visits.get(4), visits.get(5)));

        List<TopThreePigs> topThree = MetricsUtil.calculateTopThreePigs(animals, visits, visitsByAnimal);

        assertEquals(5, topThree.size());

        // FCR: lowest first, animal 4 dropped for having no FCR.
        TopThreePigs fcr = topThree.get(0);
        assertEquals("FCR", fcr.metric());
        assertArrayEquals(new int[] { 2, 3, 1 }, fcr.pigNumbers());
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, fcr.pigValues(), DELTA);
        assertEquals(DisplayType.DECIMAL, fcr.displayType());

        // Weight: heaviest first, capped at the top three of four animals.
        TopThreePigs weight = topThree.get(1);
        assertEquals("Weight", weight.metric());
        assertArrayEquals(new int[] { 4, 2, 3 }, weight.pigNumbers());
        assertArrayEquals(new double[] { 120.0, 110.0, 100.0 }, weight.pigValues(), DELTA);

        // Feed Intake: most feed first, capped at three.
        TopThreePigs feed = topThree.get(2);
        assertEquals("Feed Intake", feed.metric());
        assertArrayEquals(new int[] { 4, 2, 3 }, feed.pigNumbers());
        assertArrayEquals(new double[] { 60.0, 50.0, 40.0 }, feed.pigValues(), DELTA);

        // Longest Visit: ranks individual visits, so the same pig can appear more than once.
        TopThreePigs longest = topThree.get(3);
        assertEquals("Longest Visit", longest.metric());
        assertArrayEquals(new int[] { 2, 3, 2 }, longest.pigNumbers());
        assertArrayEquals(new double[] { 300.0, 200.0, 150.0 }, longest.pigValues(), DELTA);
        assertEquals(DisplayType.TIME, longest.displayType());

        // Most Visits: ranked by visit count per pig.
        TopThreePigs mostVisits = topThree.get(4);
        assertEquals("Most Visits", mostVisits.metric());
        assertArrayEquals(new int[] { 2, 3, 1 }, mostVisits.pigNumbers());
        assertArrayEquals(new double[] { 3.0, 2.0, 1.0 }, mostVisits.pigValues(), DELTA);
        assertEquals(DisplayType.INT, mostVisits.displayType());
    }
}
