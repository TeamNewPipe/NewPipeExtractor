package org.schabi.newpipe;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestTimedOutException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NewPipeTestRunner extends Runner {

    private class TestException extends Exception {
        TestException(Throwable throwable) {
            super(throwable);
        }
    }

    
    private class MethodResultCollector {
        boolean ignoredByUser;
        String methodName;
        Throwable thrownException;

        MethodResultCollector(Method method, Object testClassInstance) throws IllegalAccessException {
            ignoredByUser = method.isAnnotationPresent(Ignore.class);
            methodName = method.getName();
            thrownException = null;
            if (ignoredByUser) return;

            Test testAnnotation = method.getAnnotation(Test.class);
            Class expectedThrowable = testAnnotation.expected();
            long timeout = testAnnotation.timeout();

            try {
                invokeAllMethods(beforeMethods, testClassInstance);
                invokeCheckingTimeAndExceptions(method, testClassInstance, timeout, expectedThrowable);
                invokeAllMethods(afterMethods, testClassInstance);
            } catch (InvocationTargetException e) {
                thrownException = e.getCause();
            } catch (TestException e) {
                thrownException = e.getCause();
            }
        }


        boolean isIgnoredByUser() {
            return ignoredByUser;
        }

        boolean isSuccessful() {
            return thrownException == null;
        }


        void showResults(RunNotifier notifier, boolean ignoreAnyError) {
            Description methodDescription = Description.createTestDescription(testClass, methodName);

            if (ignoredByUser) {
                notifier.fireTestIgnored(methodDescription);
                System.out.println(methodName + "() ignored because of @Ignore");

            } else {
                if (thrownException == null) {
                    notifier.fireTestStarted(methodDescription);
                    notifier.fireTestFinished(methodDescription);

                } else if (thrownException instanceof ReCaptchaException) {
                    notifier.fireTestIgnored(methodDescription);
                    System.out.println(methodName + "() ignored since it threw a ReCaptchaException");
                    thrownException.printStackTrace(System.out);

                } else if (ignoreAnyError) {
                    notifier.fireTestIgnored(methodDescription);
                    System.out.println(methodName + "() ignored since the whole class " + testClass.getName() +
                            " has more than " + (int)(failRatioTriggeringIgnore*100) + "% of failed tests");
                    thrownException.printStackTrace(System.out);

                } else {
                    notifier.fireTestStarted(methodDescription);
                    notifier.fireTestFailure(new Failure(methodDescription, thrownException));
                    notifier.fireTestFinished(methodDescription);

                }
            }
        }

    }


    private Class testClass;
    private float failRatioTriggeringIgnore;
    private final ArrayList<Method> testMethods,
            beforeMethods, afterMethods,
            beforeClassMethods, afterClassMethods;

    public NewPipeTestRunner(Class testClass) throws InitializationError {
        this.testClass = testClass;
        obtainRunnerOptions();

        testMethods = new ArrayList<>();
        beforeMethods = new ArrayList<>();
        afterMethods = new ArrayList<>();
        beforeClassMethods = new ArrayList<>();
        afterClassMethods = new ArrayList<>();

        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                validatePublicVoidNoArg(method, false);
                testMethods.add(method);

            } else if (method.isAnnotationPresent(Before.class)) {
                validatePublicVoidNoArg(method, false);
                beforeMethods.add(method);

            } else if (method.isAnnotationPresent(After.class)) {
                validatePublicVoidNoArg(method, false);
                afterMethods.add(method);

            } else if (method.isAnnotationPresent(BeforeClass.class)) {
                validatePublicVoidNoArg(method, true);
                beforeClassMethods.add(method);

            } else if (method.isAnnotationPresent(AfterClass.class)) {
                validatePublicVoidNoArg(method, true);
                afterClassMethods.add(method);

            }
        }
    }


    @Override
    public Description getDescription() {
        return Description.createTestDescription(testClass, "NewPipe custom runner");
    }


    private static void validatePublicVoidNoArg(Method method, boolean shouldBeStatic) throws InitializationError {
        List<Throwable> errors = new ArrayList<>();

        if (method.getParameterTypes().length != 0) {
            errors.add(new Exception("Method " + method.getName() + " should have no parameters"));
        }
        if (Modifier.isStatic(method.getModifiers()) != shouldBeStatic) {
            String state = shouldBeStatic ? "should" : "should not";
            errors.add(new Exception("Method " + method.getName() + "() " + state + " be static"));
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            errors.add(new Exception("Method " + method.getName() + "() should be public"));
        }
        if (method.getReturnType() != Void.TYPE) {
            errors.add(new Exception("Method " + method.getName() + "() should be void"));
        }

        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
    }

    private static void invokeAllMethods(List<Method> methods, Object testClassInstance) throws InvocationTargetException, IllegalAccessException {
        for (Method method : methods) {
            method.invoke(testClassInstance);
        }
    }

    abstract class RunnableCollectingThrowables implements Runnable {
        Throwable collectedThrowable;

        abstract void runWithExceptions() throws Throwable;

        @Override
        public final void run() {
            try {
                runWithExceptions();
            } catch (Throwable e) {
                collectedThrowable = e;
            }
        }

        void throwCollectedThrowableIfPresent() throws Throwable {
            if (collectedThrowable != null) {
                throw collectedThrowable;
            }
        }
    }

    private void invokeWithTimeout(final Method method, final Object testClassInstance, long timeout) throws InvocationTargetException, IllegalAccessException, TestException {
        if (timeout > 0) {
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            RunnableCollectingThrowables runnable = new RunnableCollectingThrowables() {
                @Override
                void runWithExceptions() throws Throwable {
                    method.invoke(testClassInstance);
                }
            };

            try {
                final Future<Object> f = (Future<Object>) executorService.submit(runnable);
                f.get(timeout, TimeUnit.MILLISECONDS);
                runnable.throwCollectedThrowableIfPresent();
            } catch (final TimeoutException e) {
                throw new TestException(new TestTimedOutException(timeout, TimeUnit.MILLISECONDS));
            } catch (Throwable e) {
                throw new TestException(e);
            } finally {
                executorService.shutdown();
            }
        } else {
            method.invoke(testClassInstance);
        }
    }

    private void invokeCheckingTimeAndExceptions(Method method, Object testClassInstance, long timeout, Class expectedThrowable) throws InvocationTargetException, IllegalAccessException, TestException {
        boolean complete = false;
        try {
            invokeWithTimeout(method, testClassInstance, timeout);
            complete = true;
        } catch (InvocationTargetException e) {
            if (expectedThrowable.equals(Test.None.class)) {
                throw e;
            } else if (!expectedThrowable.isInstance(e.getCause())) {
                String message = "Unexpected exception, expected <" + expectedThrowable.getName() +
                        "> but was <" + e.getCause().getClass().getName() + ">";
                throw new TestException(new Exception(message, e));
            }
        }

        if (complete && !expectedThrowable.equals(Test.None.class)) {
            String message = "Expected exception <" + expectedThrowable.getName() + ">";
            throw new TestException(new Exception(message));
        }
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
        try {
            Object testClassInstance = testClass.newInstance();
            invokeAllMethods(beforeClassMethods, testClassInstance);

            List<MethodResultCollector> savedResults = new ArrayList<>();
            for (Method testMethod : testMethods) {
                savedResults.add(new MethodResultCollector(testMethod, testClassInstance));
            }

            int nrSuccessful = 0, nrNotIgnoredByUser = 0;
            for (MethodResultCollector savedResult : savedResults) {
                if (!savedResult.isIgnoredByUser()) {
                    ++nrNotIgnoredByUser;
                    if (savedResult.isSuccessful()) {
                        ++nrSuccessful;
                    }
                }
            }

            for (MethodResultCollector savedResult : savedResults) {
                savedResult.showResults(notifier, (float)(nrNotIgnoredByUser - nrSuccessful) / nrNotIgnoredByUser > failRatioTriggeringIgnore);
            }

            invokeAllMethods(afterClassMethods, testClassInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
