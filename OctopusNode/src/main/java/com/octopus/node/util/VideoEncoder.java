package com.octopus.node.util;

import org.apache.log4j.Logger;
import ws.schild.jave.*;

import java.io.File;

public class VideoEncoder {
    private final static Logger LOG = Logger.getLogger(VideoEncoder.class);

    /**
     * Used to listen on the encoding progress
     */
    static class Listener implements EncoderProgressListener {
        @Override
        public void sourceInfo(MultimediaInfo info) {
            LOG.debug("Source format: " + info.getFormat());
        }

        @Override
        public void progress(int perMil) {
            LOG.debug("Step: " + perMil + "/ 1000");
        }

        @Override
        public void message(String message) {
            LOG.debug(message);
        }
    }

    public static void encode(File source, File target, String targetEncoding) throws EncoderException {
        final Encoder encoder = new Encoder();
        final EncodingAttributes encodingAttributes = new EncodingAttributes()
                .setFormat(targetEncoding)
                .setDecodingThreads(5)
                .setEncodingThreads(5)
                .setAudioAttributes(new AudioAttributes())
                .setVideoAttributes(new VideoAttributes());

        encoder.encode(new MultimediaObject(source), target, encodingAttributes, new Listener());
    }
}
