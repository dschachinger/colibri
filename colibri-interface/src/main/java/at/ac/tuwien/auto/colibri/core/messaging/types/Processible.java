package at.ac.tuwien.auto.colibri.core.messaging.types;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;

public interface Processible
{
	void process(Datastore store) throws InterfaceException;
}
