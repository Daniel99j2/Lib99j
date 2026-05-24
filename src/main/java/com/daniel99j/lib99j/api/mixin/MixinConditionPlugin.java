package com.daniel99j.lib99j.api.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.service.MixinService;

import java.util.ArrayList;
import java.util.List;

public class MixinConditionPlugin {
    public static boolean meetsConditions(String clazz) {
        try {
            List<AnnotationNode> annotations = MixinService.getService().getBytecodeProvider().getClassNode(clazz).visibleAnnotations;

            if(annotations == null) return true;

            List<MixinConditionInternal> conditions = new ArrayList<>();

            for (AnnotationNode annotationNode : annotations) {
                if(annotationNode.desc.equals(Type.getDescriptor(MixinCondition.class))) conditions.add(new MixinConditionInternal(annotationNode.values.get(1).toString(), annotationNode.values.get(3).toString()));
            }

            if (conditions.isEmpty()) return true;
            for (MixinConditionInternal condition : conditions) {
                if (!FabricLoader.getInstance().isModLoaded(condition.mod())) return false;
                if (!matches(condition.condition(), FabricLoader.getInstance().getModContainer(condition.mod()).get().getMetadata().getVersion()))
                    return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean matches(String requirement, Version version) throws VersionParsingException {
        if (requirement.equals("*")) return true;

        VersionPredicate predicate = VersionPredicateParser.parse(requirement);

        return predicate.test(version);
    }
}