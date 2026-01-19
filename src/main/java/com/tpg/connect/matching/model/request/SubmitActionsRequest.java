package com.tpg.connect.matching.model.request;

import com.tpg.connect.matching.model.entity.MatchAction.ActionType;

import java.util.List;

public record SubmitActionsRequest(
        List<ActionItem> actions
) {
    public record ActionItem(
            long targetConnectId,
            ActionType action
    ) {
    }
}

