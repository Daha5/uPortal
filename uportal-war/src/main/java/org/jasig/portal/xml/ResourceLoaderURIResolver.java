package org.jasig.portal.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A URIResolver that uses a provided Spring {@link ResourceLoader} to resolve references in a XSL document.
 * Assumes that the provided 'base + href' argument or if base is null 'href' argument can be correctly resolved
 * by the {@link ResourceLoader}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class ResourceLoaderURIResolver implements URIResolver {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final ResourceLoader resourceLoader;
    
    public ResourceLoaderURIResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        final Resource resolvedResource;
        if (base != null) {
            final Resource baseResource = this.resourceLoader.getResource(base);
            
            try {
                resolvedResource = baseResource.createRelative(href);
            }
            catch (IOException e) {
                throw new TransformerException("Failed to find '" + href + "' relative to: " + baseResource, e);
            }
            
            this.logger.debug("Created resource {} for href: {} and base: {}", new Object[] {resolvedResource, href, baseResource});
        }
        else {
            resolvedResource = this.resourceLoader.getResource(href);
            
            this.logger.debug("Created resource {} for href: {}", resolvedResource, href);
        }
        

        final InputStream resourceStream;
        try {
            resourceStream = resolvedResource.getInputStream();
        }
        catch (IOException e) {
            throw new TransformerException("Failed to get InputStream for " + resolvedResource, e);
        }
        
        final StreamSource streamSource = new StreamSource(resourceStream);
        streamSource.setSystemId(base);
        
        return streamSource;
    }
}