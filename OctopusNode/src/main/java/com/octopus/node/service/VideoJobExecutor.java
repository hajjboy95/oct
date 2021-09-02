package com.octopus.node.service;

import com.octopus.node.core.JobExecutor;
import com.octopus.node.core.JobType;
import com.octopus.node.core.VideoPayload;
import com.octopus.node.util.VideoEncoder;
import com.octopus.transport.SubJob;
import com.octopus.transport.SubJobResult;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class VideoJobExecutor implements JobExecutor {
    private final static Logger LOG = Logger.getLogger(VideoJobExecutor.class);
    private final static String FOLDER_ROOT_IN = "src/main/resources/video/in/";
    private final static String FOLDER_ROOT_OUT = "src/main/resources/video/out/";

    public SubJobResult execute(SubJob subJob) {
        final VideoPayload payload = VideoPayload.from(subJob.getData());

        // Create the output file
        new File(FOLDER_ROOT_OUT + subJob.getParentUUID()).mkdir();
        final String targetFilePath = FOLDER_ROOT_OUT + subJob.getParentUUID() + "/" + subJob.getSequenceNumber() + "." + payload.getTargetEncoding();
        final File targetFile = new File(targetFilePath);

        try {
            // Download the source file
            // TODO: Get the file url from the VideoPayload
            final String fileUrl = String.format("http://localhost:8000/files/video/%s/%d.%s", subJob.getParentUUID(), subJob.getSequenceNumber(), payload.getSourceEncoding());
            final String sourceFilePath = FOLDER_ROOT_IN + subJob.getParentUUID() + "/" + subJob.getSequenceNumber() + "." + payload.getSourceEncoding();
            final File sourceFile = new File(sourceFilePath);
            FileUtils.copyURLToFile(new URL(fileUrl), sourceFile);

            // Encode the video
            VideoEncoder.encode(sourceFile, targetFile, payload.getTargetEncoding());

            // TODO: Properly send the result
            return new SubJobResult(null, subJob);
        } catch (EncoderException | IOException e) {
            targetFile.delete();
            throw new RuntimeException(e);
        }
    }

    @Override
    public JobType getJobType() {
        return JobType.WEB_SCRAPING;
    }
}
