package dev.securemask;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface MaskField {
    /** Roles allowed to see the unmasked value. Empty means no role can see it. */
    String[] revealTo() default {};
    MaskType type() default MaskType.FULL;
}
