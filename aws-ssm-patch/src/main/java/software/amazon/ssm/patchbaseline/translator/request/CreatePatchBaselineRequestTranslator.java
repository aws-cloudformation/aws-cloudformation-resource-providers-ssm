//package software.amazon.ssm.patchbaseline.translator.request;
//
//
//import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
//import software.amazon.ssm.patchbaseline.ResourceModel;
//
//public class CreatePatchBaselineRequestTranslator implements RequestTranslator<CreatePatchBaselineRequest>{
//
//    private final SimpleTypeValidator simpleTypeValidator;
//    private final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;
//    private final TargetsListTranslator targetsListTranslator;
//
//    /**
//     * Constructor that initializes all required fields.
//     */
//    public CreatePatchBaselineRequestTranslator() {
//        this.simpleTypeValidator = new SimpleTypeValidator();
//        this.instanceAssociationOutputLocationTranslator = new InstanceAssociationOutputLocationTranslator();
//        this.targetsListTranslator = new TargetsListTranslator();
//    }
//
//    /**
//     * Used for unit tests.
//     *
//     * @param simpleTypeValidator Validator for simple data types.
//     * @param instanceAssociationOutputLocationTranslator PropertyTranslator for InstanceAssociationOutputLocation property.
//     * @param targetsListTranslator PropertyTranslator for Targets List property.
//     */
//    public CreatePatchBaselineRequestTranslator(final SimpleTypeValidator simpleTypeValidator,
//                                       final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator,
//                                       final TargetsListTranslator targetsListTranslator) {
//        this.simpleTypeValidator = simpleTypeValidator;
//        this.instanceAssociationOutputLocationTranslator = instanceAssociationOutputLocationTranslator;
//        this.targetsListTranslator = targetsListTranslator;
//    }
//
//    /**
//     * Converts ResourceModel object into CreateAssociationRequest.
//     *
//     * @param model ResourceModel to convert into a CreateAssociationRequest.
//     * @return CreateAssociationRequest with properties present on the model.
//     */
//    @Override
//    public CreatePatchBaselineRequest resourceModelToRequest(final ResourceModel model) {
//        final CreatePatchBaselineRequest.Builder createPatchBaselineRequestBuilder =
//                CreatePatchBaselineRequest.builder()
//                        .name(model.getName());
//
//        simpleTypeValidator.getValidatedString(model.getAssociationName())
//                .ifPresent(createAssociationRequestBuilder::associationName);
//
//        simpleTypeValidator.getValidatedString(model.getDocumentVersion())
//                .ifPresent(createAssociationRequestBuilder::documentVersion);
//
//        simpleTypeValidator.getValidatedString(model.getInstanceId())
//                .ifPresent(createAssociationRequestBuilder::instanceId);
//
//        simpleTypeValidator.getValidatedMap(model.getParameters())
//                .ifPresent(createAssociationRequestBuilder::parameters);
//
//        simpleTypeValidator.getValidatedString(model.getScheduleExpression())
//                .ifPresent(createAssociationRequestBuilder::scheduleExpression);
//
//        targetsListTranslator.resourceModelPropertyToServiceModel(model.getTargets())
//                .ifPresent(createAssociationRequestBuilder::targets);
//
//        instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(model.getOutputLocation())
//                .ifPresent(createAssociationRequestBuilder::outputLocation);
//
//        simpleTypeValidator.getValidatedString(model.getAutomationTargetParameterName())
//                .ifPresent(createAssociationRequestBuilder::automationTargetParameterName);
//
//        simpleTypeValidator.getValidatedString(model.getMaxErrors())
//                .ifPresent(createAssociationRequestBuilder::maxErrors);
//
//        simpleTypeValidator.getValidatedString(model.getMaxConcurrency())
//                .ifPresent(createAssociationRequestBuilder::maxConcurrency);
//
//        simpleTypeValidator.getValidatedString(model.getComplianceSeverity())
//                .ifPresent(createAssociationRequestBuilder::complianceSeverity);
//
//        return createAssociationRequestBuilder.build();
//    }
//
//    return CreatePatchBaselineRequest.builder()
//            .withClientToken(request.getClientRequestToken())
//            .withApprovalRules(model.getApprovalRules())
//            .withApprovedPatches(model.getApprovedPatches())
//            .withApprovedPatchesComplianceLevel(model.getApprovedPatchesComplianceLevel())
//            .withApprovedPatchesEnableNonSecurity(model.getApprovedPatchesEnableNonSecurity())
//            .withGlobalFilters(model.getGlobalFilters())
//            .withOperatingSystem(model.getOperatingSystem())
//            .withRejectedPatches(model.getRejectedPatches())
//            .withRejectedPatchesAction(model.getRejectedPatchesAction())
//            .withTags(model.getTags())
//            .withName(model.getName())
//            .withSources(model.getSources())
//            .withDescription(model.getDescription())
//            .build();
//}
