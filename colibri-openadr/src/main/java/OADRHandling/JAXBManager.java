package OADRHandling;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Convenience class for using JAXB with OpenADR 2.0b profile
 * generated classes.  This is basically initializes the
 * {@link JAXBContext} with the right context path (see
 * {@link #DEFAULT_JAXB_CONTEXT_PATH}) and provides convenience methods to
 * build a {@link Marshaller} instance.
 *
 * @author tnichols
 */
public class JAXBManager {

    public static final String DEFAULT_JAXB_CONTEXT_PATH =
            "com.enernoc.open.oadr2.model.v20b" +
                    ":com.enernoc.open.oadr2.model.v20b.atom" +
                    ":com.enernoc.open.oadr2.model.v20b.currency" +
                    ":com.enernoc.open.oadr2.model.v20b.ei" +
                    ":com.enernoc.open.oadr2.model.v20b.emix" +
                    ":com.enernoc.open.oadr2.model.v20b.gml" +
                    ":com.enernoc.open.oadr2.model.v20b.greenbutton" +
                    ":com.enernoc.open.oadr2.model.v20b.power" +
                    ":com.enernoc.open.oadr2.model.v20b.pyld" +
                    ":com.enernoc.open.oadr2.model.v20b.siscale" +
                    ":com.enernoc.open.oadr2.model.v20b.strm" +
                    ":com.enernoc.open.oadr2.model.v20b.xcal" +
                    ":com.enernoc.open.oadr2.model.v20b.xmldsig" +
                    ":com.enernoc.open.oadr2.model.v20b.xmldsig11";

    JAXBContext jaxbContext;  // thread-safe
    OADR2NamespacePrefixMapper nsMapper; // thread-safe

    public JAXBManager() throws JAXBException {
        this(DEFAULT_JAXB_CONTEXT_PATH);
    }

    public JAXBManager(final String jaxbContextPath) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(jaxbContextPath);
        this.nsMapper = this.createPrefixMapper();
    }

    public JAXBContext getContext() {
        return this.jaxbContext;
    }

    public Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = this.jaxbContext.createMarshaller();
        this.nsMapper.addTo(marshaller);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return marshaller;
    }

    protected OADR2NamespacePrefixMapper createPrefixMapper() {
        return new OADR2NamespacePrefixMapper();
    }
}
