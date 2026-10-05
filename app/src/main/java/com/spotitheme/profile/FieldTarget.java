package com.spotitheme.profile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** A pinned field identity, including type and static/instance ownership. */
public final class FieldTarget {
    private final String owner;
    private final String name;
    private final String type;
    private final boolean isStatic;

    public FieldTarget(String owner, String name, String type, boolean isStatic) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
    }

    public Field resolve(ClassLoader loader) throws ReflectiveOperationException {
        Field field = Class.forName(owner, false, loader).getDeclaredField(name);
        if (!field.getType().getName().equals(type) || Modifier.isStatic(field.getModifiers()) != isStatic) {
            throw new NoSuchFieldException("Field shape differs from pinned profile: " + owner + "." + name);
        }
        field.setAccessible(true);
        return field;
    }
}
