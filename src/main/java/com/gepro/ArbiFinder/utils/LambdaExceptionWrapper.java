package com.gepro.ArbiFinder.utils;

import com.gepro.ArbiFinder.utils.ThrowingBiFunction;

import java.util.function.BiFunction;

public class LambdaExceptionWrapper {
    static <T, U, R, E extends Exception> BiFunction<T, U, R> handlingBiFunctionWrapper(
            ThrowingBiFunction<T, U, R, E> throwingBiFunction, Class<E> exceptionClass) {

        return (t, u) -> {
            try {
                return throwingBiFunction.apply(t, u);
            } catch (Exception ex) {
                try {
                    E exCast = exceptionClass.cast(ex);
                    System.err.println(
                            "Exception occured : " + exCast.getMessage());
                } catch (ClassCastException ccEx) {
                    throw new RuntimeException(ex);
                }
            }
            return null;
        };
    }
}
