package com.spotitheme.profile;

import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class MethodTargetTest {
    public static final class Fixture {
        public Fixture(int value) {}
        private static String choose(int[] values, String[][] names) { return "array"; }
        private static int choose(int value) { return value; }
    }

    private String owner() {
        return "L" + Fixture.class.getName().replace('.', '/') + ";->";
    }

    @Test public void resolvesExactOverloadAndArrayTypes() throws Exception {
        Method method = (Method) new MethodTarget(owner() + "choose([I[[Ljava/lang/String;)Ljava/lang/String;")
                .resolve(getClass().getClassLoader());
        assertEquals("array", method.invoke(null, new int[]{1}, new String[][]{{"a"}}));
        assertEquals(3, ((Method) new MethodTarget(owner() + "choose(I)I")
                .resolve(getClass().getClassLoader())).invoke(null, 3));
    }

    @Test public void resolvesConstructor() throws Exception {
        assertEquals(Fixture.class, new MethodTarget(owner() + "<init>(I)V")
                .resolve(getClass().getClassLoader()).getDeclaringClass());
    }

    @Test public void rejectsChangedReturnType() throws Exception {
        assertThrows(NoSuchMethodException.class, () -> new MethodTarget(owner() + "choose(I)J")
                .resolve(getClass().getClassLoader()));
    }

    @Test public void rejectsMalformedAndNonReflectableDescriptors() {
        for (String value : new String[]{"broken", owner() + "choose(V)I", owner() + "choose([V)I",
                owner() + "choose(I)IX", owner() + "choose(L;)I", owner() + "<clinit>()V",
                owner() + "<init>()I", owner() + "choose(I)"}) {
            assertThrows(value, IllegalArgumentException.class, () -> new MethodTarget(value));
        }
    }

    @Test public void gatesBothVersionIdentifiers() {
        assertTrue(Profile_9_1_86_2432.supports("9.1.86.2432", 146555520));
        assertFalse(Profile_9_1_86_2432.supports("9.1.86.2432", 1));
        assertFalse(Profile_9_1_86_2432.supports("9.1.84.2231", 146555520));
        assertFalse(Profile_9_1_86_2432.supports(null, 146555520));
        assertThrows(IllegalArgumentException.class, () -> new Profile_9_1_86_2432(
                "9.1.84.2231", 146555520, getClass().getClassLoader()));
    }

    @Test public void unknownRolesCannotTriggerFallbackDiscovery() {
        Profile_9_1_86_2432 profile = new Profile_9_1_86_2432("9.1.86.2432", 146555520,
                getClass().getClassLoader());
        assertThrows(IllegalArgumentException.class, () -> profile.resolve("unknown"));
        assertThrows(UnsupportedOperationException.class, () -> Profile_9_1_86_2432.targets().clear());
    }
}
