package org.schabi.newpipe.downloader;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @see MockOnly
 */
public class MockOnlyCondition implements ExecutionCondition {
    private static final String MOCK_ONLY_REASON = "Mock only";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        if (DownloaderFactory.getDownloaderType() == DownloaderType.REAL) {
            return ConditionEvaluationResult.disabled(MOCK_ONLY_REASON);
        } else {
            return ConditionEvaluationResult.enabled(MOCK_ONLY_REASON);
        }
    }
}
