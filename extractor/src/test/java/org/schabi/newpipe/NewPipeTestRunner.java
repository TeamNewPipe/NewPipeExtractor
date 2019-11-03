package org.schabi.newpipe;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewPipeTestRunner extends BlockJUnit4ClassRunner {
    private final NewPipeTestRunnerOptions options;

    private List<String> methodsNotFailing;
    private int currentRetry;

    public NewPipeTestRunner(Class testClass) throws InitializationError {
        super(testClass);

        if (testClass.isAnnotationPresent(NewPipeTestRunnerOptions.class)) {
            options = (NewPipeTestRunnerOptions) testClass.getAnnotation(NewPipeTestRunnerOptions.class);
            validateOptions(testClass);
        } else {
            throw new InitializationError("Test class " + testClass.getCanonicalName() +
                    " running with " + NewPipeTestRunner.class.getSimpleName() + " should have the @" +
                    NewPipeTestRunnerOptions.class.getSimpleName() + " annotation");
        }
    }

    private void validateOptions(Class testClass) throws InitializationError {
        if (options.classDelayMs() < 0) {
            throw new InitializationError("classDelayMs value should not be negative in annotation @" +
                    NewPipeTestRunnerOptions.class.getSimpleName() + " in class " + testClass.getCanonicalName());
        }
        if (options.methodDelayMs() < 0) {
            throw new InitializationError("methodDelayMs value should not be negative in annotation @" +
                    NewPipeTestRunnerOptions.class.getSimpleName() + " in class " + testClass.getCanonicalName());
        }
        if (options.retry() < 1) {
            throw new InitializationError("retry value should be bigger than 0 in annotation @" +
                    NewPipeTestRunnerOptions.class.getSimpleName() + " in class " + testClass.getCanonicalName());
        }
    }


    private void sleep(int milliseconds) {
        if (milliseconds > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(milliseconds);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        sleep(options.classDelayMs()); // @see NewPipeTestRunnerOptions.classDelayMs

        methodsNotFailing = new ArrayList<>();
        for (currentRetry = 1; currentRetry <= options.retry(); ++currentRetry) {
            if (getChildren().size() == methodsNotFailing.size()) {
                break;
            }
            super.run(notifier);
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (isMethodAlreadyNotFailing(method)) return;
        Description description = describeChild(method);

        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
            markMethodAsNotFailing(method);

            String ignoreReason = method.getAnnotation(Ignore.class).value();
            System.out.println(method.getName() + "() ignored because of @Ignore" +
                    (ignoreReason.isEmpty() ? "" : ": " + ignoreReason));

        } else {
            sleep(options.methodDelayMs()); // @see NewPipeTestRunnerOptions.methodDelayMs
            Statement statement = methodBlock(method);
            notifier.fireTestStarted(description);

            try {
                statement.evaluate();
                markMethodAsNotFailing(method);

            } catch (Throwable e) {
                if (currentRetry < options.retry() || e instanceof AssumptionViolatedException) {
                    notifier.fireTestAssumptionFailed(new Failure(description, e));
                } else {
                    notifier.fireTestFailure(new Failure(description, e)); // test is not going to be retried anymore
                }

            } finally {
                notifier.fireTestFinished(description);
            }
        }
    }


    private void markMethodAsNotFailing(FrameworkMethod method) {
        methodsNotFailing.add(method.getName());
    }

    private boolean isMethodAlreadyNotFailing(FrameworkMethod method) {
        return methodsNotFailing.contains(method.getName());
    }
}
