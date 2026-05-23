package com.bupt.ta.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bupt.ta.TestUtils;
import com.bupt.ta.model.TAProfile;

/**
 * Unit tests for the {@link ProfileStorage} persistence layer.
 */
class ProfileStorageTest {
    private Path tempRoot;
    private ServletContext servletContext;
    private ProfileStorage profileStorage;

    /**
     * Sets up a temporary directory and initializes ProfileStorage before each test.
     *
     * @throws IOException if the temporary directory cannot be created.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("profile-storage-test");
        servletContext = TestUtils.createServletContext(tempRoot);
        profileStorage = new ProfileStorage(servletContext);
    }

    /**
     * Cleans up the temporary directory after each test.
     *
     * @throws IOException if file cleanup fails.
     */
    @AfterEach
    void tearDown() throws IOException {
        if (tempRoot != null) {
            Files.walk(tempRoot)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Tests saving a TA profile and loading it back by email address.
     */
    @Test
    void saveAndLoadProfileByEmail() {
        TAProfile profile = new TAProfile("ta@example.com");
        profile.setName("Alice");
        profile.setMajor("CS");
        profile.setGpa(3.8);
        profileStorage.save(profile);

        TAProfile loaded = profileStorage.load("ta@example.com");
        assertNotNull(loaded);
        assertEquals("Alice", loaded.getName());
        assertEquals("CS", loaded.getMajor());
        assertEquals(3.8, loaded.getGpa());
    }

    /**
     * Tests that saving a null profile is safely ignored.
     */
    @Test
    void savingNullProfileIsIgnored() {
        profileStorage.save(null);
        assertNull(profileStorage.load("missing@example.com"));
    }

    /**
     * Tests that saving a profile without an email is safely ignored.
     */
    @Test
    void savingProfileWithoutEmailIsIgnored() {
        TAProfile profile = new TAProfile();
        profile.setName("NoEmail");
        profileStorage.save(profile);
        assertNull(profileStorage.load(null));
    }

    /**
     * Tests that loading a profile for an unknown email returns null.
     */
    @Test
    void loadReturnsNullForUnknownEmail() {
        assertNull(profileStorage.load("unknown@example.com"));
        assertNull(profileStorage.load(null));
    }
}
