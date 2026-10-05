package com.spotitheme.profile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Exact APK method identity; resolving it never enumerates classes or methods. */
public final class MethodTarget {
    private final String descriptor;
    private final String owner;
    private final String name;
    private final List<String> parameters;
    private final String returnType;

    public MethodTarget(String descriptor) {
        this.descriptor = descriptor;
        int arrow = descriptor.indexOf("->");
        int open = descriptor.indexOf('(', arrow + 2);
        int close = descriptor.indexOf(')', open + 1);
        if (arrow < 1 || open < arrow + 3 || close < open
                || !descriptor.startsWith("L") || descriptor.charAt(arrow - 1) != ';') {
            throw new IllegalArgumentException("Invalid method descriptor: " + descriptor);
        }
        owner = descriptor.substring(1, arrow - 1).replace('/', '.');
        name = descriptor.substring(arrow + 2, open);
        parameters = new ArrayList<>();
        int cursor = open + 1;
        while (cursor < close) {
            int end = typeEnd(descriptor, cursor, close, false);
            parameters.add(descriptor.substring(cursor, end));
            cursor = end;
        }
        int end = typeEnd(descriptor, close + 1, descriptor.length(), true);
        if (end != descriptor.length()) throw new IllegalArgumentException("Trailing descriptor data");
        returnType = descriptor.substring(close + 1);
        if (name.equals("<clinit>") || (name.equals("<init>") && !returnType.equals("V"))) {
            throw new IllegalArgumentException("Not a reflectable executable: " + descriptor);
        }
    }

    private static int typeEnd(String value, int start, int limit, boolean allowVoid) {
        int cursor = start;
        while (cursor < limit && value.charAt(cursor) == '[') cursor++;
        if (cursor >= limit) throw new IllegalArgumentException("Missing descriptor type");
        char type = value.charAt(cursor);
        if (type == 'L') {
            int end = value.indexOf(';', cursor + 1);
            if (end <= cursor + 1 || end >= limit) throw new IllegalArgumentException("Invalid object type");
            return end + 1;
        }
        if ("ZBCSIJFD".indexOf(type) >= 0 || (allowVoid && cursor == start && type == 'V')) return cursor + 1;
        throw new IllegalArgumentException("Invalid descriptor type: " + type);
    }

    private static Class<?> type(String descriptor, ClassLoader loader) throws ClassNotFoundException {
        return switch (descriptor) {
            case "V" -> void.class;
            case "Z" -> boolean.class;
            case "B" -> byte.class;
            case "C" -> char.class;
            case "S" -> short.class;
            case "I" -> int.class;
            case "J" -> long.class;
            case "F" -> float.class;
            case "D" -> double.class;
            default -> Class.forName(descriptor.charAt(0) == '[' ? descriptor.replace('/', '.')
                    : descriptor.substring(1, descriptor.length() - 1).replace('/', '.'), false, loader);
        };
    }

    public Executable resolve(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> declaringClass = Class.forName(owner, false, loader);
        Class<?>[] types = new Class<?>[parameters.size()];
        for (int i = 0; i < types.length; i++) types[i] = type(parameters.get(i), loader);
        if (name.equals("<init>")) {
            Constructor<?> constructor = declaringClass.getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor;
        }
        Method method = declaringClass.getDeclaredMethod(name, types);
        if (method.getReturnType() != type(returnType, loader)) {
            throw new NoSuchMethodException("Return type differs from pinned profile: " + descriptor);
        }
        method.setAccessible(true);
        return method;
    }

    public String descriptor() { return descriptor; }
}
