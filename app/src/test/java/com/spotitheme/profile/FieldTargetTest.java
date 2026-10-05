package com.spotitheme.profile;

import org.junit.Test;
import static org.junit.Assert.*;

public class FieldTargetTest {
    public static final class Fixture {
        private int instance = 7;
        private static String shared = "value";
    }

    @Test public void resolvesPrivateInstanceAndStaticFields() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        assertEquals(7, new FieldTarget(Fixture.class.getName(), "instance", "int", false)
                .resolve(loader).getInt(new Fixture()));
        assertEquals("value", new FieldTarget(Fixture.class.getName(), "shared", "java.lang.String", true)
                .resolve(loader).get(null));
    }

    @Test public void rejectsTypeAndOwnershipChanges() {
        ClassLoader loader = getClass().getClassLoader();
        assertThrows(NoSuchFieldException.class, () -> new FieldTarget(Fixture.class.getName(), "instance", "long", false).resolve(loader));
        assertThrows(NoSuchFieldException.class, () -> new FieldTarget(Fixture.class.getName(), "instance", "int", true).resolve(loader));
        assertThrows(NoSuchFieldException.class, () -> new FieldTarget(Fixture.class.getName(), "missing", "int", false).resolve(loader));
    }
}
