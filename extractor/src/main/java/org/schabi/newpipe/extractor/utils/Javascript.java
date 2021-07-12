package org.schabi.newpipe.extractor.utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class Javascript {

    public String run(String function, String functionName, String... parameters) {
        try {
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            ScriptableObject scope = context.initSafeStandardObjects();

            context.evaluateString(scope, function, functionName, 1, null);
            Function jsFunction = (Function) scope.get(functionName, scope);
            Object result = jsFunction.call(context, scope, scope, parameters);
            return result.toString();
        } finally {
            Context.exit();
        }
    }

}
