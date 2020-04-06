//package software.amazon.ssm.patchbaseline.mapper;
//
//import com.fasterxml.jackson.databind.cfg.MapperConfig;
//import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
//import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
//
///**
// * The sdk has two setters for "enum" values: one receives Enum, the other String. Jackson can't figure out which one
// * to use, this introspector selects the String setter.
// */
//class AwsSdkIntrospector extends NopAnnotationIntrospector {
//
//    /**
//     * Figures out which setter to use when two are available.
//     *
//     * @param config  not used
//     * @param setter1 to choose from
//     * @param setter2 to choose from
//     * @return the setter to use
//     */
//    @Override
//    public AnnotatedMethod resolveSetterConflict(MapperConfig<?> config, AnnotatedMethod setter1, AnnotatedMethod setter2) {
//        AnnotatedMethod stringSetter = findStringSetter(setter1, setter2);
//        AnnotatedMethod enumSetter = findEnumSetter(setter1, setter2);
//        if (stringSetter != null && enumSetter != null) {
//            return stringSetter;
//        }
//        return super.resolveSetterConflict(config, setter1, setter2);
//    }
//
//    private AnnotatedMethod findStringSetter(AnnotatedMethod... setters) {
//        for (AnnotatedMethod am : setters) {
//            if (am.getParameterCount() == 1
//                    && am.getParameterType(0).getRawClass().equals(String.class)) {
//                return am;
//            }
//        }
//        return null;
//    }
//
//    private AnnotatedMethod findEnumSetter(AnnotatedMethod... setters) {
//        for (AnnotatedMethod am : setters) {
//            if (am.getParameterCount() == 1
//                    && am.getParameterType(0).isEnumType()) {
//                return am;
//            }
//        }
//        return null;
//    }
//}
//
