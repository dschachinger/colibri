package at.ac.tuwien.auto.colibri.messaging.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.SyntaxException;

public abstract class MessageRdf extends Message
{
	private Model model = null;

	private static final Logger log = Logger.getLogger(MessageRdf.class.getName());

	public final String getContent() throws InterfaceException
	{
		String content = null;

		if (model != null)
		{
			try
			{
				StringWriter sw = new StringWriter();

				// read model from content
				switch (this.getContentType())
				{
					case RDF_XML:
						model.write(sw, "RDF/XML");
						break;
					case TURTLE:
						model.write(sw, "TURTLE");
						break;
					default:
						throw new ProcessingException("Content type " + this.getContentType() + " is not supported for message type + " + this.getMessageType(), this.getPeer());
				}

				content = sw.toString();
				sw.close();
			}
			catch (IOException e)
			{
				log.severe(e.getMessage());
			}
		}
		return content;
	}

	public final void setContent(String content) throws SyntaxException
	{
		// create model for querying
		Model model = ModelFactory.createDefaultModel();

		if (content != null && content != "")
		{
			// create input stre am
			InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

			try
			{
				// read model from content
				switch (this.getContentType())
				{
					case RDF_XML:
						model.read(is, null, "RDF/XML");
						break;
					case TURTLE:
						model.read(is, null, "TURTLE");
						break;
					default:
						throw new ProcessingException("Content type " + this.getContentType() + " is not supported for message type + " + this.getMessageType(), this.getPeer());
				}
			}
			catch (Exception e)
			{
				throw new SyntaxException("Content cannot be parsed", this);
			}

			try
			{
				// close input stream
				is.close();
			}
			catch (IOException e)
			{
				throw new SyntaxException(e.getMessage(), this);
			}
		}

		this.model = model;
	}

	public final Model getModel()
	{
		return this.model;
	}

	public final void setModel(Model model)
	{
		this.model = model;
	}
}
