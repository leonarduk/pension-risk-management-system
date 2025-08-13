package com.leonarduk.finance.springboot;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST endpoint exposing time-series utilities.
 */
@RequestMapping("/series")
@RestController
public class SeriesEndpoint {

    @Autowired
    private final StockFeed stockFeed;

    public SeriesEndpoint(StockFeed stockFeed) {
        this.stockFeed = stockFeed;
    }

    /**
     * Align one series to another using index rebasing.
     *
     * @param sourceTicker ticker to rebase
     * @param targetTicker ticker providing the target index
     * @param years optional history length
     * @return mapped series keyed by date
     */
    @PostMapping("/map")
    @ResponseBody
    public Map<String, Map<String, Double>> mapSeries(
            @RequestParam("source") String sourceTicker,
            @RequestParam("target") String targetTicker,
            @RequestParam(name = "years", required = false, defaultValue = "1") int years
    ) throws IOException {
        Instrument source = Instrument.fromString(sourceTicker);
        Instrument target = Instrument.fromString(targetTicker);

        Optional<StockV1> srcStock = stockFeed.get(source, years, true, true, false);
        Optional<StockV1> tgtStock = stockFeed.get(target, years, true, true, false);

        Map<String, Map<String, Double>> result = new HashMap<>();
        if (srcStock.isEmpty() || tgtStock.isEmpty()) {
            result.put("mapped", Collections.emptyMap());
            return result;
        }

        Map<LocalDate, Double> srcSeries = toSeries(srcStock.get().getHistory());
        Map<LocalDate, Double> tgtSeries = toSeries(tgtStock.get().getHistory());

        Map<LocalDate, Double> aligned = alignSeries(srcSeries, tgtSeries);
        Map<String, Double> mapped = aligned.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue,
                        (a, b) -> b, LinkedHashMap::new));
        result.put("mapped", mapped);
        return result;
    }

    private Map<LocalDate, Double> toSeries(List<Bar> history) {
        return history.stream().collect(
                Collectors.toMap(bar -> bar.getEndTime().toLocalDate(),
                        bar -> bar.getClosePrice().doubleValue()));
    }

    private Map<LocalDate, Double> alignSeries(Map<LocalDate, Double> source, Map<LocalDate, Double> target) {
        SortedSet<LocalDate> intersection = new TreeSet<>(source.keySet());
        intersection.retainAll(target.keySet());
        if (intersection.isEmpty()) {
            return Collections.emptyMap();
        }
        LocalDate start = intersection.first();
        double scale = target.get(start) / source.get(start);

        Map<LocalDate, Double> rebased = source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * scale));

        SortedSet<LocalDate> orderedTarget = new TreeSet<>(target.keySet());
        Map<LocalDate, Double> result = new LinkedHashMap<>();
        Double last = null;
        for (LocalDate date : orderedTarget) {
            if (date.isBefore(start)) {
                continue;
            }
            if (rebased.containsKey(date)) {
                last = rebased.get(date);
            }
            if (last != null) {
                result.put(date, last);
            }
        }
        return result;
    }
}
