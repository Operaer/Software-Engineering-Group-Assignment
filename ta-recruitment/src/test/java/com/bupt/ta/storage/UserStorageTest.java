package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link UserStorage} persistence layer.
 */
class UserStorageTest {

    private Path tempRoot;
    private ServletContext servletContext;
    private UserStorage userStorage;

    /**
     * Sets up a temporary directory and initializes UserStorage before each test.
     *
     * @throws IOException if the temporary directory cannot be created.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("ta-recruitment-test");
        servletContext = createServletContext(tempRoot);
        userStorage = new UserStorage(servletContext);
    }

    /**
     * Cleans up the temporary directory after each test.
     *
     * @throws IOException if file cleanup fails.
     */
    @AfterEach
    void tearDown() throws IOException {
        if (tempRoot != null && Files.exists(tempRoot)) {
            Files.walk(tempRoot)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Tests creating a user and finding them by email address.
     *
     * @throws IOException if file operations fail.
     */
    @Test
    void createAndFindUser() throws IOException {
        User user = new User("tester", "tester@example.com", "pass123", User.Role.TA, true);
        userStorage.createUser(user);

        User found = userStorage.findByEmail("tester@example.com");
        assertNotNull(found);
        assertEquals("tester", found.getUsername());
        assertTrue(found.passwordMatches("pass123"));
    }

    /**
     * Tests updating user fields and verifying the changes are persisted.
     *
     * @throws IOException if file operations fail.
     */
    @Test
    void updateUserPersistsChanges() throws IOException {
        User user = new User("tester", "tester@example.com", "pass123", User.Role.TA, true);
        userStorage.createUser(user);

        user.setPassword("newpass");
        user.setActive(false);
        userStorage.updateUser(user);

        User found = userStorage.findByEmail("tester@example.com");
        assertNotNull(found);
        assertFalse(found.isActive());
        assertTrue(found.passwordMatches("newpass"));
    }

    /**
     * Tests that creating a user with a duplicate email address throws an exception.
     *
     * @throws IOException if file operations fail.
     */
    @Test
    void createUserWithDuplicateEmailThrows() throws IOException {
        User user = new User("tester", "tester@example.com", "pass123", User.Role.TA, true);
        userStorage.createUser(user);

        User duplicate = new User("tester2", "tester@example.com", "pass123", User.Role.TA, true);
        assertThrows(IllegalArgumentException.class, () -> userStorage.createUser(duplicate));
    }

    /**
     * Creates a mock ServletContext using a dynamic proxy for testing purposes.
     *
     * @param root the temporary directory path for resolving real paths.
     * @return a proxy-based ServletContext implementation.
     */
    private static ServletContext createServletContext(Path root) {
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
            return defaultValue(returnType);
        };

        return (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(),
                new Class[]{ServletContext.class},
                handler
        );
    }

    /**
     * Returns the default value for primitive types, or null for object types.
     *
     * @param returnType the class representing the return type.
     * @return the default value for the given type.
     */
    private static Object defaultValue(Class<?> returnType) {
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
    }
}
