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
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.util.concurrent.TimeUnit;

public class NewPipeTestRunner extends BlockJUnit4ClassRunner {
    private final NewPipeTestRunnerOptions options;

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
        super.run(notifier);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);

        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);

            String ignoreReason = method.getAnnotation(Ignore.class).value();
            System.out.println(method.getName() + "() ignored because of @Ignore" +
                    (ignoreReason.isEmpty() ? "" : ": " + ignoreReason));

        } else {
            sleep(options.methodDelayMs()); // @see NewPipeTestRunnerOptions.methodDelayMs
            Statement statement = methodBlock(method);
            notifier.fireTestStarted(description);

            try {
                statement.evaluate();
            } catch (AssumptionViolatedException | ReCaptchaException e) {
                notifier.fireTestAssumptionFailed(new Failure(description, e));

                if (e instanceof ReCaptchaException) {
                    System.out.println(method.getName() + "() ignored since it threw a ReCaptchaException");
                }

            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            } finally {
                notifier.fireTestFinished(description);
            }
        }
    }
}
