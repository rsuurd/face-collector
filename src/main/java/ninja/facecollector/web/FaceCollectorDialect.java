package ninja.facecollector.web;

import ninja.facecollector.services.GiphyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Collections;
import java.util.Set;

@Component
public class FaceCollectorDialect extends AbstractDialect implements IExpressionObjectDialect {
	private GiphyService giphyService;

	@Autowired
	public FaceCollectorDialect(GiphyService giphyService) {
		super("Face collector Dialect");

		this.giphyService = giphyService;
	}

	public IExpressionObjectFactory getExpressionObjectFactory() {
		return new IExpressionObjectFactory() {
			public Set<String> getAllExpressionObjectNames() {
				return Collections.singleton("giphy");
			}

			public Object buildObject(IExpressionContext context, String expressionObjectName) {
				return giphyService;
			}

			public boolean isCacheable(String expressionObjectName) {
				return true;
			}
		};
	}
}
