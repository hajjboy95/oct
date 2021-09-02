package com.octopus.node.service;

import com.octopus.node.core.JobExecutor;
import com.octopus.node.core.JobType;
import com.octopus.transport.SubJobResult;
import com.octopus.node.core.WebScrapingPayload;
import com.octopus.transport.SubJob;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class WebScrapingJobExecutor implements JobExecutor {
    private final static Logger LOG = Logger.getLogger(WebScrapingJobExecutor.class);
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    private static class Result {
        private final String url;
        private final HashMap<String, List<String>> tagResults = new HashMap<>();
        private Result(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public HashMap<String, List<String>> getTagResults() {
            return tagResults;
        }

        public void setTagResults(String tag, List<String> results) {
            tagResults.put(tag, results);
        }

        @Override
        public String toString() {
            return "url: " + url + ", " + tagResults.toString();
        }
    }

    public SubJobResult execute(SubJob subJob) {
        final WebScrapingPayload payload = WebScrapingPayload.from(subJob.getData());
        final ArrayList<Result> resultSet = new ArrayList<>(payload.getUrls().size());

        payload.getUrls().parallelStream().forEach(url -> {
            try {
                String logMessage = String.format("Processing SubJob with parentUUID %s, sequence number %d and url %s",
                        subJob.getParentUUID(), subJob.getSequenceNumber(), url);
                LOG.info(logMessage);

                final Result urlResult = new Result(url);

                final Connection.Response response = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .execute();
                final Document doc = response.parse();

                for (String tag : payload.getScrapingTags()) {
                    LOG.info("processing tag " + tag);
                    final List<Element> elements = doc.select(tag);
                    urlResult.setTagResults(tag, elements.stream().map(Element::html).collect(Collectors.toList()));
                }

                resultSet.add(urlResult);
            } catch (HttpStatusException | MalformedURLException | UnsupportedMimeTypeException | SocketTimeoutException e) {
                LOG.error("HTTPException Error occurred in connecting to url " + url, e);
            } catch (IOException e) {
                LOG.error("error occurred in connecting to url " + url, e);
            }
        });

        return new SubJobResult(resultSet, subJob);
    }

    @Override
    public JobType getJobType() {
        return JobType.WEB_SCRAPING;
    }
}
