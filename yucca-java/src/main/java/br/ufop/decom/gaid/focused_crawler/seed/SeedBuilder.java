package br.ufop.decom.gaid.focused_crawler.seed;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONObject;

import br.ufop.decom.gaid.focused_crawler.seed.search.Search;
import edu.uci.ics.crawler4j.url.WebURL;

public class SeedBuilder {

	private Search search;
	private int numSeeds;
	
	public SeedBuilder(Search search, int numSeeds) {
		this.search = search;
		this.numSeeds = numSeeds;
	}
	
	public List<WebURL> build() {
		List<JSONObject> result = this.search.search(this.numSeeds);
		return parseResult(result);
	}

	public List<WebURL> buildGenreSeeds() {
		List<JSONObject> result = this.search.searchByGenre(this.numSeeds);
		return parseResult(result);
	}

	public List<WebURL> buildContentSeeds() {
		List<JSONObject> result = this.search.searchByContent(this.numSeeds);
		return parseResult(result);
	}

	private List<WebURL> parseResult (List<JSONObject> result) {
		List<WebURL> seeds = new ArrayList<>();

		for(JSONObject entry : result) {
			WebURL seed = new WebURL();
			seed.setURL(StringEscapeUtils.escapeHtml4((String) entry.get("url")));
			seed.setTag(StringEscapeUtils.escapeHtml4((String) entry.get("tag")));
			seed.setAnchor(StringEscapeUtils.escapeHtml4((String) entry.get("anchor")));
			seeds.add(seed);
		}

		return seeds;
	}

	public void setNumSeeds(int numSeeds) {
		this.numSeeds = numSeeds;
	}
	
}
