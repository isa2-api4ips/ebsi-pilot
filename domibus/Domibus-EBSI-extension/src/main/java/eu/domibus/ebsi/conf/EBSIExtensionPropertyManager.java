package eu.domibus.ebsi.conf;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EBSIExtensionPropertyManager extends DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    public static final String EBSI_URL = "domibus.ebsi.url";
    public static final String EBSI_NOTARY_ADDRESS = "domibus.ebsi.notary.address";
    public static final String EBSI_APPLICATION_NAME = "domibus.ebsi.application.name";
    public static final String EBSI_KEYSTORE_LOCATION = "domibus.ebsi.keystore.location";
    public static final String EBSI_KEYSTORE_TYPE = "domibus.ebsi.keystore.type";
    public static final String EBSI_KEYSTORE_PASSWORD = "domibus.ebsi.keystore.password";
    public static final String EBSI_KEY_ALIAS = "domibus.ebsi.key.private.alias";
    public static final String EBSI_KEY_PASSWORD = "domibus.ebsi.key.private.password";

    public static final String EBSI_DID_NAME= "domibus.ebsi.did.name";


    public static final String EBSI_TIMESTAMP_TSA_URL = "domibus.ebsi.timestamp.tsa.url";
    public static final String EBSI_TIMESTAMP_TSA_TYPE = "domibus.ebsi.timestamp.tsa.type";

    public static final String EBSI_TIMESTAMP_TYPE = "domibus.ebsi.timestamp.type";
    public static final String DOMIBUS_EBSI_TIMESTAMP_ASYNCHRONOUS = "domibus.ebsi.timestamp.asynchronous";

    public static final String EBSI_TIMESTAMP_MESSAGE_TYPE = "domibus.ebsi.timestamp.message.type";
    public static final String EBSI_TIMESTAMP_MESSAGE_DIRECTION = "domibus.ebsi.timestamp.message.direction";



    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public EBSIExtensionPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(EBSI_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_NOTARY_ADDRESS, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_DID_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),

                new DomibusPropertyMetadataDTO(EBSI_APPLICATION_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_KEYSTORE_LOCATION, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_KEYSTORE_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_KEYSTORE_PASSWORD, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_KEY_ALIAS, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_KEY_PASSWORD, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),

                new DomibusPropertyMetadataDTO(EBSI_TIMESTAMP_TSA_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_TIMESTAMP_TSA_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_TIMESTAMP_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DOMIBUS_EBSI_TIMESTAMP_ASYNCHRONOUS, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_TIMESTAMP_MESSAGE_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EBSI_TIMESTAMP_MESSAGE_DIRECTION, DomibusPropertyMetadataDTO.Type.STRING, Module.MSH, DomibusPropertyMetadataDTO.Usage.GLOBAL)

        );
        knownProperties = allProperties.stream().collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, domibusPropertyMetadataDTO -> domibusPropertyMetadataDTO));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }
}
