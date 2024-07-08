package org.elasticsearch.index.analysis.url;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.plugin.analysis.AnalysisURLPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.StreamsUtils;
import org.junit.Before;

import java.util.*;

import static java.util.Collections.singletonMap;

/**
 * Joe Linn
 * 8/1/2015
 */
public abstract class URLAnalysisTestCase extends ESIntegTestCase {
    protected static final String INDEX = "url_token_filter";
    protected static final String TYPE = "test";


    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(AnalysisURLPlugin.class, WhitespaceTokenizerPlugin.class);
    }

    /**
     * For subclasses to override. Overrides must call {@code super.setUp()}.
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String settings = StreamsUtils.copyToStringFromClasspath("/test-settings.json");
        String mapping = StreamsUtils.copyToStringFromClasspath("/test-mapping.json");
        client().admin().indices().prepareCreate(INDEX).setSettings(settings, XContentType.JSON).addMapping(TYPE, mapping, XContentType.JSON).get();
        refresh();
        Thread.sleep(75);   // Ensure that the shard is available before we start making analyze requests.
    }

    protected List<AnalyzeAction.AnalyzeToken> analyzeURL(String url, String analyzer) {
        return client().admin().indices().prepareAnalyze(INDEX, url).setAnalyzer(analyzer).get().getTokens();
    }

    public static class WhitespaceTokenizerPlugin extends Plugin implements AnalysisPlugin {
        @Override
        public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
            return singletonMap("whitespace",
                    PreConfiguredTokenizer.singleton("whitespace", WhitespaceTokenizer::new));
        }
    }

}
