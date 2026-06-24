package com.vinberts.shrinkly.task;

import com.vinberts.shrinkly.service.ClickReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Periodically reconciles the Postgres {@code clicks} column from the authoritative
 * Redis click counters. The redirect hot path only does an atomic Redis INCR; this
 * scheduled job writes those counts back to the database in batch so click-sorted
 * listings and the home dashboard stay current without a synchronous DB write per
 * redirect. The reconciliation loop is shared with the manual {@code /admin/reindex}
 * endpoint via {@link ClickReconciliationService}.
 */
@Slf4j
@Service
public class ClickCountReconcileTask {

    private final ClickReconciliationService clickReconciliationService;

    public ClickCountReconcileTask(final ClickReconciliationService clickReconciliationService) {
        this.clickReconciliationService = clickReconciliationService;
    }

    @Scheduled(cron = "${clicks.reconcile.cron.expression}")
    public void reconcileClickCounts() {
        clickReconciliationService.reconcileAll();
    }
}
