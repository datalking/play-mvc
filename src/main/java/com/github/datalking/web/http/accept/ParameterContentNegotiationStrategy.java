package com.github.datalking.web.http.accept;

import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 */
public class ParameterContentNegotiationStrategy extends AbstractMappingContentNegotiationStrategy {

	private static final Logger logger = LoggerFactory.getLogger(ParameterContentNegotiationStrategy.class);

	private String parameterName = "format";


	public ParameterContentNegotiationStrategy(Map<String, MediaType> mediaTypes) {
		super(mediaTypes);
	}

	public void setParameterName(String parameterName) {
		Assert.notNull(parameterName, "parameterName is required");
		this.parameterName = parameterName;
	}

	@Override
	protected String getMediaTypeKey(WebRequest webRequest) {
		return webRequest.getParameter(this.parameterName);
	}

	@Override
	protected void handleMatch(String mediaTypeKey, MediaType mediaType) {
		if (logger.isDebugEnabled()) {
			logger.debug("Requested media type is '" + mediaType + "' (based on parameter '" +
					this.parameterName + "'='" + mediaTypeKey + "')");
		}
	}

}
