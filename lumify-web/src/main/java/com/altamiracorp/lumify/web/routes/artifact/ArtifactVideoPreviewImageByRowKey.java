package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public class ArtifactVideoPreviewImageByRowKey extends BaseRequestHandler {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ArtifactVideoPreviewImageByRowKey.class);
    private final ArtifactRepository artifactRepository;
    private final ArtifactThumbnailRepository artifactThumbnailRepository;

    @Inject
    public ArtifactVideoPreviewImageByRowKey(final ArtifactRepository artifactRepo,
                                             final ArtifactThumbnailRepository thumbnailRepo) {
        artifactRepository = artifactRepo;
        artifactThumbnailRepository = thumbnailRepo;
    }

    public static String getUrl(HttpServletRequest request, ArtifactRowKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/video-preview";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        ArtifactRowKey artifactRowKey = new ArtifactRowKey(UrlUtils.urlDecode(getAttributeString(request, "_rowKey")));

        String widthStr = getOptionalParameter(request, "width");
        int[] boundaryDims = new int[]{200 * ArtifactRepository.FRAMES_PER_PREVIEW, 200};

        if (widthStr != null) {
            boundaryDims[0] = Integer.parseInt(widthStr) * ArtifactRepository.FRAMES_PER_PREVIEW;
            boundaryDims[1] = Integer.parseInt(widthStr);

            response.setContentType("image/jpeg");
            response.addHeader("Content-Disposition", "inline; filename=thumnail" + boundaryDims[0] + ".jpg");

            byte[] thumbnailData = artifactThumbnailRepository.getThumbnailData(artifactRowKey, "video-preview", boundaryDims[0], boundaryDims[1], user);
            if (thumbnailData != null) {
                LOGGER.debug("Cache hit for: %s (video-preview) %d x %d", artifactRowKey.toString(), boundaryDims[0], boundaryDims[1]);
                ServletOutputStream out = response.getOutputStream();
                out.write(thumbnailData);
                out.close();
                return;
            }
        }

        InputStream in = artifactRepository.getVideoPreviewImage(artifactRowKey);
        try {
            if (widthStr != null) {
                LOGGER.info("Cache miss for: %s (video-preview) %d x %d", artifactRowKey.toString(), boundaryDims[0], boundaryDims[1]);
                byte[] thumbnailData = artifactThumbnailRepository.createThumbnail(artifactRowKey, "video-preview", in, boundaryDims, user).getMetadata().getData();
                ServletOutputStream out = response.getOutputStream();
                out.write(thumbnailData);
                out.close();
            } else {
                response.setContentType("image/png");
                IOUtils.copy(in, response.getOutputStream());
            }
        } finally {
            in.close();
        }
    }
}
