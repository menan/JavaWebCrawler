import org.apache.log4j.PropertyConfigurator;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
		
        public static void main(String[] args) throws Exception {
    			int numberOfCrawlers = 10;
        		
                String crawlStorageFolder = "/Users/menan/Projects/eclipse-workspace/data/crawler/root";
        		PropertyConfigurator.configure("log4j.properties");
        		
        		CrawlConfig config = new CrawlConfig();

        		/*
        		 * The three crawlers should have different storage folders for their
        		 * intermediate data
        		 */
        		config.setCrawlStorageFolder(crawlStorageFolder);

        		config.setPolitenessDelay(1000);
        		
        		config.setMaxDepthOfCrawling(2);

        		config.setMaxPagesToFetch(30);

        		config.setIncludeBinaryContentInCrawling(true); // this to allow it to crawl through pdf files and other files

        		
        		/*
        		 * Connection timeout in milliseconds
        		 */
        		config.setConnectionTimeout(30000); // 1 Minute

        		/*
        		 * This config parameter can be used to set your crawl to be resumable
        		 * (meaning that you can resume the crawl from a previously
        		 * interrupted/crashed crawl). Note: if you enable resuming feature and
        		 * want to start a fresh crawl, you need to delete the contents of
        		 * rootFolder manually.
        		 */
        		config.setResumableCrawling(false);

        		config.setMaxDownloadSize(10485760); // 10 Mb (1 Mb = 1048576)


                /*
                 * Instantiate the controller for this crawl.
                 */
                PageFetcher pageFetcher = new PageFetcher(config);
                RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
                RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
                CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

                /*
                 * For each crawl, you need to add some seed urls. These are the first
                 * URLs that are fetched and then the crawler starts following links
                 * which are found in these pages
                 */
//                controller.addSeed("http://www.unsigned.com/artists");
                controller.addSeed("http://www.unsigned.com/browse_artists/browse_results.php");
//                controller.addSeed("http://www.unsigned.com/");

                /*
                 * Start the crawl. This is a blocking operation, meaning that your code
                 * will reach the line after this only when crawling is finished.
                 */
                System.out.println("Starting...");
                controller.start(MyWebCrawler.class, numberOfCrawlers);
                
                controller.waitUntilFinish();
                System.out.println("Finished Crawling!");

        }
}