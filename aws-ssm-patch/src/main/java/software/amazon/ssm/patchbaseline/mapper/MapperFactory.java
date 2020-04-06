//package software.amazon.ssm.patchbaseline.mapper;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//
///**
// * Build mappers that will successfully map from a cloudformation template to sdk request objects.
// */
//public class MapperFactory {
//
//    private MapperFactory() {
//    }
//
//    /**
//     * Mapper that will map from json to request objects.
//     *
//     * @return mapper
//     */
//    public static ObjectMapper buildJsonMapper() {
//        return configureMapper(new ObjectMapper());
//    }
//
//    private static ObjectMapper configureMapper(ObjectMapper om) {
//        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        om.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
//        om.setAnnotationIntrospector(new AwsSdkIntrospector());
//        return om;
//    }
//
//}
