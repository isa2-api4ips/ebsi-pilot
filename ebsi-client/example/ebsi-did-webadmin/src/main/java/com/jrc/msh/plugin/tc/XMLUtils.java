package com.jrc.msh.plugin.tc;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static javax.xml.bind.Marshaller.JAXB_FRAGMENT;
import static java.lang.Boolean.TRUE;

public class XMLUtils {
    /**
     *
     * @param xml
     * @param cls
     * @return
     * @throws JAXBException
     */
    public static Object deserialize(String xml, Class cls)
            throws JAXBException {
        final Unmarshaller um = JAXBContext.newInstance(cls).createUnmarshaller();
        return um.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }

    /**
     *
     * @param fXMLFilePath
     * @param cls
     * @return
     * @throws JAXBException
     */
    public static Object deserialize(File fXMLFilePath, Class cls)
            throws JAXBException {
        final Unmarshaller um = JAXBContext.newInstance(cls).createUnmarshaller();
        return um.unmarshal(fXMLFilePath);
    }

    /**
     *
     * @param obj
     * @param filename
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public static void serialize(Object obj, File file)
            throws JAXBException,
            FileNotFoundException {
        final Marshaller m = JAXBContext.newInstance(obj.getClass()).
                createMarshaller();
        m.setProperty(JAXB_FRAGMENT, TRUE);
        m.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);
        m.marshal(obj, new FileOutputStream(file));
    }
}
