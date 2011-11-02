package net.rujel.reusables.xml;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;


public class Resolver implements URIResolver {

	private Map sources;
	
	public Resolver(Map dict) {
		super();
		sources = dict;
	}
	
	public Source resolve(String href, String base) throws TransformerException {
		if(href == null)
			return null;
		if(sources != null) {
			Source result = (Source)sources.get(href);
			if(result != null)
				return result;
		}
		if(href.startsWith("class:")) {
			try {
				Class cl = Class.forName(href.substring(6));
				Constructor constr = cl.getConstructor((Class[])null);
				return (Source)constr.newInstance((Object[])null);
			} catch (Exception e) {
				throw new TransformerException("Failed to construct requested source", e);
			}
		}
		return new StreamSource(base);
	}

}
