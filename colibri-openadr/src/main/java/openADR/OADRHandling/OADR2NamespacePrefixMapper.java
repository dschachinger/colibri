package openADR.OADRHandling;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps OpenADR 2.0b namespaces to standard prefixes.  Note this is
 * a feature specific to certain XML parser implementations and may
 * not work in all cases.
 *
 * @see XMLNS
 * @author tnichols
 */
public class OADR2NamespacePrefixMapper extends NamespacePrefixMapper {

	static final String[] allURIs;

	private Logger logger = LoggerFactory.getLogger(OADR2NamespacePrefixMapper.class);

	static {
		List<String> uris = new ArrayList<String>();
		for ( XMLNS ns : XMLNS.values() ) uris.add( ns.getNamespaceURI() );
		allURIs = uris.toArray( new String[uris.size()] );
	}

	public OADR2NamespacePrefixMapper() {}
	
	public OADR2NamespacePrefixMapper(Marshaller marshaller) throws PropertyException {
		this.addTo(marshaller);
	}
	
	/**
	 * This attempts to add the namespace prefix mapper to the JAXB marshaller, so that
	 * serialized payloads will have 'standard' XML namespace prefixes.  Note this is 
	 * only likely to work if you're using the Sun XML APIs.
	 * @param marshaller
	 * @throws PropertyException
	 */
	public void addTo( Marshaller marshaller ) throws PropertyException {
		// This probably only works for com.sun.xml.bind.v2.runtime.MarshallerImpl
		try {
			marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", this); 
		}
		catch ( Exception ex ) {
		    try {
		        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", this);
		    }
		    catch ( Exception e2 ) {
				logger.error("Couldn't enable the Namespace Prefix mapper, we're probably not"
		                + " using the Sun XML impl. " + e2.getMessage() );
		    }
		}		
	}

	/**
	 * Return the 'preferred' namespace prefix for the given URI.  This is called
	 * automatically if you're using the Sun Java XML APIs if .
	 * @see NamespacePrefixMapper#getPreferredPrefix(String, String, boolean)
	 */
	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		try {
			XMLNS ns = XMLNS.fromURI( namespaceUri );
			if ( ns == XMLNS.OADR2 ) return ns.getPrefix();
			return ns.getPrefix();
		}
		catch ( IllegalArgumentException ex ) {
			return suggestion;
		}
	}
    
	/**
	 * @see NamespacePrefixMapper#getPreDeclaredNamespaceUris()
	 */
	@Override
    public String[] getPreDeclaredNamespaceUris() {
        return allURIs;
    }
}
