package com.altamiracorp.lumify.storm;

import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.storm.textHighlighting.ArtifactHighlightingBolt;

public class StormRunner extends StormRunnerBase {
    private static final String TOPOLOGY_NAME = "lumify";

    public static void main(String[] args) throws Exception {
        int res = new StormRunner().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected String getTopologyName() {
        return TOPOLOGY_NAME;
    }

    public StormTopology createTopology(int parallelismHint) {
        TopologyBuilder builder = new TopologyBuilder();
        createArtifactHighlightingTopology(builder, parallelismHint);
        return builder.createTopology();
    }

    private void createArtifactHighlightingTopology(TopologyBuilder builder, int parallelismHint) {
        builder.setSpout("artifactHighlightSpout", new LumifyKafkaSpout(getConfiguration(), WorkQueueRepository.ARTIFACT_HIGHLIGHT_QUEUE_NAME, getQueueStartOffsetTime()), 1)
                .setMaxTaskParallelism(1);
        builder.setSpout("userArtifactHighlightSpout", new LumifyKafkaSpout(getConfiguration(), WorkQueueRepository.USER_ARTIFACT_HIGHLIGHT_QUEUE_NAME, getQueueStartOffsetTime()), 1)
                .setMaxTaskParallelism(1);
        builder.setBolt("artifactHighlightBolt", new ArtifactHighlightingBolt(), parallelismHint)
                .shuffleGrouping("artifactHighlightSpout")
                .shuffleGrouping("userArtifactHighlightSpout");
    }
}