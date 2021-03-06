package com.gepro.ArbiFinder.utils;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Exception> {
    R apply(T t, U u) throws E;
}
