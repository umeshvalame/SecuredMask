package dev.securemask;
import java.util.Set;
@FunctionalInterface
public interface RoleResolver {
    /** Must return trusted roles derived from the current authenticated principal. */
    Set<String> currentRoles();
}
