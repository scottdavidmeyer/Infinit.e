/*******************************************************************************
 * Copyright 2012, The Infinit.e Open Source Project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.ikanow.infinit.e.data_model.api.knowledge;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ikanow.infinit.e.data_model.api.BaseApiPojo;

//
// Generated by clients to make requests to the API (SearchResource/SearchController)
//

public class AdvancedQueryPojo extends BaseApiPojo {

	static public class QueryTermPojo 
	{
		public String ftext; // Free text query (arbitrary Lucene query)
		public String etext; // Exact text search
		public String entity; // Gazateer index ("disambiguous_name/type", '/' can be ':')
		// Alternative for entity (overrides it):
		public String entityValue; // (must be specified)
		public String entityType; // (can be left unspecified)
		
		static public class EntityOptionPojo { // Options for "entity" types
			public boolean expandAlias = false; // If true (!default), adds aliases to the search
			public boolean expandOntology = false; // If true (!default), adds "useful" generalizations of the entity to the search
		}
		public EntityOptionPojo entityOpt;
		
		static public class AssociationTermPojo { // All these terms are ANDed together
			public QueryTermPojo entity1; // obviously only etext/ftext/entity/(entityValue,entityType) make sense
			public QueryTermPojo entity2; // obviously only etext/ftext/entity/(entityValue,entityType) make sense
			public String verb; // verb or verb category
			public QueryTermPojo.GeoTermPojo geo;
			public QueryTermPojo.TimeTermPojo time;
			public String type; // Event,Fact,Summary
		}
		public AssociationTermPojo event;
		public AssociationTermPojo assoc;
		
		static public class TimeTermPojo { // Time bounding term for the query
			public String min; // Min date, inclusive, (default - no min) - any sensible date format parsed
			public String max; // Max date, inclusive, (default - no max) - any sensible date format parsed
		}
		public TimeTermPojo time;
		
		static public class GeoTermPojo { // Geo bounding term for the query
			//1 of these
			public String centerll; // The center of the bounding box, "(lat,lng)" or "lat,lng" format			
				//OR
			public String name; //name of some geolocation e.g. new jersey, united states (NOT SUPPORTED YET)
				//OR
			public String polys; //list of polygon lat/lng points (NOT SUPPORTED YET)			
			//REQUIRED for these (maybe, i dont think we need dist for polys or some name (e.g. countries)
			public String dist; // The radius of the circle enclosing the bounding box, "Nu" where N is the distance, and u is the unit, from {mi,km,nm}, default km
			
			//OR can be this for bounding box
			public String minll; // Top-left (same format as above)
			public String maxll; // Bottom-right
			
			//OPTIONAL for all
				//e.g. send countrysubsidiary and we will only search states, cities, points (not country so US doesnt show up 1mil times)
			public String ontology_type; //the ontology type of the searchterm so we can only search this type an below 
		}
		public GeoTermPojo geo;
		
		public String metadataField = null; // Restricts an etext/ftext to apply only to the specified metadata field
											// Ignored for other query types.
	}
	public List<QueryTermPojo> qt; // Array of query terms as above
	public String logic; // The logic to combine these query terms, use Lucene logic with qt[n], n=0,1,2,... or n=1,2,3...

	static public class QueryRawPojo { // Raw query object for power users
		public QueryRawPojo() {}
		public QueryRawPojo(String s) { query = s; }
		public String query; // (ElasticSearch too complex to be written into an object so just pass it through)
		
		static public class Deserializer implements JsonDeserializer<QueryRawPojo> {
			public QueryRawPojo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
				return new QueryRawPojo(json.toString());
			}
		}
	}
	public QueryRawPojo raw; // Allows an ElasticSearch raw query (raw instead of query as the root object), note this overrides the qt/logic parameters
	
	static public class QueryInputPojo { // Input parameters for the query
		public String name; // If specified, the saved dataset name
		public List<String> tags; // If specified, an ORd list of tags that need to be in a source for it to be added to the query
		
		static public class TypeAndTagTermPojo { // If specified, an ORd list of types (each type ANDed with an ORd list of tags)
			public String type; // The type (eg news, social) - must be specified
			public List<String> tags; // An optional list of tags ANDed with the above type (must be forced to lower case)
		}
		public List<TypeAndTagTermPojo> typeAndTags;
		
		public List<String> sources; // A list of sources to include/exclude depending on the "src_include" tag
		public Boolean srcInclude; // Defaults to true - whether the sources are to be included (true) or excluded (false)
	}
	public QueryInputPojo input; // (optional, searches over the entire system group DB if left out)
	
	static public class QueryScorePojo { // Scoring parameters for the query
		public Integer numAnalyze = 1000; // The number of documents on which to perform processing, default 1000
		public Double sigWeight = 0.67; // The relative weight to give to query significance (Infinit.e score), default to 0.67
		public Double relWeight = 0.33; // The relative weight to give to search relevance (standard Lucene score), default to 0.33
		
		static public class TimeProxTermPojo { // Scoring based on time
			public String time = "now"; // The "centre" time, any sensible format is parsed, including "now" (default)
			public String decay = "1m"; // The decay parameter, used as 1/(1 + (t-t0)/decay) ... ie after time decay the score is x'd by 0.5, default "1m" 
				// time format "Nu" with N the quantity and u the unit, one of {d,w,m,y}
			public transient long nTime; // (time as a long - for internal calcs)
			public transient double dInvDecay; //(1/decay in ms - for internal calcs)
		}
		public TimeProxTermPojo timeProx; // (optional, not applied if left out)
		
		static public class GeoProxTermPojo { // Scoring based on location
			public String ll; // The "centre" location, format "(lat,lng)" or "lat,lng"
			public String decay; // The decay distance used like TimeProxTermPojo, "Nu" format as discussed above in GeoTermPojo.dist
		}
		public GeoProxTermPojo geoProx; // (optional, not applied if left out)
	}
	public QueryScorePojo score; // (optional, just returns the most recent "numReturn" docs if left out)
	
	static public class QueryOutputPojo {
		public String format = "json"; // output format: "xml", "json" (default), "rss" ("xml" and "json" are the same fields in different formats, "rss" only includes the URL)
		static public class DocumentOutputPojo {
			public Boolean enable = true; // (bring back documents if true)
			public Boolean eventsTimeline = false; // (bring back just the events, not the documents)
			public Integer numReturn = 100; // (number of documents to bring back)
			public Integer numEventsTimelineReturn = 1000; // (number of events to bring back)
			public Integer skip = 0;
			public Boolean ents = true;
			public Boolean geo = true; // (if ents==false, this still brings back entities)
			public Boolean events = true;
			public Boolean facts = true;
			public Boolean summaries = true;
			public Boolean metadata = true;
		}
		public DocumentOutputPojo docs = new DocumentOutputPojo();
		
		static public class AggregationOutputPojo {
			// Geo-spatial/temporal aggregation
			// (A more powerful interface to do the same thing is listed below)
			public Integer geoNumReturn = 0; // (100 works, 1000 is probably best if you can wait for it)
			public String timesInterval = null; // "month", or "<N>[ydwh]" (disabled if null)
			
			// Different built-in aggregation types, the number of records to return, format { "term", "count" }
			// (A more powerful interface to the same thing is listed below)
			public Integer entsNumReturn = 0;
			public Integer eventsNumReturn = 0;
			public Integer factsNumReturn  = 0;
			public Integer sources = 0; // (src urls)
			public Integer sourceMetadata = 0; // (both tags and types)
			
			// A better interface, note will override the above if no regex filter set:
			// (not supported as of V0)
			static public class EntityAggregationOutputPojo {
				// This has different meaning depending on which of numReturn/geoMaxCount/timesInterval is set:
				public String target = null; // 1 of "geo", "times", "entity", "event", "fact", "source", "source_tag", "source_type", or "metadata.<[object.]*field>"
				// And then one of:
				public Integer numReturn = 0; // Number of entries to return - aggregates document counts vs the specified object
				public String regexFilter = null; // (only applies for numReturn set, not geoMaxCount or timesInterval)
				//or
				public Integer geoMaxCount = null; // Number of geotags to return - counts either documents (target==null), or numeric fields (target="metadata.*")
				//or
				public String timesInterval = null; // Date histogram width - counts either documents (target==null), or numeric fields (object="metadata.*")				
			}
			public List<EntityAggregationOutputPojo> aggList = null; 

			// Alternatively:
			public String raw = null; // (allows the user to specify his own facets in ElasticSearch syntax as an alternative)
		}
		public AggregationOutputPojo aggregation = null; // (off by default)
		
		// Output filters (entity types, assoc verb categories)
		static public class FilterOutputPojo {
			public String[] entityTypes = null;
			public String[] assocVerbs = null;
		}
		public FilterOutputPojo filter = null; // (off by default)
		
	}
	public QueryOutputPojo output; // (optional, just defaults to the above defaults if omitted)
}
