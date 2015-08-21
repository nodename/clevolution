package clevolution;

import clojure.lang.RT;
import clojure.lang.Var;
import clojure.lang.Compiler;

/**
 * Created by alan on 8/20/15.
 */
public class ClassPatch {
    public static void pushClassLoader() {
        Var.pushThreadBindings(RT.map(new Object[]{Compiler.LOADER, RT.makeClassLoader()}));
    }
}
