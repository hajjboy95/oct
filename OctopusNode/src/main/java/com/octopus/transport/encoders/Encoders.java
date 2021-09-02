package com.octopus.transport.encoders;

import com.octopus.node.service.SubJobUpdate;
import com.octopus.transport.SubJobResult;
import com.octopus.transport.NodeRegistrationRequest;

public class Encoders {
    public static class NodeRegistrationRequestEncoder extends TextEncoder<NodeRegistrationRequest> {}
    public static class SubJobResultEncoder extends TextEncoder<SubJobResult> {}
    public static class SubJobUpdateEncoder extends TextEncoder<SubJobUpdate> {}
}
