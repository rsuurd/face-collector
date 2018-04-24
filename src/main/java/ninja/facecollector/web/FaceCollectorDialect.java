package ninja.facecollector.web;

import ninja.facecollector.services.DiscordService;
import ninja.facecollector.services.GiphyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class FaceCollectorDialect extends AbstractDialect implements IExpressionObjectDialect {
	private Map<String, Object> objects;

	@Autowired
	public FaceCollectorDialect(DiscordService discordService, GiphyService giphyService) {
		super("Face collector Dialect");

		objects = new HashMap<>();
		objects.put("discord", discordService);
		objects.put("giphy", giphyService);
	}

	public IExpressionObjectFactory getExpressionObjectFactory() {
		return new IExpressionObjectFactory() {
			public Set<String> getAllExpressionObjectNames() {
				return objects.keySet();
			}

			public Object buildObject(IExpressionContext context, String expressionObjectName) {
				return objects.get(expressionObjectName);
			}

			public boolean isCacheable(String expressionObjectName) {
				return true;
			}
		};
	}
}
