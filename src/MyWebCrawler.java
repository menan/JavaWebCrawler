
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyWebCrawler extends WebCrawler {
		public static String HOST = "localhost";
		public static String DB = "artists7";

        private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                          + "|png|tiff?|mid|mp2|mp3|mp4"
                                                          + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                          + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
        private Mongo mc;
    	private DB db;
    	private DBCollection coll;
    	private static DirectedGraph<URL, DefaultEdge> g = new DefaultDirectedGraph<URL, DefaultEdge>(DefaultEdge.class);
    	
    	
    	public MyWebCrawler(){
    		System.out.println("constructor got called");
    		try {
				mc = new MongoClient(HOST, 27017);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
    		db = mc.getDB(DB);
    		coll = db.getCollection("data");
    	}
    	
        /**
         * You should implement this function to specify whether
         * the given url should be crawled or not (based on your
         * crawling logic).
         */
        @Override
        public boolean shouldVisit(WebURL url) {
//        	System.out.println("Not sure if I should visit: " + url.toString());
                String href = url.getURL().toLowerCase();
                return !FILTERS.matcher(href).matches()
                		&& href.contains("unsigned.com/")
                		&& !href.equals("http://www.unsigned.com/")
//                		&& !href.contains("/browse_artists")
                		&& !href.contains("/forgot_password")
                		&& !href.contains("/register")
                		&& !href.contains("/fans")
                		&& !href.contains("/about")
                		&& !href.contains("/login")
                		&& !href.contains("/learn-to-play-guitar")
                		&& !href.contains("/business-jobs")
                		&& !href.contains("/contact")
                		&& !href.contains("/advertise")
                		&& !href.contains("/soulfoundation")
                		&& !href.contains("/spread")
                		;
        }

        /**
         * This function is called when a page is fetched and ready 
         * to be processed by your program.
         */
        @Override
        public void visit(Page page) {          
                String url = page.getWebURL().getURL();
                System.out.println("Crawling " + url);
        		
                if (page.getParseData() instanceof HtmlParseData) {
                        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                        String html = htmlParseData.getHtml();
                		Document jDoc	=	Jsoup.parse(html.toString());
                		Element artistName = jDoc.select("div#artist_infos > h3").first();
                		Elements genre_from = jDoc.select("div#artist_infos > p > a");

                		Element views = jDoc.select("div#artist_stats_box > p > strong").first();
                		Element plays = jDoc.select("div#artist_stats_box > p > strong").last();
                		
                		Element email = jDoc.select("div#artist_contacts_box > p > script").first();
                		Elements websites = jDoc.select("div#artist_contacts_box > p > a");

                		Element bio = jDoc.select("div#artist_biography_content").first();

                		Element image = jDoc.select("div#artist_image > img").first();
                		Elements comments = jDoc.select("div.user_comment");
                		
                		
                		String imgageURL = "http://www.unsigned.com" + image.attr("src");
                		
                		String emailStr = "";
                		
                		if(email != null){
                			emailStr = sanitizeEmails(email.html());
                		}
                		else{
                			System.out.println("Email is nil tho");
                		}
                		
                		ArrayList<String>  genre = new ArrayList<String>();
                		ArrayList<String>  from = new ArrayList<String>();
                		
                		for	(Element	link	:	genre_from)	{
                			if (link.attr("href").contains("countries")){
                				from.add(link.text().replace(" ", ""));
                			}
                			else{
                				genre.add(link.text());
                			}
            			}

                		ArrayList<String> urls = new ArrayList<String>();
                		for	(Element	w	:	websites)	{
                			urls.add(w.text());
            			}
                		
                		if (artistName != null){

                    		String username = page.getWebURL().getPath().substring(1);
                    		
                    		BasicDBObject doc = new BasicDBObject("docid", page.getWebURL().getDocid());
                    		doc.append("username", username);
                    		doc.append("genre", genre);
                    		doc.append("from", from);
                    		doc.append("no_of_views", views.text());
                    		doc.append("no_of_plays", plays.text());
                    		doc.append("no_of_comments", comments.size());
                    		doc.append("email", emailStr);
                    		doc.append("websites", urls);
                    		doc.append("bio", bio.text());
                    		doc.append("image", imgageURL);

                    		System.out.println("artist name: " + artistName.text());
                    		System.out.println("username: " + username);
                    		System.out.println("genre: " + genre.toString());
                    		System.out.println("from: " + from.toString());
                    		System.out.println("total views: " + views.text());
                    		System.out.println("total plays: " + plays.text());
                    		System.out.println("Number of Comments: " + comments.size());
                    		System.out.println("Email: " + emailStr);
                    		System.out.println("Websites: " + urls.toString());
                    		System.out.println("Bio: " + bio.text());
                    		System.out.println("Image: " + imgageURL);

                    		try{
            	        		coll.save(doc);
                    		}
                    		catch(Exception e){
                    			System.out.println("Error saving to database, so shutting down the server");
                    			Controller.stop();
                    		}
                    		
                    		System.out.println("=============================");
                		}
                		else{
                			System.out.println("Artist name is nil tho");
                		}

                }
        }
        
        public String sanitizeEmails(String html)
        {
			return html.substring(16,html.length()-3).replace(", ", "@").replace("'", "");
        	
        }
        
        public static DirectedGraph<URL, DefaultEdge> getGraph(){
        	return g;
        }

}