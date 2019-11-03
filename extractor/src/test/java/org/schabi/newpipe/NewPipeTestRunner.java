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

public class NewPipeTestRunner extends BlockJUnit4ClassRunner {
    public NewPipeTestRunner(Class testClass) throws InitializationError {
        super(testClass);

        if (testClass.isAnnotationPresent(NewPipeTestRunnerOptions.class)) {
            NewPipeTestRunnerOptions options = (NewPipeTestRunnerOptions) testClass.getAnnotation(NewPipeTestRunnerOptions.class);
        } else {
            throw new IllegalArgumentException("Test classes running with " + NewPipeTestRunner.class.getName() + " should also have @NewPipeTestRunnerOptions");
        }
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
