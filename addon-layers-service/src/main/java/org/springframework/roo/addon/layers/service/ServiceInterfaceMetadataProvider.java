package org.springframework.roo.addon.layers.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.taggers.CustomDataKeyDecorator;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.layers.LayerCustomDataKeys;
import org.springframework.roo.project.layers.LayerTypeMatcher;

/**
 * 
 * @author Stefan Schmidt
 * @since 1.2
 */
@Component(immediate=true)
@Service
public class ServiceInterfaceMetadataProvider extends AbstractItdMetadataProvider {
	
	@Reference private CustomDataKeyDecorator customDataKeyDecorator;
	
	private static final JavaType ROO_SERVICE = new JavaType(RooService.class.getName());
	
	protected void activate(ComponentContext context) {
		super.setDependsOnGovernorBeingAClass(false);
		metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
		addMetadataTrigger(ROO_SERVICE);
		customDataKeyDecorator.registerMatcher(getClass().getName(), new LayerTypeMatcher(LayerCustomDataKeys.LAYER_TYPE, ServiceInterfaceMetadata.class.getName(), ROO_SERVICE, new JavaSymbolName(RooService.DOMAIN_TYPES)));
	}

	protected void deactivate(ComponentContext context) {
		metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
		removeMetadataTrigger(ROO_SERVICE);
	}
	
	@Override
	protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {
		ServiceAnnotationValues annotationValues = new ServiceAnnotationValues(governorPhysicalTypeMetadata);
		ClassOrInterfaceTypeDetails coitd = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata.getMemberHoldingTypeDetails();
		if (coitd == null) {
			return null;
		}
		MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(getClass().getName(), coitd);
		JavaType[] domainTypes = annotationValues.getDomainTypes();
		if (domainTypes == null || domainTypes.length == 0) {
			return null;
		}
		Map<JavaType, String> domainTypePlurals = new HashMap<JavaType, String>();
		for (JavaType type : domainTypes) {
			String pluralId = PluralMetadata.createIdentifier(type, Path.SRC_MAIN_JAVA);
			PluralMetadata pluralMetadata = (PluralMetadata) metadataService.get(pluralId);
			if (pluralMetadata == null) {
				return null;
			}
			metadataDependencyRegistry.registerDependency(pluralId, metadataIdentificationString);
			domainTypePlurals.put(type, pluralMetadata.getPlural());
		}
		return new ServiceInterfaceMetadata(metadataIdentificationString, aspectName, governorPhysicalTypeMetadata, memberDetails, annotationValues, domainTypePlurals);
	}
	
	public String getItdUniquenessFilenameSuffix() {
		return "Service";
	}

	public String getProvidesType() {
		return ServiceInterfaceMetadata.getMetadataIdentiferType();
	}

	@Override
	protected String createLocalIdentifier(JavaType javaType, Path path) {
		return ServiceInterfaceMetadata.createIdentifier(javaType, path);
	}

	@Override
	protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
		JavaType javaType = ServiceInterfaceMetadata.getJavaType(metadataIdentificationString);
		Path path = ServiceInterfaceMetadata.getPath(metadataIdentificationString);
		String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, path);
		return physicalTypeIdentifier;
	}
}
