package dev.securemask;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.List;

public final class SecureMaskModule extends SimpleModule {
    public SecureMaskModule(RoleResolver resolver) {
        super("SecureMask");
        if (resolver == null) throw new IllegalArgumentException("RoleResolver is required");
        setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription description, List<BeanPropertyWriter> properties) {
                for (int i = 0; i < properties.size(); i++) {
                    BeanPropertyWriter writer = properties.get(i);
                    MaskField annotation = writer.getAnnotation(MaskField.class);
                    if (annotation != null) properties.set(i, new MaskingPropertyWriter(writer, annotation, resolver));
                }
                return properties;
            }
        });
    }
}
