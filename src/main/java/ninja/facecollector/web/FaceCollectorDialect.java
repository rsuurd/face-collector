/*
 *     Face Collector
 *     Copyright (C) 2018 Rolf Suurd
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
