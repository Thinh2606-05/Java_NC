package tfus.web.dto;

import tfus.algorithm.result.PatternEvaluation;
import tfus.algorithm.result.WindowResult;
import tfus.domain.*;
import tfus.event.MiningEvent;
import tfus.service.MiningFacade;
import tfus.snapshot.ListsSnapshot;
import tfus.verification.Check;
import tfus.verification.VerificationReport;

import java.util.*;

/**
 * Chuyển đổi các thực thể domain, kết quả, trace và verification sang Map/List tương thích với Json serializer.
 */
public final class JsonMapper {

    private JsonMapper() {}

    public static Map<String, Object> inputToJson(MiningInput input) {
        Map<String, Object> res = new LinkedHashMap<>();
        Dataset ds = input.dataset();
        res.put("name", ds.name());

        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("windowSize", input.config().windowSize());
        cfg.put("batchSize", ds.batchSize());
        cfg.put("periodSize", ds.periodSize());
        cfg.put("delta", input.config().delta());
        cfg.put("pruningStrategy", input.config().pruningStrategy());
        cfg.put("recursionRule", input.config().recursionRule().name());
        res.put("config", cfg);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, Double> e : ds.externalUtility().entrySet()) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("item", e.getKey());
            itemMap.put("eu", e.getValue());
            itemMap.put("m", ds.m(e.getKey()));
            items.add(itemMap);
        }
        res.put("items", items);

        List<Map<String, Object>> txns = new ArrayList<>();
        for (Transaction t : ds.transactions()) {
            Map<String, Object> tmap = new LinkedHashMap<>();
            tmap.put("tid", t.tid());
            tmap.put("batch", t.batchNumber());
            tmap.put("period", t.periodNumber());
            List<Map<String, Object>> iqs = new ArrayList<>();
            for (ItemQuantity iq : t.items()) {
                Map<String, Object> qmap = new LinkedHashMap<>();
                qmap.put("item", iq.item());
                qmap.put("qty", iq.quantity());
                iqs.add(qmap);
            }
            tmap.put("items", iqs);
            txns.add(tmap);
        }
        res.put("transactions", txns);
        return res;
    }

    public static Map<String, Object> runResultToJson(MiningInput input, MiningFacade.RunResult run) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("input", inputToJson(input));

        List<Map<String, Object>> windows = new ArrayList<>();
        for (WindowResult wr : run.results()) {
            windows.add(windowResultToJson(wr));
        }
        res.put("windows", windows);

        List<Map<String, Object>> traceList = new ArrayList<>();
        for (MiningEvent ev : run.events()) {
            traceList.add(eventToJson(ev));
        }
        res.put("trace", traceList);
        return res;
    }

    public static Map<String, Object> windowResultToJson(WindowResult wr) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("windowNumber", wr.windowNumber());
        map.put("elapsedMs", wr.elapsedMs());

        List<Map<String, Object>> pats = new ArrayList<>();
        for (Pattern p : wr.patterns()) {
            Map<String, Object> pmap = new LinkedHashMap<>();
            pmap.put("label", p.label());
            pmap.put("tFUR", p.tFUR());
            pmap.put("startPeriod", p.startPeriod());
            pats.add(pmap);
        }
        map.put("patterns", pats);

        List<Map<String, Object>> evals = new ArrayList<>();
        for (PatternEvaluation pe : wr.allEvaluations()) {
            Map<String, Object> emap = new LinkedHashMap<>();
            List<String> items = pe.pattern().stream().map(FuzzyItem::label).toList();
            emap.put("pattern", items);
            emap.put("fu", pe.fu());
            emap.put("rfu", pe.rfu());
            emap.put("mstfu", pe.mstfu());
            emap.put("rTFUUBR", pe.rTFUUBR());
            emap.put("tFUR", pe.tFUR());
            emap.put("startPeriod", pe.startPeriod());
            emap.put("pruned", pe.pruned());
            emap.put("isResult", pe.isResult());
            evals.add(emap);
        }
        map.put("evaluations", evals);

        if (wr.snapshot() != null) {
            map.put("snapshot", snapshotToJson(wr.snapshot()));
        }
        return map;
    }

    public static Map<String, Object> snapshotToJson(ListsSnapshot snap) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("windowNumber", snap.windowNumber());
        map.put("label", snap.label());

        List<Map<String, Object>> lists = new ArrayList<>();
        for (ListsSnapshot.ListSnapshot ls : snap.lists()) {
            Map<String, Object> lmap = new LinkedHashMap<>();
            lmap.put("item", ls.fuzzyItem().label());
            lmap.put("fu", ls.fu());
            lmap.put("fuub", ls.fuub());
            lmap.put("rfu", ls.rfu());
            lmap.put("mstfu", ls.mstfu());
            lmap.put("tfuubr", ls.tfuubr());

            List<Map<String, Object>> ents = new ArrayList<>();
            for (ListsSnapshot.EntrySnapshot es : ls.entries()) {
                Map<String, Object> em = new LinkedHashMap<>();
                em.put("tid", es.tid());
                em.put("batch", es.batchNumber());
                em.put("period", es.periodNumber());
                em.put("u", es.utility());
                em.put("minFv", es.minFv());
                em.put("ru", es.remainingUtility());
                ents.add(em);
            }
            lmap.put("entries", ents);
            lists.add(lmap);
        }
        map.put("lists", lists);
        return map;
    }

    public static Map<String, Object> eventToJson(MiningEvent event) {
        Map<String, Object> map = new LinkedHashMap<>();
        switch (event) {
            case MiningEvent.BatchArrived e -> {
                map.put("type", "BatchArrived");
                map.put("batchNumber", e.batchNumber());
                map.put("windowNumber", e.windowNumber());
            }
            case MiningEvent.TransactionScanned e -> {
                map.put("type", "TransactionScanned");
                map.put("tid", e.tid());
                map.put("tidIndex", e.tidIndex());
                map.put("batchNumber", e.batchNumber());
                map.put("periodNumber", e.periodNumber());
                map.put("tfu", e.tfu());
                map.put("mtfu", e.mtfu());
                List<Map<String, Object>> detList = new ArrayList<>();
                for (var d : e.details()) {
                    Map<String, Object> dm = new LinkedHashMap<>();
                    dm.put("item", d.item());
                    dm.put("quantity", d.quantity());
                    dm.put("utility", d.utility());
                    dm.put("fv", d.fvMap());
                    dm.put("fu", d.fuMap());
                    detList.add(dm);
                }
                map.put("details", detList);
            }
            case MiningEvent.BatchRemoved e -> {
                map.put("type", "BatchRemoved");
                map.put("batchNumber", e.batchNumber());
                List<Map<String, Object>> rem = new ArrayList<>();
                for (var re : e.removedEntries()) {
                    Map<String, Object> rm = new LinkedHashMap<>();
                    rm.put("item", re.fuzzyItem().label());
                    rm.put("tid", re.tid());
                    rm.put("fu", re.fuzzyUtility());
                    rem.add(rm);
                }
                map.put("removedEntries", rem);
            }
            case MiningEvent.RestructureCompleted e -> {
                map.put("type", "RestructureCompleted");
                map.put("windowNumber", e.windowNumber());
                map.put("sigmaTfu", e.sigmaTfu());
                map.put("processingOrder", e.processingOrder().stream().map(FuzzyItem::label).toList());
                map.put("prunedItems", e.prunedItems().stream().map(FuzzyItem::label).toList());
            }
            case MiningEvent.PatternEvaluated e -> {
                map.put("type", "PatternEvaluated");
                map.put("pattern", e.pattern().stream().map(FuzzyItem::label).toList());
                map.put("fu", e.fu());
                map.put("rfu", e.rfu());
                map.put("mstfu", e.mstfu());
                map.put("rTFUUBR", e.rTFUUBR());
                map.put("tFUR", e.tFUR());
                map.put("pruned", e.pruned());
                map.put("isResult", e.isResult());
            }
            case MiningEvent.WindowMined e -> {
                map.put("type", "WindowMined");
                map.put("windowNumber", e.windowNumber());
                map.put("resultCount", e.results().size());
                map.put("totalEvaluated", e.totalEvaluated());
            }
        }
        return map;
    }

    public static Map<String, Object> verificationToJson(VerificationReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("datasetName", report.datasetName());
        map.put("totalChecks", report.checks().size());
        map.put("passCount", report.passCount());
        map.put("roundingCount", report.roundingCount());
        map.put("mismatchCount", report.mismatchCount());
        map.put("missingCount", report.missingCount());
        map.put("infoCount", report.infoCount());
        map.put("isFullyPassing", report.isFullyPassing());

        List<Map<String, Object>> checks = new ArrayList<>();
        for (Check c : report.checks()) {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("group", c.group().name());
            cm.put("groupDescription", c.group().description());
            cm.put("name", c.name());
            cm.put("expected", c.expected());
            cm.put("actual", c.actual());
            cm.put("status", c.status().name());
            cm.put("note", c.note());
            checks.add(cm);
        }
        map.put("checks", checks);
        return map;
    }
}
