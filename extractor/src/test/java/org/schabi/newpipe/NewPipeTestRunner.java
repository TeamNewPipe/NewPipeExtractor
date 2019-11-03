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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NewPipeTestRunner extends BlockJUnit4ClassRunner {
    
    private class MethodResultCollector {
        @Nullable String ignoredByUserReason;
        Description description;

        @Nullable AssumptionViolatedException failedAssumption;
        @Nullable Throwable thrownException;

        MethodResultCollector(Description description, @Nonnull String ignoredByUserReason) {
            this.description = description;
            this.ignoredByUserReason = ignoredByUserReason;
        }

        MethodResultCollector(Description description, Statement statement) {
            this.description = description;
            this.ignoredByUserReason = null; // not ignored

            try {
                statement.evaluate();
            } catch (AssumptionViolatedException e) {
                failedAssumption = e;
            } catch (Throwable e) {
                thrownException = e;
            }
        }


        boolean isIgnoredByUser() {
            return ignoredByUserReason != null;
        }

        boolean isSuccessful() {
            return thrownException == null;
        }


        void showResults(RunNotifier notifier, boolean ignoreAnyError) {
            if (ignoredByUserReason == null) { // not ignored
                if (thrownException == null && failedAssumption == null) {
                    notifier.fireTestStarted(description);
                    notifier.fireTestFinished(description);

                } else if (thrownException instanceof ReCaptchaException) {
                    notifier.fireTestIgnored(description);
                    System.out.println(description.getMethodName() + "() ignored since it threw a ReCaptchaException");
                    thrownException.printStackTrace(System.out);

                } else if (ignoreAnyError) {
                    notifier.fireTestIgnored(description);
                    System.out.println(description.getMethodName() + "() ignored since the whole class " + testClass.getName() +
                            " has more than " + (int)(failRatioTriggeringIgnore*100) + "% of failed tests");
                    thrownException.printStackTrace(System.out);

                } else {
                    notifier.fireTestStarted(description);
                    if (thrownException == null) {
                        notifier.fireTestAssumptionFailed(new Failure(description, failedAssumption));
                    } else {
                        notifier.fireTestFailure(new Failure(description, thrownException));
                    }
                    notifier.fireTestFinished(description);
                }
            } else {
                notifier.fireTestIgnored(description);
                System.out.println(description.getMethodName() + "() ignored because of @Ignore" +
                        (ignoredByUserReason.isEmpty() ? "" : ": " + ignoredByUserReason));
            }
        }

    }


    private final Class testClass;
    private float failRatioTriggeringIgnore;
    private List<MethodResultCollector> results;

    public NewPipeTestRunner(Class testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        obtainRunnerOptions();
    }

    private void obtainRunnerOptions() {
        if (testClass.isAnnotationPresent(NewPipeTestRunnerOptions.class)) {
            NewPipeTestRunnerOptions options = (NewPipeTestRunnerOptions) testClass.getAnnotation(NewPipeTestRunnerOptions.class);

            failRatioTriggeringIgnore = options.failRatioTriggeringIgnore();
        } else {
            throw new IllegalArgumentException("Test classes running with " + NewPipeTestRunner.class.getName() + " should also have @NewPipeTestRunnerOptions");
        }
    }


    @Override
    public void run(RunNotifier notifier) {
        results = new ArrayList<>();
        super.run(notifier);

        int nrSuccessful = 0, nrNotIgnoredByUser = 0;
        for (MethodResultCollector result : results) {
            if (!result.isIgnoredByUser()) {
                ++nrNotIgnoredByUser;
                if (result.isSuccessful()) {
                    ++nrSuccessful;
                }
            }
        }

        boolean ignoreAnyError;
        if (nrNotIgnoredByUser <= 1) {
            ignoreAnyError = false;
        } else {
            ignoreAnyError = ((float)(nrNotIgnoredByUser - nrSuccessful) / nrNotIgnoredByUser) > failRatioTriggeringIgnore;
        }

        for (MethodResultCollector result : results) {
            result.showResults(notifier, ignoreAnyError);
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (isIgnored(method)) {
            results.add(new MethodResultCollector(description, method.getAnnotation(Ignore.class).value()));
        } else {
            Statement statement = methodBlock(method);
            results.add(new MethodResultCollector(description, statement));
        }
    }
}
