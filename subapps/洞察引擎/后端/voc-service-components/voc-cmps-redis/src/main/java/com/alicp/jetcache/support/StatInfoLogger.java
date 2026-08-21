package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class StatInfoLogger implements Consumer<StatInfo> {
    private static final Logger log = LoggerFactory.getLogger(StatInfoLogger.class);
    private boolean verboseLog;
    protected int maxNameLength = 65;

    public StatInfoLogger(boolean verboseLog) {
        this.verboseLog = verboseLog;
    }

    @Override
    public void accept(StatInfo statInfo) {
        List<CacheStat> stats = statInfo.getStats();
        Collections.sort(stats, (o1, o2) -> {
            if (o1.getCacheName() == null) {
                return -1;
            } else {
                return o2.getCacheName() == null ? 1 : o1.getCacheName().compareTo(o2.getCacheName());
            }
        });
        StringBuilder sb;
        if (this.verboseLog) {
            sb = this.logVerbose(statInfo);
        } else {
            sb = this.logStatSummary(statInfo);
        }
//        log.info("===========================================================");
    }

    private StringBuilder logTitle(int initSize, StatInfo statInfo) {
        StringBuilder sb = new StringBuilder(initSize);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
//        sb.append("jetcache stat from ").append(sdf.format(new Date(statInfo.getStartTime()))).append(" to ").append(sdf.format(statInfo.getEndTime())).append("\n");
        sb.append("jetcache stat from ").append(sdf.format(new Date(statInfo.getStartTime()))).append(" to ").append(sdf.format(statInfo.getEndTime()));
        log.info(sb.toString());
        return sb;
    }

    private void printSepLine(StringBuilder sb, String title) {
        StringBuilder sep = new StringBuilder();
        title.chars().forEach((c) -> {
            if (c == 124) {
                sep.append('+');
            } else {
                sep.append('-');
            }
        });
//        sep.append('\n');
        log.info(sep.toString());
    }

    private StringBuilder logStatSummary(StatInfo statInfo) {
        StringBuilder sb = this.logTitle(2048, statInfo);
        List<CacheStat> stats = statInfo.getStats();
        OptionalInt maxCacheNameLength = stats.stream().mapToInt((sx) -> {
            return this.getName(sx.getCacheName()).length();
        }).max();
        int len = Math.max(5, maxCacheNameLength.orElse(0));
        String title = String.format("%-" + len + "s|%10s|%7s|%14s|%14s|%14s|%14s|%11s|%11s", "cache", "qps", "rate", "get", "hit", "fail", "expire", "avgLoadTime", "maxLoadTime");
//        String title = String.format("%-" + (len-1) + "s|%5s|%6s|%4s|%4s|%4s|%4s|%11s|%11s", "cache", "qps", "rate", "get", "hit", "fail", "expire", "avgLoadTime", "maxLoadTime");
//        sb.append(title).append('\n');
        this.printSepLine(sb, title);
        log.info(title);
        Iterator var7 = stats.iterator();

        while(var7.hasNext()) {
            CacheStat s = (CacheStat)var7.next();
            StringBuilder content = new StringBuilder();
//            content.append(String.format("%-" + len + "s", this.getName(s.getCacheName())));
            content.append(String.format("%-" + len + "s", this.getName(s.getCacheName()))).append('|');
//            content.append(String.format("%,5.2f", s.qps()));
            content.append(String.format("%,10.2f", s.qps())).append('|');
//            content.append(String.format("%6.2f%%", s.hitRate() * 100.0));
            content.append(String.format("%6.2f%%", s.hitRate() * 100.0)).append('|');
//            content.append(String.format("%,4d", s.getGetCount()));
            content.append(String.format("%,14d", s.getGetCount())).append('|');
//            content.append(String.format("%,5d", s.getGetHitCount()));
            content.append(String.format("%,14d", s.getGetHitCount())).append('|');
//            content.append(String.format("%,5d", s.getGetFailCount()));
            content.append(String.format("%,14d", s.getGetFailCount())).append('|');
//            content.append(String.format("%,5d", s.getGetExpireCount()));
            content.append(String.format("%,14d", s.getGetExpireCount())).append('|');
//            content.append(String.format("%,11.1f", s.avgLoadTime()));
            content.append(String.format("%,11.1f", s.avgLoadTime())).append('|');
            content.append(String.format("%,11d", s.getMaxLoadTime()));
            log.info(String.valueOf(content));
        }
        this.printSepLine(sb, title);
        return sb;
    }

    private String getName(String name) {
        if (name == null) {
            return null;
        } else {
            return name.length() > this.maxNameLength ? "..." + name.substring(name.length() - this.maxNameLength + 3) : name;
        }
    }

    private StringBuilder logVerbose(StatInfo statInfo) {
        StringBuilder sb = this.logTitle(8192, statInfo);
        List<CacheStat> stats = statInfo.getStats();
        Iterator var4 = stats.iterator();

        while(var4.hasNext()) {
            CacheStat s = (CacheStat)var4.next();
            String title = String.format("%-10s|%10s|%14s|%14s|%14s|%14s|%14s|%9s|%7s|%7s", "oper", "qps/tps", "count", "success/hit", "fail", "miss", "expired", "avgTime", "minTime", "maxTime");
            this.printSepLine(sb, title);
            sb.append(s.getCacheName()).append("(hit rate ").append(String.format("%.3f", s.hitRate() * 100.0)).append("%)").append('\n');
            sb.append(title).append('\n');
            this.printSepLine(sb, title);
            sb.append(String.format("%-10s", "get")).append('|');
            sb.append(String.format("%,10.2f", s.qps())).append('|');
            sb.append(String.format("%,14d", s.getGetCount())).append('|');
            sb.append(String.format("%,14d", s.getGetHitCount())).append('|');
            sb.append(String.format("%,14d", s.getGetFailCount())).append('|');
            sb.append(String.format("%,14d", s.getGetMissCount())).append('|');
            sb.append(String.format("%,14d", s.getGetExpireCount())).append('|');
            sb.append(String.format("%,9.1f", s.avgGetTime())).append('|');
            sb.append(String.format("%,7d", s.getMinGetTime() == Long.MAX_VALUE ? 0L : s.getMinGetTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxGetTime())).append('\n');
            sb.append(String.format("%-10s", "put")).append('|');
            sb.append(String.format("%,10.2f", s.putTps())).append('|');
            sb.append(String.format("%,14d", s.getPutCount())).append('|');
            sb.append(String.format("%,14d", s.getPutSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getPutFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgPutTime())).append('|');
            sb.append(String.format("%,7d", s.getMinPutTime() == Long.MAX_VALUE ? 0L : s.getMinPutTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxPutTime())).append('\n');
            sb.append(String.format("%-10s", "remove")).append('|');
            sb.append(String.format("%,10.2f", s.removeTps())).append('|');
            sb.append(String.format("%,14d", s.getRemoveCount())).append('|');
            sb.append(String.format("%,14d", s.getRemoveSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getRemoveFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgRemoveTime())).append('|');
            sb.append(String.format("%,7d", s.getMinRemoveTime() == Long.MAX_VALUE ? 0L : s.getMinRemoveTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxRemoveTime())).append('\n');
            sb.append(String.format("%-10s", "load")).append('|');
            sb.append(String.format("%,10.2f", s.loadQps())).append('|');
            sb.append(String.format("%,14d", s.getLoadCount())).append('|');
            sb.append(String.format("%,14d", s.getLoadSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getLoadFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgLoadTime())).append('|');
            sb.append(String.format("%,7d", s.getMinLoadTime() == Long.MAX_VALUE ? 0L : s.getMinLoadTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxLoadTime())).append('\n');
        }

        return sb;
    }
}
