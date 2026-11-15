package dev.securemask;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import java.util.Arrays;
import java.util.Set;

final class MaskingPropertyWriter extends BeanPropertyWriter {
    private final MaskField policy;
    private final RoleResolver resolver;

    MaskingPropertyWriter(BeanPropertyWriter source, MaskField policy, RoleResolver resolver) {
        super(source);
        this.policy = policy;
        this.resolver = resolver;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        // Resolve trusted roles for each serialization, not at mapper construction.
        Set<String> roles = resolver.currentRoles();
        if (roles == null) roles = Set.of();
        boolean permitted = Arrays.stream(policy.revealTo()).anyMatch(roles::contains);
        if (permitted) {
            super.serializeAsField(bean, gen, prov);
            return;
        }
        gen.writeFieldName(getName());
        if (policy.type() == MaskType.FULL) {
            // No need to access the sensitive value for full masking.
            gen.writeString("********");
        } else {
            Object raw = get(bean);
            if (raw == null) gen.writeNull();
            else if (raw instanceof String s) gen.writeString(Masking.apply(s, policy.type()));
            else throw new IllegalStateException("Partial masking supports String fields only: " + getName());
        }
    }
}
