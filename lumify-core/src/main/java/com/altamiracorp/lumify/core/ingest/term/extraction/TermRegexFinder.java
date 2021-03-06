package com.altamiracorp.lumify.core.ingest.term.extraction;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TermRegexFinder {

    public static List<TermMention> find(String artifactId, GraphVertex concept, String text, String regex) {
        return find(artifactId, concept, text, Pattern.compile(regex));
    }

    public static List<TermMention> find(String artifactId, GraphVertex concept, String text, Pattern regex) {
        Matcher m = regex.matcher(text);
        List<TermMention> termMentions = new ArrayList<TermMention>();
        while (m.find()) {
            if (m.groupCount() != 2) {
                throw new RuntimeException("regex pattern must have 2 capture groups. the first will determine the start and end index, the second will determine the sign.");
            }

            String groupCapture = m.group(1);
            int groupCaptureOffset = text.indexOf(groupCapture, m.start());

            TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactId, groupCaptureOffset, groupCaptureOffset + groupCapture.length());
            TermMention termMention = new TermMention(termMentionRowKey);
            termMention.getMetadata()
                    .setSign(m.group(2))
                    .setOntologyClassUri((String) concept.getProperty(PropertyName.DISPLAY_NAME))
                    .setConceptGraphVertexId(concept.getId());
            termMentions.add(termMention);
        }
        return termMentions;
    }
}
