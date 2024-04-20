package tools.jackson.databind.util;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedClassResolver;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EnumValuesTest extends DatabindTestUtil
{
    enum ABC {
        A("A"),
        B("b"),
        C("C");

        private final String desc;

        private ABC(String d) { desc = d; }

        @Override
        public String toString() { return desc; }
    }

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testConstructFromName() {
        SerializationConfig cfg = MAPPER.serializationConfig()
                .without(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        AnnotatedClass enumClass = resolve(MAPPER, ABC.class);
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("A", values.serializedValueFor(ABC.A).toString());
        assertEquals("B", values.serializedValueFor(ABC.B).toString());
        assertEquals("C", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

    @Test
    public void testConstructWithToString() {
        SerializationConfig cfg = MAPPER.serializationConfig()
                .with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        AnnotatedClass enumClass = resolve(MAPPER, ABC.class);
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("A", values.serializedValueFor(ABC.A).toString());
        assertEquals("b", values.serializedValueFor(ABC.B).toString());
        assertEquals("C", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

    @Test
    public void testEnumResolverNew()
    {
        AnnotatedClass annotatedClass = resolve(MAPPER, ABC.class);
        EnumResolver enumRes = EnumResolver.constructUsingToString(MAPPER.deserializationConfig(),
               annotatedClass);
        assertEquals(ABC.B, enumRes.getEnum(1));
        assertNull(enumRes.getEnum(-1));
        assertNull(enumRes.getEnum(3));
        assertEquals(2, enumRes.lastValidIndex());
        List<Enum<?>> enums = enumRes.getEnums();
        assertEquals(3, enums.size());
        assertEquals(ABC.A, enums.get(0));
        assertEquals(ABC.B, enums.get(1));
        assertEquals(ABC.C, enums.get(2));
    }

    // [databind#3053]
    @Test
    public void testConstructFromNameLowerCased() {
        SerializationConfig cfg = MAPPER.serializationConfig()
            .with(EnumFeature.WRITE_ENUMS_TO_LOWERCASE);
        AnnotatedClass enumClass = resolve(MAPPER, ABC.class);
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("a", values.serializedValueFor(ABC.A).toString());
        assertEquals("b", values.serializedValueFor(ABC.B).toString());
        assertEquals("c", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

    private AnnotatedClass resolve(ObjectMapper mapper, Class<?> enumClass) {
        return AnnotatedClassResolver.resolve(mapper.serializationConfig(),
                mapper.constructType(enumClass), null);
    }
}
