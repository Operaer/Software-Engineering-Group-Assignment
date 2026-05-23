package com.bupt.ta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;

import javax.servlet.ServletContext;

/**
 * Utility class providing test helpers for TA recruitment unit tests.
 */
public final class TestUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TestUtils() {
    }

    /**
     * Creates a mock ServletContext backed by a temporary directory path.
     *
     * @param root the temporary directory path used to resolve real paths.
     * @return a proxy-based ServletContext implementation for testing.
     */
    public static ServletContext createServletContext(Path root) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("getRealPath".equals(method.getName()) && args != null && args.length == 1) {
                String path = (String) args[0];
                if (path == null) {
                    return null;
                }
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                return root.resolve(path).toString();
            }
            Class<?> returnType = method.getReturnType();
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == float.class) {
                return 0f;
            }
            if (returnType == double.class) {
                return 0d;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return null;
        };

        return (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(),
                new Class[]{ServletContext.class},
                handler
        );
    }
}
