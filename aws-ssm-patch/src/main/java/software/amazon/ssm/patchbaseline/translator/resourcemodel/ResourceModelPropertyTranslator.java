package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Translate properties from Response to Resource Model
 * Translate properties from Resource Model to Response
 */

public class ResourceModelPropertyTranslator {

    /**
     * Translate Resource Model Tags to Request Tags
     */
    public static Optional<List<Tag>> translateToRequestTags(final List<software.amazon.ssm.patchbaseline.Tag> resourceModelTags) {

        if (!CollectionUtils.isNullOrEmpty(resourceModelTags)) {
            List<Tag> requestTags = resourceModelTags.stream()
                    .collect(Collectors.mapping(entry ->
                                    Tag.builder()
                                            .key(entry.getKey())
                                            .value(entry.getValue())
                                            .build(),
                            Collectors.toList()));
            return Optional.of(requestTags);
        }
        return Optional.empty();
    }

    /**
     * Translate Request Tags to Resource Model Tags
     */
    public  static Optional<List<software.amazon.ssm.patchbaseline.Tag>> translateToResourceModelTags(final List<Tag> requestTags) {

        if (!CollectionUtils.isNullOrEmpty(requestTags)) {
            List<software.amazon.ssm.patchbaseline.Tag> resourceModelTags = requestTags.stream()
                    .collect(Collectors.mapping(entry ->
                                    software.amazon.ssm.patchbaseline.Tag.builder()
                                            .key(entry.key())
                                            .value(entry.value())
                                            .build(),
                            Collectors.toList()));
            return Optional.of(resourceModelTags);
        }
        return Optional.empty();
    }

    /**
     * Translate Resource Model Sources to Request Sources
     */
    public  static Optional<List<PatchSource>> translateToRequestSources(final List<software.amazon.ssm.patchbaseline.PatchSource> sources) {

        if (!CollectionUtils.isNullOrEmpty(sources)) {
            List<PatchSource> requestSources = sources.stream()
                    .collect(Collectors.mapping(entry ->
                                    PatchSource.builder()
                                            .configuration(entry.getConfiguration())
                                            .name(entry.getName())
                                            .products(entry.getProducts())
                                            .build(),
                            Collectors.toList()));
            return Optional.of(requestSources);
        }
        return Optional.empty();
    }

    /**
     * Translate Request Sources to Resource Model Sources
     */
    public  static Optional<List<software.amazon.ssm.patchbaseline.PatchSource>> translateToResourceModelSources(final List<PatchSource> sources) {

        if (!CollectionUtils.isNullOrEmpty(sources)) {
            List<software.amazon.ssm.patchbaseline.PatchSource> resourceModelSources = sources.stream()
                    .collect(Collectors.mapping(entry ->
                                    software.amazon.ssm.patchbaseline.PatchSource.builder()
                                            .configuration(entry.configuration())
                                            .name(entry.name())
                                            .products(entry.products())
                                            .build(),
                            Collectors.toList()));
            return Optional.of(resourceModelSources);
        }
        return Optional.empty();
    }

    /**
     * Translate Resource Model GlobalFilters to Request GlobalFilters
     */
    public  static Optional<PatchFilterGroup> translateToRequestGlobalFilters(final software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilter) {

        if (globalFilter != null && !CollectionUtils.isNullOrEmpty(globalFilter.getPatchFilters())) {
            List<software.amazon.ssm.patchbaseline.PatchFilter> patchFilters = globalFilter.getPatchFilters();
            List<PatchFilter> requestPatchFilters = translateToRequestPatchFilters(patchFilters);
            PatchFilterGroup requestGlobalFilter = PatchFilterGroup.builder()
                    .patchFilters(requestPatchFilters)
                    .build();
            return Optional.of(requestGlobalFilter);
        }
        return Optional.empty();
    }

    /**
     * Translate Resource Model PatchFilters to Request PatchFilters
     */
    private  static List<PatchFilter> translateToRequestPatchFilters(final List<software.amazon.ssm.patchbaseline.PatchFilter> patchFilters) {

        List<PatchFilter> requestPatchFilters = new ArrayList<>();

        if (!CollectionUtils.isNullOrEmpty(patchFilters)) {
            for (software.amazon.ssm.patchbaseline.PatchFilter entry : patchFilters) {
                PatchFilter build = PatchFilter.builder()
                        .key(entry.getKey())
                        .values(entry.getValues())
                        .build();
                requestPatchFilters.add(build);
            }
        }
        return requestPatchFilters;
    }

    /**
     * Translate Request GlobalFilters to Resource Model GlobalFilters
     */
    public  static Optional<software.amazon.ssm.patchbaseline.PatchFilterGroup> translateToResourceModelGlobalFilters(final PatchFilterGroup globalFilter) {

        if (globalFilter != null && !CollectionUtils.isNullOrEmpty(globalFilter.patchFilters())) {
            List<PatchFilter> patchFilters = globalFilter.patchFilters();
            List<software.amazon.ssm.patchbaseline.PatchFilter> resourceModelPatchFilters = translateToResourceModelPatchFilters(patchFilters);
            software.amazon.ssm.patchbaseline.PatchFilterGroup resourceModelGlobalFilter =
                    software.amazon.ssm.patchbaseline.PatchFilterGroup.builder()
                    .patchFilters(resourceModelPatchFilters)
                    .build();
            return Optional.of(resourceModelGlobalFilter);
        }
        return Optional.empty();
    }

    /**
     * Translate Request PatchFilter to Resource Model PatchFilters
     */
    private  static List<software.amazon.ssm.patchbaseline.PatchFilter> translateToResourceModelPatchFilters(final List<PatchFilter> patchFilters) {

        List<software.amazon.ssm.patchbaseline.PatchFilter> resourceModelPatchFilter = new ArrayList<>();

        if (!CollectionUtils.isNullOrEmpty(patchFilters)) {
            resourceModelPatchFilter = patchFilters.stream()
                    .collect(Collectors.mapping(entry ->
                                    software.amazon.ssm.patchbaseline.PatchFilter.builder()
                                            .key(entry.keyAsString())
                                            .values(entry.values())
                                            .build(),
                            Collectors.toList()));
        }
        return resourceModelPatchFilter;
    }


    /**
     * Translate Resource Model ApprovalRules to Request ApprovalRules
     */
    public  static Optional<PatchRuleGroup> translateToRequestApprovalRules(final software.amazon.ssm.patchbaseline.RuleGroup approvalRules) {

        if (approvalRules != null && !CollectionUtils.isNullOrEmpty(approvalRules.getPatchRules())) {
            List<software.amazon.ssm.patchbaseline.Rule> rules = approvalRules.getPatchRules();
            List<PatchRule> requestPatchRules = translateToRequestPatchRules(rules);
            PatchRuleGroup requestApprovalRules = PatchRuleGroup.builder()
                    .patchRules(requestPatchRules)
                    .build();
            return Optional.of(requestApprovalRules);
        }
        return Optional.empty();
    }

    /**
     * Translate Resource Model Rules to Request PatchRules
     */
    private static  List<PatchRule> translateToRequestPatchRules(final List<software.amazon.ssm.patchbaseline.Rule> rules) {

        List<PatchRule> requestPatchRules = new ArrayList<>();

        if (!CollectionUtils.isNullOrEmpty(rules)) {
            for (software.amazon.ssm.patchbaseline.Rule entry : rules) {
                PatchRule.Builder patchRuleBuilder = PatchRule.builder()
                        .approveAfterDays(entry.getApproveAfterDays())
                        .complianceLevel(entry.getComplianceLevel())
                        .enableNonSecurity(entry.getEnableNonSecurity());
                translateToRequestGlobalFilters(entry.getPatchFilterGroup()).ifPresent(patchRuleBuilder::patchFilterGroup);

                requestPatchRules.add(patchRuleBuilder.build());
            }
        }
        return requestPatchRules;
    }


    /**
     * Translate Request ApprovalRules to Resource Model ApprovalRules
     */
    public static Optional<software.amazon.ssm.patchbaseline.RuleGroup> translateToResourceModelApprovalRules(final PatchRuleGroup approvalRules) {

        if (approvalRules != null && !CollectionUtils.isNullOrEmpty(approvalRules.patchRules())) {
            List<PatchRule> patchRules = approvalRules.patchRules();
            List<software.amazon.ssm.patchbaseline.Rule> requestPatchRules = translateToResourceModelPatchRules(patchRules);
            software.amazon.ssm.patchbaseline.RuleGroup resourceModelApprovalRules =
                    software.amazon.ssm.patchbaseline.RuleGroup.builder()
                    .patchRules(requestPatchRules)
                    .build();
            return Optional.of(resourceModelApprovalRules);
        }
        return Optional.empty();
    }
    /**
     * Translate Request PatchRules to Resource Model Rules
     */
    private static List<software.amazon.ssm.patchbaseline.Rule> translateToResourceModelPatchRules(final List<PatchRule> patchRules) {

        List<software.amazon.ssm.patchbaseline.Rule> resourceModelPatchRules = new ArrayList<>();

        if (!CollectionUtils.isNullOrEmpty(patchRules)) {
            for (PatchRule entry : patchRules) {
                software.amazon.ssm.patchbaseline.Rule.RuleBuilder ruleBuilder =
                        software.amazon.ssm.patchbaseline.Rule.builder()
                                .approveAfterDays(entry.approveAfterDays())
                                .complianceLevel(entry.complianceLevelAsString())
                                .enableNonSecurity(entry.enableNonSecurity());
                translateToResourceModelGlobalFilters(entry.patchFilterGroup()).ifPresent(ruleBuilder::patchFilterGroup);
                resourceModelPatchRules.add(ruleBuilder.build());
            }
        }
        return resourceModelPatchRules;
    }



}


