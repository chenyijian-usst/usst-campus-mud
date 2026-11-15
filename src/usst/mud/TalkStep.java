package usst.mud;

/**
 * 多段剧情中的"一段对话"。
 *
 * 现有的一次性 NPC 只能"满足条件 -> 发奖励 -> finished"，
 * 支线需要同一个 NPC 在不同阶段说不同的话，所以把每一段抽成 TalkStep：
 * 到达某个阶段、带着某件道具时触发，播完台词后推进到下一阶段。
 */
public class TalkStep {

    private final String questId;
    private final int atStage;           // 玩家处于该阶段时触发
    private final String requireItemId;  // 需要玩家持有的道具，可为 null
    private final String consumeItemId;  // 触发时消耗的道具，可为 null
    private final String grantItemId;    // 触发后获得的道具，可为 null
    private final int creditReward;
    private final int nextStage;         // 推进到的阶段
    private final String[] lines;        // 台词
    private final String hint;           // 条件不满足时的提示

    public TalkStep(String questId, int atStage, String requireItemId, String consumeItemId,
                    String grantItemId, int creditReward, int nextStage,
                    String[] lines, String hint) {
        if (questId == null || questId.trim().isEmpty()) {
            throw new IllegalArgumentException("对话所属的支线 id 不能为空");
        }
        if (atStage < 0 || nextStage < 0) {
            throw new IllegalArgumentException("支线阶段不能为负数");
        }
        if (creditReward < 0 || creditReward > Player.MAX_CREDIT) {
            throw new IllegalArgumentException("对话奖励学分不合法：" + creditReward);
        }
        if (lines == null || lines.length == 0) {
            throw new IllegalArgumentException("对话台词不能为空");
        }
        this.questId = questId.trim();
        this.atStage = atStage;
        this.requireItemId = requireItemId;
        this.consumeItemId = consumeItemId;
        this.grantItemId = grantItemId;
        this.creditReward = creditReward;
        this.nextStage = nextStage;
        this.lines = lines.clone();
        this.hint = hint == null ? "……" : hint;
    }

    /** 当前阶段是否轮到这一段 */
    public boolean matches(Player player) {
        return player.getQuestStage(questId) == atStage;
    }

    /** 前置道具是否齐全；背包满且没有腾出格子时不允许发放奖励 */
    public boolean canApply(Player player) {
        if (requireItemId != null && !player.getBag().has(requireItemId)) {
            return false;
        }
        if (grantItemId != null && !player.getBag().has(grantItemId) && player.getBag().isFull()) {
            boolean freesSlot = consumeItemId != null && player.getBag().has(consumeItemId);
            if (!freesSlot) {
                return false;
            }
        }
        return true;
    }

    public String getHint() {
        return hint;
    }

    /** 播放台词、扣除/发放物品、推进阶段，返回给玩家看的文本 */
    public String apply(Player player) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(lines[i]);
        }
        if (consumeItemId != null && player.getBag().remove(consumeItemId, 1)) {
            Item consumed = CampusData.itemById(consumeItemId);
            sb.append("\n（你递上了「").append(consumed == null ? consumeItemId : consumed.getName())
                    .append("」）");
        }
        if (creditReward > 0) {
            sb.append("\n").append(player.addCredit(creditReward));
        }
        if (grantItemId != null) {
            Item reward = CampusData.itemById(grantItemId);
            if (reward != null && player.getBag().add(reward)) {
                sb.append("\n你获得了「").append(reward.getName()).append("」。");
            }
        }
        player.setQuestStage(questId, nextStage);
        return sb.toString();
    }
}
