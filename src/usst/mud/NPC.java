package usst.mud;

import java.util.Random;

/**
 * 校园 NPC。
 *
 * 支持两种模式：
 * 1. 一次性任务（原有模式）：满足条件后发一次奖励，然后只聊闲话；
 * 2. 多段剧情：挂一组 TalkStep，玩家处于不同支线阶段时触发不同的对话。
 *
 * 两种模式可以共存：保安大叔先用一次性任务发印章，完成后再在支线阶段 3
 * 触发"认出老照片"的第二段对话。
 */
public class NPC {

    private final String name;
    private final String role;
    private final int roomId;
    private final String[] lines;        // 闲聊台词，随机抽一句
    private final String requireItemId;  // 需要交给他的物品 id，可为 null
    private final String rewardItemId;   // 交任务后给的物品 id，可为 null
    private final int creditReward;
    private final int needSolved;        // 需要已答对的题目数
    private final String hint;           // 条件不满足时的提示
    private final TalkStep[] steps;      // 多段支线对话，可为 null
    private final Random random = new Random();
    private boolean finished;

    /** 一次性任务 NPC */
    public NPC(String name, String role, int roomId, String[] lines,
               String requireItemId, String rewardItemId, int creditReward,
               int needSolved, String hint) {
        this(name, role, roomId, lines, requireItemId, rewardItemId,
                creditReward, needSolved, hint, null);
    }

    /** 一次性任务 + 多段支线对话 */
    public NPC(String name, String role, int roomId, String[] lines,
               String requireItemId, String rewardItemId, int creditReward,
               int needSolved, String hint, TalkStep[] steps) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("NPC 名字不能为空");
        }
        if (roomId < 0) {
            throw new IllegalArgumentException("NPC 所在场景不合法");
        }
        if (creditReward < 0 || creditReward > Player.MAX_CREDIT) {
            throw new IllegalArgumentException("NPC 奖励学分不合法");
        }
        this.name = name.trim();
        this.role = role == null ? "" : role;
        this.roomId = roomId;
        this.lines = lines == null ? new String[]{"……"} : lines.clone();
        this.requireItemId = requireItemId;
        this.rewardItemId = rewardItemId;
        this.creditReward = creditReward;
        this.needSolved = needSolved;
        this.hint = hint == null ? "……" : hint;
        this.steps = steps == null ? null : steps.clone();
    }

    /**
     * 与 NPC 对话。
     * 顺序：先走未完成的一次性任务，再匹配当前支线阶段的对话，最后才是闲话。
     */
    public String talk(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("玩家不能为空");
        }
        if (!finished && hasSimpleQuest()) {
            return simpleTalk(player);
        }
        if (steps != null) {
            for (int i = 0; i < steps.length; i++) {
                if (!steps[i].matches(player)) {
                    continue;
                }
                if (!steps[i].canApply(player)) {
                    return name + "：" + steps[i].getHint();
                }
                return name + "：" + steps[i].apply(player);
            }
        }
        return name + "：" + randomLine();
    }

    /** 是否配置了"一次性任务" */
    private boolean hasSimpleQuest() {
        return requireItemId != null || rewardItemId != null
                || creditReward > 0 || needSolved > 0;
    }

    /** 原有的一次性任务逻辑 */
    private String simpleTalk(Player player) {
        if (needSolved > 0 && player.getSolvedCount() < needSolved) {
            return name + "：" + hint + "\n（进度：已答对 " + player.getSolvedCount()
                    + "/" + needSolved + " 题）";
        }
        if (requireItemId != null && !player.getBag().has(requireItemId)) {
            return name + "：" + hint;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("：").append(randomLine());
        if (requireItemId != null) {
            Item required = CampusData.itemById(requireItemId);
            player.getBag().remove(requireItemId, 1);
            sb.append("\n（你递上了「").append(required == null ? requireItemId : required.getName())
                    .append("」）");
        }
        if (creditReward > 0) {
            sb.append("\n").append(player.addCredit(creditReward));
        }
        if (rewardItemId != null) {
            Item reward = CampusData.itemById(rewardItemId);
            if (reward != null && player.getBag().add(reward)) {
                sb.append("\n你获得了「").append(reward.getName()).append("」。");
            }
        }
        finished = true;
        return sb.toString();
    }

    private String randomLine() {
        return lines[random.nextInt(lines.length)];
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public int getRoomId() {
        return roomId;
    }

    public boolean isFinished() {
        return finished;
    }
}
